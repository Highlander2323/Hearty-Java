package com.example.foodapp.kotlin

import android.util.Log
import java.sql.Connection
import java.sql.PreparedStatement

class CheckDiets(var idIngredients: List<MutableMap<String?, String?>>, var connection: Connection?, var servings: Int, var idRecipe: String) {
    var fatsTotal = 0f
    var carbsTotal = 0f
    var proteinTotal = 0f
    var totalDiets = 0f
    fun calculateValues() {
        val query = "select * from Ingredient where ing_id = ?"
        var ps: PreparedStatement
        try {
            for (i in idIngredients.indices) {
                ps = connection!!.prepareStatement(query)
                ps.setString(1, idIngredients[i]["Id"])
                val rs = ps.executeQuery()
                while (rs.next()) {
                    fatsTotal += rs.getString("ing_fats").toFloat() * idIngredients[i]["Amount"]!!.toInt() / 100
                    carbsTotal += rs.getString("ing_carbs").toFloat() * idIngredients[i]["Amount"]!!.toInt() / 100
                    proteinTotal += rs.getString("ing_protein").toFloat() * idIngredients[i]["Amount"]!!.toInt() / 100
                }
            }
        } catch (e: Exception) {
            Log.e("ERROR DB: ", e.message!!)
        }
    }

    fun checkDiets() {
        var query = "select diet_id from Diet"
        var ps: PreparedStatement
        var ok: Boolean
        try {
            ps = connection!!.prepareStatement(query)
            val rs = ps.executeQuery()
            while (rs.next()) {
                totalDiets++
            }
        } catch (e: Exception) {
            Log.e("ERROR DB: ", e.message!!)
        }
        try {
            var i = 1
            while (i <= totalDiets) {
                query = "select * from Diet where diet_id = ?"
                ok = true
                ps = connection!!.prepareStatement(query)
                ps.setString(1, i.toString())
                val rs = ps.executeQuery()
                while (rs.next()) {
                    if (fatsTotal / servings > rs.getString("diet_max_fats").toFloat()) {
                        ok = false
                    }
                    if (carbsTotal / servings > rs.getString("diet_max_carbs").toFloat()) {
                        ok = false
                    }
                    if (proteinTotal / servings > rs.getString("diet_max_protein").toFloat()) {
                        ok = false
                    }
                    if (ok == true) {
                        query = "insert into DietRecipeLink values (?,?)"
                        ps = connection!!.prepareStatement(query)
                        ps.setString(1, rs.getString("diet_id"))
                        ps.setString(2, idRecipe)
                        ps.execute()
                    }
                }
                i++
            }
        } catch (e: Exception) {
            Log.e("ERROR DB: ", e.message!!)
        }
    }
}