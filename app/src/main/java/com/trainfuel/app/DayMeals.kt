package com.trainfuel.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Previous / next week arrows with the date range in the middle. */
@Composable
fun WeekSelector(vm: AppViewModel) {
    val start = vm.weekStart(vm.selectedDate)
    val fmt = DateTimeFormatter.ofPattern("MMM d")
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = { vm.shiftWeek(-1) }) { Text("◀", color = TfYellow) }
        Text(
            "${start.format(fmt)} – ${start.plusDays(6).format(fmt)}",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = { vm.shiftWeek(1) }) { Text("▶", color = TfYellow) }
    }
}

/** Training day / rest day switch. Later this can be driven by Health Connect. */
@Composable
fun TrainingToggle(vm: AppViewModel, date: LocalDate) {
    val p = vm.profile ?: return
    val training = vm.isTrainingDay(date)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                if (training) "Training day" else "Rest day",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (training) "+${TargetCalculator.sessionKcal(p)} kcal for your ${p.sport.label} session"
                else "Lower calories, recovery focus",
                color = TfMuted,
                fontSize = 12.sp
            )
        }
        Switch(
            checked = training,
            onCheckedChange = { vm.setTrainingDay(date, it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = TfBlack,
                checkedTrackColor = TfYellow,
                uncheckedThumbColor = TfMuted,
                uncheckedTrackColor = TfSurface2
            )
        )
    }
}

/** The meal slots of one day: add / change / remove and the "eaten" check. */
@Composable
fun DayMeals(vm: AppViewModel, date: LocalDate) {
    var picking by remember { mutableStateOf<Slot?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        vm.slotsFor(date).forEach { slot ->
            val e = vm.entry(date, slot)
            val recipe = e?.let { RecipeData.byId[it.recipeId] }
            TfCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Label("${slot.emoji}  ${slot.label}")
                        Spacer(Modifier.height(4.dp))
                        if (e != null && recipe != null) {
                            Text(
                                recipe.name,
                                color = if (e.eaten) TfMuted else Color.White,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = if (e.eaten) TextDecoration.LineThrough else null
                            )
                            Text(
                                "${fmtServ(e.servings)} serving  •  ${macroLine(vm.macrosOf(e))}",
                                color = TfMuted,
                                fontSize = 12.sp
                            )
                        } else {
                            Text("Nothing planned", color = TfMuted, fontSize = 14.sp)
                        }
                    }
                    if (e != null) {
                        Checkbox(
                            checked = e.eaten,
                            onCheckedChange = { vm.toggleEaten(date, slot) },
                            colors = tfCheckboxColors()
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { picking = slot }) {
                        Text(if (e == null) "+ Add" else "Change", color = TfYellow)
                    }
                    if (e != null) {
                        TextButton(onClick = { vm.removeEntry(date, slot) }) {
                            Text("Remove", color = TfMuted)
                        }
                    }
                }
            }
        }
    }

    picking?.let { s -> SlotPickerDialog(vm, date, s) { picking = null } }
}
