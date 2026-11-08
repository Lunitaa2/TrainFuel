package com.trainfuel.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/** Pick a recipe for one slot. Top section = recommendations that fit what is left of the day. */
@Composable
fun SlotPickerDialog(vm: AppViewModel, date: LocalDate, slot: Slot, onDismiss: () -> Unit) {
    val suggestions = vm.suggestions(date, slot)
    val all = RecipeData.all.filter { slot in it.slots }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TfSurface,
        title = {
            Text("${slot.emoji} ${slot.label}", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (suggestions.isNotEmpty()) {
                    item { Label("Recommended for you") }
                    items(suggestions) { s ->
                        RecipeRow(s.recipe.name, s.macros, "×${fmtServ(s.servings)}") {
                            vm.addEntry(date, slot, s.recipe, s.servings)
                            onDismiss()
                        }
                    }
                }
                item { Label("All ${slot.label} meals") }
                items(all) { r ->
                    RecipeRow(r.name, r.macros, "1 serving") {
                        vm.addEntry(date, slot, r, 1.0)
                        onDismiss()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TfYellow) }
        }
    )
}
