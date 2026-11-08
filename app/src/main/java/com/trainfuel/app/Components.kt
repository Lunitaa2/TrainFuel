package com.trainfuel.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.roundToInt

// ---------------- Formatting helpers ----------------

fun macroLine(m: Macros): String = "${m.kcal} kcal  •  P ${m.protein}g  C ${m.carbs}g  F ${m.fat}g"

fun fmtServ(d: Double): String = if (d % 1.0 == 0.0) d.toInt().toString() else d.toString()

private fun trimNum(d: Double): String =
    if (d % 1.0 == 0.0) d.toInt().toString() else String.format(Locale.US, "%.1f", d)

fun formatQty(amount: Double, unit: String): String {
    val r = (amount * 10).roundToInt() / 10.0
    return when {
        unit == "g" && r >= 1000 -> "${trimNum(r / 1000)} kg"
        unit == "ml" && r >= 1000 -> "${trimNum(r / 1000)} L"
        else -> "${trimNum(r)} $unit"
    }
}

fun statusLine(planned: Macros, target: Macros): String {
    val pct = if (target.kcal == 0) 0 else planned.kcal * 100 / target.kcal
    return when {
        planned.kcal == 0 -> "Nothing planned yet. Tap Auto-plan or add meals."
        pct < 90 -> "${target.kcal - planned.kcal} kcal still to plan"
        pct <= 110 -> "On target ✅"
        else -> "${planned.kcal - target.kcal} kcal over target"
    }
}

// ---------------- Building blocks ----------------

@Composable
fun Brand(modifier: Modifier = Modifier) {
    Row(modifier) {
        Text("TRAIN", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 1.sp)
        Text("FUEL", color = TfYellow, fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 1.sp)
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
fun Label(text: String) {
    Text(text.uppercase(), color = TfYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
}

@Composable
fun TfCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = TfSurface),
        border = BorderStroke(1.dp, TfOutline)
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
fun MacroBar(label: String, value: Int, target: Int, unit: String, color: Color = TfYellow) {
    val frac = if (target <= 0) 0f else (value.toFloat() / target).coerceIn(0f, 1f)
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = TfMuted, fontSize = 13.sp)
            Text("$value / $target $unit", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { frac },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = if (target > 0 && value > target * 1.1) TfRed else color,
            trackColor = TfOutline
        )
    }
}

@Composable
fun RecipeRow(name: String, m: Macros, tag: String, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TfSurface2)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                name,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(tag, color = TfYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Text(macroLine(m), color = TfMuted, fontSize = 12.sp)
    }
}

@Composable
fun <T> ChipSelector(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options) { o ->
            FilterChip(
                selected = o == selected,
                onClick = { onSelect(o) },
                label = { Text(label(o)) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = TfSurface2,
                    labelColor = Color.White,
                    selectedContainerColor = TfYellow,
                    selectedLabelColor = TfBlack
                )
            )
        }
    }
}

@Composable
fun tfFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TfYellow,
    unfocusedBorderColor = TfOutline,
    focusedLabelColor = TfYellow,
    unfocusedLabelColor = TfMuted,
    cursorColor = TfYellow,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)

@Composable
fun tfCheckboxColors(): CheckboxColors = CheckboxDefaults.colors(
    checkedColor = TfYellow,
    checkmarkColor = TfBlack,
    uncheckedColor = TfMuted
)
