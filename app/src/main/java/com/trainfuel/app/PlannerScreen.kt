package com.trainfuel.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
private fun DayTab(
    date: LocalDate,
    selected: Boolean,
    training: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) TfYellow else TfSurface2
    val fg = if (selected) TfBlack else Color.White
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3),
            color = fg,
            fontSize = 11.sp
        )
        Text(date.dayOfMonth.toString(), color = fg, fontWeight = FontWeight.Bold)
        Text(
            if (training) "●" else "○",
            color = if (selected) TfBlack else TfYellow,
            fontSize = 8.sp
        )
    }
}

@Composable
fun PlannerScreen(vm: AppViewModel) {
    val date = vm.selectedDate
    val target = vm.target(date)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Meal planner", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
        WeekSelector(vm)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            vm.weekDays(date).forEach { d ->
                DayTab(d, d == date, vm.isTrainingDay(d), Modifier.weight(1f)) { vm.selectDate(d) }
            }
        }
        Text("● training day   ○ rest day", color = TfMuted, fontSize = 11.sp)

        if (target != null) {
            val planned = vm.planned(date)
            TfCard {
                TrainingToggle(vm, date)
                Spacer(Modifier.height(12.dp))
                Label("Planned vs target")
                MacroBar("Calories", planned.kcal, target.kcal, "kcal")
                MacroBar("Protein", planned.protein, target.protein, "g")
                MacroBar("Carbs", planned.carbs, target.carbs, "g")
                MacroBar("Fat", planned.fat, target.fat, "g")
                Spacer(Modifier.height(4.dp))
                Text(statusLine(planned, target), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.autoFill(date) }, modifier = Modifier.weight(1f)) {
                    Text("✨ Auto-plan day", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = { vm.autoFillWeek(date) }, modifier = Modifier.weight(1f)) {
                    Text("Auto-plan week", color = TfYellow)
                }
            }

            val weekPlanned = vm.weekPlanned(date)
            val weekTarget = vm.weekTarget(date)
            val weekEntries = vm.weekEntries(date)
            TfCard {
                Label("This week")
                MacroBar("Weekly calories planned", weekPlanned.kcal, weekTarget.kcal, "kcal")
                Text(
                    "Meals eaten: ${weekEntries.count { it.eaten }} / ${weekEntries.size}",
                    color = TfMuted,
                    fontSize = 12.sp
                )
            }
        }

        SectionTitle("Meals")
        DayMeals(vm, date)
    }
}
