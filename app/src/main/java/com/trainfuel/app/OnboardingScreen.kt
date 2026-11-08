package com.trainfuel.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DAY_LABELS = listOf("M", "T", "W", "T", "F", "S", "S")

private fun validate(
    age: String,
    height: String,
    weight: String,
    sex: Sex,
    sport: Sport,
    goal: Goal,
    days: Set<Int>,
    minutes: Int
): Pair<Profile?, String?> {
    val a = age.toIntOrNull()
    if (a == null || a < 18 || a > 80) return null to "Age must be between 18 and 80."
    val h = height.toDoubleOrNull()
    if (h == null || h < 120 || h > 230) return null to "Height must be between 120 and 230 cm."
    val w = weight.toDoubleOrNull()
    if (w == null || w < 35 || w > 200) return null to "Weight must be between 35 and 200 kg."
    if (days.size < MIN_TRAINING_DAYS) {
        return null to "TrainFuel is for people who train. Pick at least $MIN_TRAINING_DAYS training days."
    }
    return Profile(a, sex, h, w, sport, goal, days, minutes) to null
}

@Composable
fun OnboardingScreen(
    initial: Profile?,
    onSave: (Profile) -> Unit,
    onCancel: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var age by remember { mutableStateOf(initial?.age?.toString() ?: "") }
    var sex by remember { mutableStateOf(initial?.sex ?: Sex.MALE) }
    var height by remember { mutableStateOf(initial?.heightCm?.toInt()?.toString() ?: "") }
    var weight by remember { mutableStateOf(initial?.weightKg?.toString() ?: "") }
    var sport by remember { mutableStateOf(initial?.sport ?: Sport.HYROX) }
    var goal by remember { mutableStateOf(initial?.goal ?: Goal.PERFORMANCE) }
    var days by remember { mutableStateOf(initial?.trainingWeekdays ?: setOf(1, 3, 5)) }
    var minutes by remember { mutableStateOf((initial?.sessionMinutes ?: 60).toFloat()) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier.verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Brand()
        Text(
            if (initial == null) "Let's fuel your training" else "Edit your profile",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            "Tell us about you and your training. We calculate your daily calories and macros, and they change on training days and rest days.",
            color = TfMuted,
            fontSize = 14.sp
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = age,
                onValueChange = { age = it.filter { c -> c.isDigit() }.take(3) },
                label = { Text("Age") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = tfFieldColors(),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = height,
                onValueChange = { height = it.filter { c -> c.isDigit() }.take(3) },
                label = { Text("Height (cm)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = tfFieldColors(),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                label = { Text("Weight (kg)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = tfFieldColors(),
                modifier = Modifier.weight(1f)
            )
        }

        Label("Sex")
        ChipSelector(Sex.values().toList(), sex, { it.label }) { sex = it }

        Label("Your sport")
        ChipSelector(Sport.values().toList(), sport, { it.label }) { sport = it }

        Label("Your goal")
        ChipSelector(Goal.values().toList(), goal, { it.label }) { goal = it }

        Label("Training days (pick at least $MIN_TRAINING_DAYS)")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DAY_LABELS.forEachIndexed { index, label ->
                val day = index + 1
                val on = day in days
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (on) TfYellow else TfSurface2)
                        .clickable { days = if (on) days - day else days + day }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(label, color = if (on) TfBlack else Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Label("Typical session length: ${minutes.toInt()} min")
        Slider(
            value = minutes,
            onValueChange = { minutes = it },
            valueRange = 30f..180f,
            steps = 9
        )

        error?.let { Text(it, color = TfRed, fontSize = 13.sp) }

        Button(
            onClick = {
                val (profile, problem) = validate(age, height, weight, sex, sport, goal, days, minutes.toInt())
                if (profile != null) {
                    error = null
                    onSave(profile)
                } else {
                    error = problem
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (initial == null) "Build my plan" else "Save changes", fontWeight = FontWeight.Bold)
        }

        if (onCancel != null) {
            TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = TfMuted)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
