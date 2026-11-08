package com.trainfuel.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GroceryScreen(vm: AppViewModel) {
    val items = vm.autoGrocery(vm.selectedDate) + vm.manual
    val toBuy = items.filter { !it.checked }
    val inBasket = items.filter { it.checked }
    var name by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Grocery list", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
        WeekSelector(vm)
        Text("Built from the meals you planned this week. Same ingredients are merged.", color = TfMuted, fontSize = 12.sp)

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Add item") },
                singleLine = true,
                colors = tfFieldColors(),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                vm.addManual(name)
                name = ""
            }) { Text("Add") }
        }

        if (items.isEmpty()) {
            Text(
                "Nothing here yet. Plan some meals and your shopping list builds itself.",
                color = TfMuted,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        if (toBuy.isNotEmpty()) {
            Label("To buy (${toBuy.size})")
            toBuy.forEach { GroceryRow(it, vm) }
        }
        if (inBasket.isNotEmpty()) {
            Label("In basket (${inBasket.size})")
            inBasket.forEach { GroceryRow(it, vm) }
            OutlinedButton(onClick = { vm.resetBasket() }, modifier = Modifier.fillMaxWidth()) {
                Text("Reset basket", color = TfYellow)
            }
        }
    }
}

@Composable
private fun GroceryRow(item: GroceryItem, vm: AppViewModel) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TfSurface)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.checked,
            onCheckedChange = { vm.toggleGrocery(item) },
            colors = tfCheckboxColors()
        )
        Text(
            item.name,
            color = if (item.checked) TfMuted else Color.White,
            textDecoration = if (item.checked) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f)
        )
        Text(item.qty, color = TfMuted, fontSize = 13.sp)
        if (!item.auto) {
            TextButton(onClick = { vm.deleteManual(item.key) }) { Text("✕", color = TfMuted) }
        } else {
            Spacer(Modifier.width(12.dp))
        }
    }
}
