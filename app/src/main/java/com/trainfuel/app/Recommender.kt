package com.trainfuel.app

import kotlin.math.abs
import kotlin.math.max

/**
 * Picks recipes that fill the gap left in the day.
 *  1. Work out how much of the remaining macros this slot should cover.
 *  2. For every recipe in the slot, find the serving size that gets closest.
 *  3. Rank by distance (pre-workout favours carbs/low fat, post-workout favours protein),
 *     with a penalty for repeating a recipe today or too often this week.
 */
object Recommender {
    private val servingOptions = listOf(0.5, 0.75, 1.0, 1.25, 1.5, 2.0)

    fun slotShare(slot: Slot, training: Boolean): Double =
        if (training) {
            when (slot) {
                Slot.BREAKFAST -> 0.24
                Slot.PRE_WORKOUT -> 0.09
                Slot.LUNCH -> 0.27
                Slot.POST_WORKOUT -> 0.12
                Slot.DINNER -> 0.28
            }
        } else {
            when (slot) {
                Slot.BREAKFAST -> 0.28
                Slot.LUNCH -> 0.36
                Slot.DINNER -> 0.36
                else -> 0.0
            }
        }

    fun suggest(
        slot: Slot,
        training: Boolean,
        target: Macros,
        otherPlanned: Macros,
        otherEmptySlots: List<Slot>,
        usedToday: Set<String>,
        weekUse: Map<String, Int>,
        limit: Int = 3
    ): List<Suggestion> {
        val share = slotShare(slot, training)
        var denom = share
        otherEmptySlots.forEach { denom += slotShare(it, training) }

        val remaining = Macros(
            max(0, target.kcal - otherPlanned.kcal),
            max(0, target.protein - otherPlanned.protein),
            max(0, target.carbs - otherPlanned.carbs),
            max(0, target.fat - otherPlanned.fat)
        )
        val budget = if (denom > 0.0 && remaining.kcal > 150) {
            remaining * (share / denom)
        } else {
            target * share
        }

        val wP = if (slot == Slot.POST_WORKOUT) 1.3 else 1.0
        val wC = if (slot == Slot.PRE_WORKOUT) 1.2 else 0.6
        val wF = if (slot == Slot.PRE_WORKOUT) 1.2 else 0.4

        fun d(a: Int, b: Int, floor: Int): Double = abs(a - b).toDouble() / max(b, floor)

        fun distance(m: Macros): Double =
            d(m.kcal, budget.kcal, 100) +
                wP * d(m.protein, budget.protein, 10) +
                wC * d(m.carbs, budget.carbs, 10) +
                wF * d(m.fat, budget.fat, 5)

        return RecipeData.all
            .filter { slot in it.slots }
            .map { r ->
                servingOptions.map { s ->
                    val m = r.macros * s
                    var score = distance(m)
                    if (r.id in usedToday) score += 1.0
                    score += 0.12 * (weekUse[r.id] ?: 0)
                    Suggestion(r, s, m, score)
                }.minByOrNull { it.score }!!
            }
            .sortedBy { it.score }
            .take(limit)
    }
}
