package com.trainfuel.app

/**
 * Curated meals, macros PER SERVING.
 * kcal is computed as 4*protein + 4*carbs + 9*fat so every row is internally consistent.
 * These numbers are starter estimates: verify/adjust them (e.g. with USDA FoodData Central).
 */
object RecipeData {
    private fun i(name: String, amount: Int, unit: String = "g") = Ingredient(name, amount, unit)

    private fun r(
        id: String,
        name: String,
        slots: Set<Slot>,
        p: Int,
        c: Int,
        f: Int,
        minutes: Int,
        vararg ingredients: Ingredient
    ) = Recipe(id, name, slots, Macros(4 * p + 4 * c + 9 * f, p, c, f), minutes, ingredients.toList())

    private val B = Slot.BREAKFAST
    private val PRE = Slot.PRE_WORKOUT
    private val L = Slot.LUNCH
    private val POST = Slot.POST_WORKOUT
    private val D = Slot.DINNER

    val all: List<Recipe> = listOf(
        // ---- Breakfast ----
        r("oats", "Protein Oats Bowl", setOf(B), 38, 68, 9, 10,
            i("Rolled oats", 80), i("Whey protein", 30), i("Banana", 1, "pcs"), i("Milk", 250, "ml")),
        r("scramble", "Veggie Egg Scramble & Toast", setOf(B), 26, 34, 27, 15,
            i("Eggs", 3, "pcs"), i("Spinach", 40), i("Whole-wheat bread", 2, "pcs"), i("Avocado", 70)),
        r("parfait", "Greek Yogurt Berry Parfait", setOf(B, POST), 30, 45, 8, 5,
            i("Greek yogurt", 250), i("Mixed berries", 100), i("Granola", 40), i("Honey", 10)),
        r("overnight", "Peanut Overnight Oats", setOf(B), 24, 62, 16, 5,
            i("Rolled oats", 70), i("Milk", 200, "ml"), i("Peanut butter", 20), i("Chia seeds", 10)),
        r("eggwrap", "Egg White Wrap", setOf(B, L), 34, 40, 10, 10,
            i("Egg whites", 200), i("Whole-wheat tortilla", 1, "pcs"), i("Spinach", 30), i("Cheese", 20)),

        // ---- Pre-workout (carbs first, low fat) ----
        r("bananapb", "Banana & Peanut Butter Toast", setOf(PRE), 12, 52, 12, 5,
            i("Whole-wheat bread", 2, "pcs"), i("Banana", 1, "pcs"), i("Peanut butter", 15)),
        r("ricecake", "Rice Cakes & Honey", setOf(PRE), 6, 48, 2, 3,
            i("Rice cakes", 4, "pcs"), i("Honey", 20)),
        r("dates", "Dates & Almonds Fuel", setOf(PRE), 5, 40, 8, 2,
            i("Dates", 60), i("Almonds", 15)),

        // ---- Lunch / Dinner ----
        r("chickenrice", "Chicken Rice Power Bowl", setOf(L, D), 52, 62, 10, 25,
            i("Chicken breast", 180), i("Rice (dry)", 70), i("Broccoli", 150), i("Olive oil", 10, "ml")),
        r("burrito", "Turkey Burrito Bowl", setOf(L, D), 45, 70, 14, 25,
            i("Ground turkey", 150), i("Rice (dry)", 70), i("Black beans (canned)", 100),
            i("Corn", 50), i("Salsa", 50), i("Cheese", 20)),
        r("tuna", "Tuna Pasta Salad", setOf(L), 42, 75, 12, 20,
            i("Tuna (canned)", 150), i("Pasta (dry)", 90), i("Cherry tomatoes", 100),
            i("Cucumber", 80), i("Olive oil", 10, "ml")),
        r("beef", "Lean Beef & Sweet Potato", setOf(D, L), 45, 55, 18, 30,
            i("Lean ground beef", 170), i("Sweet potato", 300), i("Green beans", 150), i("Olive oil", 5, "ml")),
        r("salmon", "Salmon, Quinoa & Greens", setOf(D), 40, 45, 24, 25,
            i("Salmon fillet", 160), i("Quinoa (dry)", 60), i("Spinach", 60), i("Olive oil", 5, "ml")),
        r("stirfry", "Chicken Stir-Fry & Noodles", setOf(D, L), 42, 68, 14, 20,
            i("Chicken breast", 150), i("Rice noodles (dry)", 80), i("Mixed vegetables", 200),
            i("Soy sauce", 20, "ml"), i("Olive oil", 10, "ml")),
        r("shrimp", "Shrimp Fried Rice", setOf(D), 36, 66, 12, 20,
            i("Shrimp", 170), i("Rice (dry)", 75), i("Eggs", 1, "pcs"), i("Mixed vegetables", 120),
            i("Soy sauce", 15, "ml"), i("Olive oil", 10, "ml")),
        r("curry", "Lentil & Chickpea Curry", setOf(L, D), 28, 75, 14, 30,
            i("Lentils (dry)", 60), i("Chickpeas (canned)", 120), i("Coconut milk", 80, "ml"),
            i("Tomatoes (canned)", 150)),
        r("chickensalad", "Light Chicken Salad", setOf(L), 44, 18, 16, 15,
            i("Chicken breast", 200), i("Mixed greens", 100), i("Cherry tomatoes", 100),
            i("Feta", 30), i("Olive oil", 12, "ml")),
        r("meatballs", "Turkey Meatballs & Zoodles", setOf(D), 40, 22, 15, 30,
            i("Ground turkey", 180), i("Zucchini", 300), i("Tomatoes (canned)", 150),
            i("Parmesan", 15), i("Olive oil", 5, "ml")),

        // ---- Post-workout (protein + carbs) ----
        r("shake", "Whey Recovery Shake", setOf(POST), 42, 55, 4, 5,
            i("Whey protein", 40), i("Banana", 1, "pcs"), i("Milk", 300, "ml"), i("Rolled oats", 30)),
        r("chocmilk", "Chocolate Milk & Banana", setOf(POST), 20, 72, 8, 2,
            i("Chocolate milk", 400, "ml"), i("Banana", 1, "pcs"), i("Whey protein", 10)),
        r("cottage", "Cottage Cheese & Pineapple", setOf(POST), 28, 30, 5, 3,
            i("Cottage cheese", 250), i("Pineapple", 150)),
        r("recoveryrice", "Recovery Rice Bowl", setOf(POST, L), 32, 78, 8, 20,
            i("Chicken breast", 90), i("Rice (dry)", 85), i("Mixed vegetables", 80), i("Soy sauce", 10, "ml"))
    )

    val byId: Map<String, Recipe> = all.associateBy { it.id }
}
