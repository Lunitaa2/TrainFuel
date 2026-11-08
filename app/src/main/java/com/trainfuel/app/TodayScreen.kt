package com.trainfuel.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max

@Composable
fun TodayScreen(vm: AppViewModel) {
    val date = LocalDate.now()
    val target = vm.target(date) ?: return
    val eaten = vm.eaten(date)
    val planned = vm.planned(date)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Brand()
            Text(date.format(DateTimeFormatter.ofPattern("EEE, MMM d")), color = TfMuted)
        }

        TfCard {
            TrainingToggle(vm, date)
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${eaten.kcal}", color = TfYellow, fontSize = 44.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(8.dp))
                Text("/ ${target.kcal} kcal eaten", color = TfMuted, modifier = Modifier.padding(bottom = 8.dp))
            }
            MacroBar("Calories", eaten.kcal, target.kcal, "kcal")
            MacroBar("Protein", eaten.protein, target.protein, "g")
            MacroBar("Carbs", eaten.carbs, target.carbs, "g")
            MacroBar("Fat", eaten.fat, target.fat, "g")
            Spacer(Modifier.height(6.dp))
            Text(
                "Left today: ${max(0, target.kcal - eaten.kcal)} kcal  •  " +
                    "P ${max(0, target.protein - eaten.protein)}g  " +
                    "C ${max(0, target.carbs - eaten.carbs)}g  " +
                    "F ${max(0, target.fat - eaten.fat)}g",
                color = TfMuted,
                fontSize = 12.sp
            )
            Text("Planned: ${planned.kcal} kcal", color = TfMuted, fontSize = 12.sp)
        }

        Button(onClick = { vm.autoFill(date) }, modifier = Modifier.fillMaxWidth()) {
            Text("✨ Auto-plan my day", fontWeight = FontWeight.Bold)
        }

        SectionTitle("Today's meals")
        Text("Tick a meal when you have eaten it.", color = TfMuted, fontSize = 12.sp)
        DayMeals(vm, date)
    }
}
