package com.trainfuel.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import java.time.LocalDate

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val store = Store(app)

    var profile by mutableStateOf(store.loadProfile())
        private set
    var plan by mutableStateOf(store.loadPlan())
        private set
    var overrides by mutableStateOf(store.loadOverrides())
        private set
    var manual by mutableStateOf(store.loadManual())
        private set
    var checkedAuto by mutableStateOf(store.loadChecked())
        private set
    var selectedDate by mutableStateOf(LocalDate.now())
        private set
    var editingProfile by mutableStateOf(false)

    // ---------------- Profile ----------------
    fun saveProfile(p: Profile) {
        profile = p
        store.saveProfile(p)
        editingProfile = false
    }

    // ---------------- Dates / training days ----------------
    fun selectDate(d: LocalDate) {
        selectedDate = d
    }

    fun shiftWeek(weeks: Int) {
        selectedDate = selectedDate.plusWeeks(weeks.toLong())
    }

    fun weekStart(d: LocalDate): LocalDate = d.minusDays((d.dayOfWeek.value - 1).toLong())

    fun weekDays(d: LocalDate): List<LocalDate> {
        val s = weekStart(d)
        return (0..6).map { s.plusDays(it.toLong()) }
    }

    fun isTrainingDay(d: LocalDate): Boolean {
        val p = profile ?: return false
        return overrides[d.toString()] ?: (d.dayOfWeek.value in p.trainingWeekdays)
    }

    fun setTrainingDay(d: LocalDate, value: Boolean) {
        overrides = overrides + (d.toString() to value)
        store.saveOverrides(overrides)
    }

    fun slotsFor(d: LocalDate): List<Slot> =
        if (isTrainingDay(d)) Slot.values().toList()
        else listOf(Slot.BREAKFAST, Slot.LUNCH, Slot.DINNER)

    fun target(d: LocalDate): Macros? {
        val p = profile ?: return null
        return TargetCalculator.targetFor(p, isTrainingDay(d))
    }

    // ---------------- Plan ----------------
    fun entry(d: LocalDate, slot: Slot): PlanEntry? =
        plan.firstOrNull { it.date == d.toString() && it.slot == slot }

    private fun dayEntries(d: LocalDate): List<PlanEntry> {
        val slots = slotsFor(d)
        return plan.filter { it.date == d.toString() && it.slot in slots }
    }

    fun macrosOf(e: PlanEntry): Macros =
        (RecipeData.byId[e.recipeId]?.macros ?: Macros()) * e.servings

    fun planned(d: LocalDate): Macros =
        dayEntries(d).fold(Macros()) { acc, e -> acc + macrosOf(e) }

    fun eaten(d: LocalDate): Macros =
        dayEntries(d).filter { it.eaten }.fold(Macros()) { acc, e -> acc + macrosOf(e) }

    fun weekEntries(d: LocalDate): List<PlanEntry> = weekDays(d).flatMap { dayEntries(it) }

    fun weekTarget(d: LocalDate): Macros =
        weekDays(d).fold(Macros()) { acc, x -> acc + (target(x) ?: Macros()) }

    fun weekPlanned(d: LocalDate): Macros =
        weekEntries(d).fold(Macros()) { acc, e -> acc + macrosOf(e) }

    fun addEntry(d: LocalDate, slot: Slot, recipe: Recipe, servings: Double) {
        // A pre/post-workout meal only makes sense on a training day.
        if ((slot == Slot.PRE_WORKOUT || slot == Slot.POST_WORKOUT) && !isTrainingDay(d)) {
            setTrainingDay(d, true)
        }
        val key = d.toString()
        plan = plan.filterNot { it.date == key && it.slot == slot } +
            PlanEntry(key, slot, recipe.id, servings, false)
        store.savePlan(plan)
    }

    fun removeEntry(d: LocalDate, slot: Slot) {
        val key = d.toString()
        plan = plan.filterNot { it.date == key && it.slot == slot }
        store.savePlan(plan)
    }

    fun toggleEaten(d: LocalDate, slot: Slot) {
        val key = d.toString()
        plan = plan.map {
            if (it.date == key && it.slot == slot) it.copy(eaten = !it.eaten) else it
        }
        store.savePlan(plan)
    }

    // ---------------- Recommendations ----------------
    fun suggestions(d: LocalDate, slot: Slot, limit: Int = 3): List<Suggestion> {
        val t = target(d) ?: return emptyList()
        val others = dayEntries(d).filter { it.slot != slot }
        val otherMacros = others.fold(Macros()) { acc, e -> acc + macrosOf(e) }
        val emptyOthers = slotsFor(d).filter { it != slot && entry(d, it) == null }
        val usedToday = others.map { it.recipeId }.toSet()
        val weekUse = weekEntries(d)
            .filter { !(it.date == d.toString() && it.slot == slot) }
            .groupingBy { it.recipeId }
            .eachCount()
        return Recommender.suggest(slot, isTrainingDay(d), t, otherMacros, emptyOthers, usedToday, weekUse, limit)
    }

    fun autoFill(d: LocalDate) {
        slotsFor(d).forEach { s ->
            if (entry(d, s) == null) {
                suggestions(d, s).firstOrNull()?.let { addEntry(d, s, it.recipe, it.servings) }
            }
        }
    }

    fun autoFillWeek(d: LocalDate) {
        weekDays(d).forEach { autoFill(it) }
    }

    // ---------------- Grocery ----------------
    /** Shopping list for the week of [d], merged by ingredient name + unit. */
    fun autoGrocery(d: LocalDate): List<GroceryItem> {
        class Acc(val name: String, val unit: String, var amount: Double)

        val map = linkedMapOf<String, Acc>()
        weekEntries(d).forEach { e ->
            val r = RecipeData.byId[e.recipeId] ?: return@forEach
            r.ingredients.forEach { ing ->
                val key = "${ing.name.lowercase()}|${ing.unit}"
                val acc = map.getOrPut(key) { Acc(ing.name, ing.unit, 0.0) }
                acc.amount += ing.amount * e.servings
            }
        }
        return map.entries
            .map { (key, a) -> GroceryItem(key, a.name, formatQty(a.amount, a.unit), key in checkedAuto, true) }
            .sortedBy { it.name }
    }

    fun toggleGrocery(item: GroceryItem) {
        if (item.auto) {
            checkedAuto = if (item.key in checkedAuto) checkedAuto - item.key else checkedAuto + item.key
            store.saveChecked(checkedAuto)
        } else {
            manual = manual.map { if (it.key == item.key) it.copy(checked = !it.checked) else it }
            store.saveManual(manual)
        }
    }

    fun addManual(name: String) {
        val n = name.trim()
        if (n.isEmpty()) return
        manual = manual + GroceryItem("m|${System.nanoTime()}", n, "", false, false)
        store.saveManual(manual)
    }

    fun deleteManual(key: String) {
        manual = manual.filterNot { it.key == key }
        store.saveManual(manual)
    }

    /** Un-tick everything that was bought and drop ticked manual items. */
    fun resetBasket() {
        checkedAuto = emptySet()
        store.saveChecked(checkedAuto)
        manual = manual.filterNot { it.checked }
        store.saveManual(manual)
    }
}
