package com.trainfuel.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DAY_NAMES = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TfMuted, fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TargetBlock(title: String, m: Macros) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        Text(macroLine(m), color = TfYellow, fontSize = 13.sp)
    }
}

@Composable
fun ProfileScreen(vm: AppViewModel) {
    val p = vm.profile ?: return
    val training = TargetCalculator.targetFor(p, true)
    val rest = TargetCalculator.targetFor(p, false)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Profile", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)

        TfCard {
            Label("You")
            Spacer(Modifier.height(6.dp))
            InfoRow("Age / sex", "${p.age} • ${p.sex.label}")
            InfoRow("Height / weight", "${p.heightCm.toInt()} cm • ${p.weightKg} kg")
            InfoRow("Sport", p.sport.label)
            InfoRow("Goal", p.goal.label)
            InfoRow("Training days", p.trainingWeekdays.sorted().joinToString(" ") { DAY_NAMES[it - 1] })
            InfoRow("Session length", "${p.sessionMinutes} min")
        }

        TfCard {
            Label("Your daily targets")
            Spacer(Modifier.height(4.dp))
            TargetBlock("Training day", training)
            TargetBlock("Rest day", rest)
            Text(
                "Training days include about ${TargetCalculator.sessionKcal(p)} kcal for one ${p.sport.label} session.",
                color = TfMuted,
                fontSize = 12.sp
            )
        }

        TfCard {
            Label("Health Connect")
            Spacer(Modifier.height(4.dp))
            Text(
                "Workout sync (Strava, Garmin, Samsung, Wear OS...) is coming next. For now, use the Training day switch on Today or Planner.",
                color = TfMuted,
                fontSize = 13.sp
            )
        }

        Button(onClick = { vm.editingProfile = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Edit profile", fontWeight = FontWeight.Bold)
        }

        Text(
            "Targets are estimates for healthy adults, not medical advice. Check with a doctor or dietitian before big changes to your diet.",
            color = TfMuted,
            fontSize = 11.sp
        )
    }
}
