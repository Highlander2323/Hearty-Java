package com.example.foodapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.sql.PreparedStatement

class SearchByIngredients : AppCompatActivity() {
    var searchBar: SearchView? = null
    var btnBack: Button? = null
    var btnSave: Button? = null
    var listSearchedIngredients: ListView? = null
    var listAddedIngredients: ListView? = null
    var txtInfo: TextView? = null
    protected var info = arrayOf("No results :/", "NO RECIPES FOUND!")
    private fun initViews() {
        searchBar = findViewById(R.id.search_by_ingredients_searchBar)
        btnBack = findViewById(R.id.search_by_ingredients_btn_back)
        btnSave = findViewById(R.id.search_by_ingredient_btn_save)
        listSearchedIngredients = findViewById(R.id.search_by_ingredients_list_search_ingredients)
        listAddedIngredients = findViewById(R.id.search_by_ingredients_list_added_ingredients)
        txtInfo = findViewById(R.id.search_by_ingredients_txt_ingredients_info)
    }

    private fun setOnClickViews() {
        searchBar!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(search: String): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText != "") callSearch(newText) else listSearchedIngredients!!.adapter = null
                return true
            }
        })
        btnBack!!.setOnClickListener { view: View? -> goBack() }
        btnSave!!.setOnClickListener { view: View? -> searchRecipe() }
    }

    private fun goBack() {
        idIngredients.clear()
        finish()
    }

    fun checkIngredients() {
        if (idIngredients.size > 0) {
            btnSave!!.isEnabled = true
            btnSave!!.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(
                    this, R.color.orange))
            return
        }
        btnSave!!.isEnabled = false
        btnSave!!.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#66000000"))
    }

    private fun searchRecipe() {
        val data: MutableList<Map<String?, String?>> = ArrayList()
        CookingFragment.txtCook!!.setText("")
        try {
            if (MainActivity.connection != null) {
                var query = "select R.recipe_id, R.recipe_name, R.recipe_cook_time from Recipe R" +
                        " join RecipeIngredient RI on RI.link_recipe_id = R.recipe_id" +
                        " where RI.link_ing_id in ("
                for (i in 0 until idIngredients.size - 1) {
                    query += "?,"
                }
                query += " ?) group by R.recipe_id, R.recipe_name, R.recipe_cook_time" +
                        " having count(*) = ?"
                val ps: PreparedStatement = MainActivity.connection!!.prepareStatement(query)
                for (i in 1..idIngredients.size) {
                    ps.setString(i, idIngredients[i - 1]["Id"])
                }
                ps.setString(idIngredients.size + 1, idIngredients.size.toString())
                val rs = ps.executeQuery()
                if (!rs.next()) {
                    txtInfo!!.text = info[1]
                    rs.close()
                    return
                }
                do {
                    val dtrecipe: MutableMap<String?, String?> = HashMap()
                    dtrecipe["Id"] = rs.getString("recipe_id")
                    dtrecipe["Name"] = rs.getString("recipe_name")
                    dtrecipe["Cook Time"] = rs.getString("recipe_cook_time")
                    data.add(dtrecipe)
                } while (rs.next())
                CookingFragment.initListRecipes(data)
            } else {
                Toast.makeText(this, "Error: Failed to connect to DB", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("DB ERROR", e.message!!)
        }
        goBack()
    }

    fun initAddedIngredients() {
        val adapter = ListAdapter(this, idIngredients, ListAdapter.ADDED_INGREDIENTS)
        listAddedIngredients!!.adapter = adapter
    }

    private fun callSearch(search: String) {
        txtInfo!!.text = ""
        listSearchedIngredients!!.adapter = null
        val data: MutableList<Map<String?, String?>> = ArrayList()
        try {
            if (MainActivity.connection != null) {
                val query = "select ing_id,ing_name from [Ingredient] where ing_name like ?" +
                        " order by ing_name offset 0 rows fetch next 15 rows only"
                val ps: PreparedStatement = MainActivity.connection!!.prepareStatement(query)
                ps.setString(1, "%$search%")
                val rs = ps.executeQuery()
                if (!rs.next()) {
                    txtInfo!!.text = info[0]
                    rs.close()
                    return
                }
                do {
                    val dtrecipe: MutableMap<String?, String?> = HashMap()
                    dtrecipe["Id"] = rs.getString("ing_id")
                    dtrecipe["Name"] = rs.getString("ing_name")
                    data.add(dtrecipe)
                } while (rs.next())
            } else {
                Toast.makeText(this, "Error: Failed to connect to DB", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("DB ERROR", e.message!!)
        }
        val adapter = ListAdapter(this, data, ListAdapter.SEARCHED_INGREDIENTS)
        listSearchedIngredients!!.adapter = adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_by_ingredients)
        initViews()
        setOnClickViews()
    }

    companion object {
        var idIngredients: MutableList<Map<String?, String?>> = ArrayList()
    }
}