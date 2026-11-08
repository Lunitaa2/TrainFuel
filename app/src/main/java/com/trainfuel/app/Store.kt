package com.trainfuel.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Tiny SharedPreferences + JSON store. Swap for Room later without touching the UI. */
class Store(context: Context) {
    private val sp = context.getSharedPreferences("trainfuel", Context.MODE_PRIVATE)

    // ---- Profile ----
    fun loadProfile(): Profile? {
        val s = sp.getString("profile", null) ?: return null
        return try {
            val o = JSONObject(s)
            val days = o.getJSONArray("days")
            Profile(
                age = o.getInt("age"),
                sex = Sex.valueOf(o.getString("sex")),
                heightCm = o.getDouble("height"),
                weightKg = o.getDouble("weight"),
                sport = Sport.valueOf(o.getString("sport")),
                goal = Goal.valueOf(o.getString("goal")),
                trainingWeekdays = (0 until days.length()).map { days.getInt(it) }.toSet(),
                sessionMinutes = o.getInt("minutes")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun saveProfile(p: Profile) {
        val o = JSONObject()
            .put("age", p.age)
            .put("sex", p.sex.name)
            .put("height", p.heightCm)
            .put("weight", p.weightKg)
            .put("sport", p.sport.name)
            .put("goal", p.goal.name)
            .put("days", JSONArray(p.trainingWeekdays.toList()))
            .put("minutes", p.sessionMinutes)
        sp.edit().putString("profile", o.toString()).apply()
    }

    // ---- Plan ----
    fun loadPlan(): List<PlanEntry> {
        val s = sp.getString("plan", null) ?: return emptyList()
        return try {
            val a = JSONArray(s)
            (0 until a.length()).map {
                val o = a.getJSONObject(it)
                PlanEntry(
                    date = o.getString("date"),
                    slot = Slot.valueOf(o.getString("slot")),
                    recipeId = o.getString("recipe"),
                    servings = o.getDouble("servings"),
                    eaten = o.getBoolean("eaten")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun savePlan(list: List<PlanEntry>) {
        val a = JSONArray()
        list.forEach {
            a.put(
                JSONObject()
                    .put("date", it.date)
                    .put("slot", it.slot.name)
                    .put("recipe", it.recipeId)
                    .put("servings", it.servings)
                    .put("eaten", it.eaten)
            )
        }
        sp.edit().putString("plan", a.toString()).apply()
    }

    // ---- Training-day overrides (date -> forced training/rest) ----
    fun loadOverrides(): Map<String, Boolean> {
        val s = sp.getString("overrides", null) ?: return emptyMap()
        return try {
            val o = JSONObject(s)
            val out = mutableMapOf<String, Boolean>()
            for (k in o.keys()) out[k] = o.getBoolean(k)
            out
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveOverrides(map: Map<String, Boolean>) {
        val o = JSONObject()
        for ((k, v) in map) o.put(k, v)
        sp.edit().putString("overrides", o.toString()).apply()
    }

    // ---- Manual grocery items ----
    fun loadManual(): List<GroceryItem> {
        val s = sp.getString("manual", null) ?: return emptyList()
        return try {
            val a = JSONArray(s)
            (0 until a.length()).map {
                val o = a.getJSONObject(it)
                GroceryItem(
                    key = o.getString("key"),
                    name = o.getString("name"),
                    qty = o.getString("qty"),
                    checked = o.getBoolean("checked"),
                    auto = false
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveManual(list: List<GroceryItem>) {
        val a = JSONArray()
        list.forEach {
            a.put(
                JSONObject()
                    .put("key", it.key)
                    .put("name", it.name)
                    .put("qty", it.qty)
                    .put("checked", it.checked)
            )
        }
        sp.edit().putString("manual", a.toString()).apply()
    }

    // ---- Checked state of auto-generated grocery items ----
    fun loadChecked(): Set<String> {
        val s = sp.getString("checked", null) ?: return emptySet()
        return try {
            val a = JSONArray(s)
            (0 until a.length()).map { a.getString(it) }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    fun saveChecked(set: Set<String>) {
        sp.edit().putString("checked", JSONArray(set.toList()).toString()).apply()
    }
}
