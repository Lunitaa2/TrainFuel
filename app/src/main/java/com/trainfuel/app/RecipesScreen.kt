package com.trainfuel.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter

@Composable
fun RecipesScreen(vm: AppViewModel) {
    var filter by remember { mutableStateOf<Slot?>(null) }
    var open by remember { mutableStateOf<Recipe?>(null) }
    val f = filter
    val list = RecipeData.all.filter { f == null || f in it.slots }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Recipes", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
        Text("Macros are per serving.", color = TfMuted, fontSize = 12.sp)
        ChipSelector(
            options = listOf<Slot?>(null) + Slot.values().toList(),
            selected = filter,
            label = { it?.label ?: "All" },
            onSelect = { filter = it }
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list) { r ->
                RecipeRow(r.name, r.macros, "${r.minutes} min") { open = r }
            }
        }
    }

    open?.let { r -> RecipeDetailDialog(vm, r) { open = null } }
}

@Composable
fun RecipeDetailDialog(vm: AppViewModel, recipe: Recipe, onDismiss: () -> Unit) {
    var slot by remember { mutableStateOf(recipe.slots.first()) }
    var servings by remember { mutableStateOf(1.0) }
    val date = vm.selectedDate
    val dateLabel = date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TfSurface,
        title = { Text(recipe.name, color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(macroLine(recipe.macros) + " per serving", color = TfYellow, fontSize = 13.sp)
                Text(
                    "${recipe.minutes} min  •  ${recipe.slots.joinToString { it.label }}",
                    color = TfMuted,
                    fontSize = 12.sp
                )
                Label("Ingredients (1 serving)")
                recipe.ingredients.forEach {
                    Text(
                        "• ${it.name}: ${formatQty(it.amount.toDouble(), it.unit)}",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
                Label("Add as")
                ChipSelector(recipe.slots.toList(), slot, { it.label }) { slot = it }
                Label("Servings")
                ChipSelector(listOf(0.5, 1.0, 1.5, 2.0), servings, { "×" + fmtServ(it) }) { servings = it }
            }
        },
        confirmButton = {
            Button(onClick = {
                vm.addEntry(date, slot, recipe, servings)
                onDismiss()
            }) { Text("Add to $dateLabel") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = TfYellow) }
        }
    )
}
