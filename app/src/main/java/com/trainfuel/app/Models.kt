package com.trainfuel.app

import kotlin.math.roundToInt

/** TrainFuel is built for people who train. Onboarding enforces this minimum. */
const val MIN_TRAINING_DAYS = 3

enum class Sex(val label: String) {
    MALE("Male"),
    FEMALE("Female")
}

/** met = rough intensity of a typical session, used to estimate training-day calories. */
enum class Sport(val label: String, val met: Double) {
    HYROX("Hyrox", 9.0),
    MARATHON("Marathon", 9.5),
    TRIATHLON("Triathlon", 8.5),
    BOXING("Boxing", 8.0),
    GENERAL("General training", 6.0)
}

enum class Goal(val label: String) {
    LOSE_WEIGHT("Lose weight"),
    BUILD_MUSCLE("Build muscle"),
    PERFORMANCE("Performance")
}

/** Declared in chronological order. Pre/post-workout only exist on training days. */
enum class Slot(val label: String, val emoji: String) {
    BREAKFAST("Breakfast", "🍳"),
    PRE_WORKOUT("Pre-workout", "⚡"),
    LUNCH("Lunch", "🥗"),
    POST_WORKOUT("Post-workout", "💪"),
    DINNER("Dinner", "🍽️")
}

/** trainingWeekdays: 1 = Monday ... 7 = Sunday */
data class Profile(
    val age: Int,
    val sex: Sex,
    val heightCm: Double,
    val weightKg: Double,
    val sport: Sport,
    val goal: Goal,
    val trainingWeekdays: Set<Int>,
    val sessionMinutes: Int
)

data class Macros(
    val kcal: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0
) {
    operator fun plus(o: Macros) =
        Macros(kcal + o.kcal, protein + o.protein, carbs + o.carbs, fat + o.fat)

    operator fun times(f: Double) = Macros(
        (kcal * f).roundToInt(),
        (protein * f).roundToInt(),
        (carbs * f).roundToInt(),
        (fat * f).roundToInt()
    )
}

data class Ingredient(val name: String, val amount: Int, val unit: String)

/** macros are PER SERVING. */
data class Recipe(
    val id: String,
    val name: String,
    val slots: Set<Slot>,
    val macros: Macros,
    val minutes: Int,
    val ingredients: List<Ingredient>
)

/** date is ISO text like 2026-11-08. One entry per date + slot. */
data class PlanEntry(
    val date: String,
    val slot: Slot,
    val recipeId: String,
    val servings: Double,
    val eaten: Boolean
)

/** auto = generated from the plan; otherwise typed in by the user. */
data class GroceryItem(
    val key: String,
    val name: String,
    val qty: String,
    val checked: Boolean,
    val auto: Boolean
)

data class Suggestion(
    val recipe: Recipe,
    val servings: Double,
    val macros: Macros,
    val score: Double
)
