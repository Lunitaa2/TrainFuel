package com.trainfuel.app

import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Daily target = estimate, not medical advice.
 *  - BMR: Mifflin-St Jeor
 *  - Rest day: BMR x 1.3
 *  - Training day: + estimated net calories of the session (MET based)
 *  - Goal: lose -12%, build +7%, performance 0%
 *  - Protein 1.8-2.2 g/kg, fat >= 0.9 g/kg (and >= 20% kcal), carbs = what is left
 */
object TargetCalculator {
    private const val MIN_KCAL = 1500.0

    fun bmr(p: Profile): Double {
        val base = 10 * p.weightKg + 6.25 * p.heightCm - 5 * p.age
        return if (p.sex == Sex.MALE) base + 5 else base - 161
    }

    /** Net extra calories burned by one session. Replace with Health Connect data later. */
    fun sessionKcal(p: Profile): Int =
        ((p.sport.met - 1.0) * p.weightKg * p.sessionMinutes / 60.0).roundToInt()

    fun targetFor(p: Profile, trainingDay: Boolean): Macros {
        val session = if (trainingDay) sessionKcal(p) else 0
        val maintenance = bmr(p) * 1.3 + session

        val factor = when (p.goal) {
            Goal.LOSE_WEIGHT -> 0.88
            Goal.BUILD_MUSCLE -> 1.07
            Goal.PERFORMANCE -> 1.0
        }
        val kcal = max(MIN_KCAL, maintenance * factor)

        val proteinPerKg = when (p.goal) {
            Goal.LOSE_WEIGHT -> 2.2
            Goal.BUILD_MUSCLE -> 2.0
            Goal.PERFORMANCE -> 1.8
        }
        val protein = p.weightKg * proteinPerKg
        val fat = max(p.weightKg * 0.9, kcal * 0.20 / 9.0)
        val carbs = max(0.0, (kcal - protein * 4 - fat * 9) / 4)

        return Macros(kcal.roundToInt(), protein.roundToInt(), carbs.roundToInt(), fat.roundToInt())
    }
}
