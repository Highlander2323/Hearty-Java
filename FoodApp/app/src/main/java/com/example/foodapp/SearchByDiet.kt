package com.example.foodapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.sql.PreparedStatement

class SearchByDiet : AppCompatActivity() {
    private var txtInfo: TextView? = null
    private val info = arrayOf("NO RECIPES FOUND!")
    private var listDiets: ListView? = null
    private var searchBar: SearchView? = null
    private fun goBack() {
        selectedDietId = ""
        selectedDietPos = -1
        searchedRecipeName = ""
        finish()
    }

    private fun initViews() {
        btnCancel = findViewById(R.id.search_by_diet_btn_back)
        btnSave = findViewById(R.id.search_by_diet_btn_save)
        listDiets = findViewById(R.id.search_by_diet_list_search_diets)
        txtInfo = findViewById(R.id.search_by_diet_txt_info)
        searchBar = findViewById(R.id.search_by_diet_searchBar)
    }

    private fun setOnClickViews() {
        btnCancel!!.setOnClickListener { view: View? -> goBack() }
        btnSave!!.setOnClickListener { view: View? -> searchRecipes() }
        searchBar!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(search: String): Boolean {
                searchedRecipeName = search
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchedRecipeName = newText
                return true
            }
        })
    }

    private fun searchRecipes() {
        val data: MutableList<Map<String?, String?>> = ArrayList()
        CookingFragment.txtCook!!.setText("")
        try {
            if (MainActivity.connection != null) {
                val query = "select R.recipe_id, R.recipe_name, R.recipe_cook_time from Recipe R" +
                        " join DietRecipeLink DRL on DRL.link_recipe_id = R.recipe_id" +
                        " where DRL.link_diet_id = ? and R.recipe_name like ?" +
                        " order by R.recipe_name offset 0 rows fetch next 30 rows " +
                        "only"
                val ps: PreparedStatement = MainActivity.connection!!.prepareStatement(query)
                ps.setString(1, selectedDietId)
                ps.setString(2, "%" + searchedRecipeName + "%")
                val rs = ps.executeQuery()
                if (!rs.next()) {
                    txtInfo!!.text = info[0]
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
            } else {
                Toast.makeText(this, "Error: Failed to connect to DB", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("DB ERROR", e.message!!)
        }
        CookingFragment.initListRecipes(data)
        goBack()
    }

    private fun initList() {
        val data: MutableList<Map<String?, String?>> = ArrayList()
        try {
            val query = "select diet_id, diet_name from [Diet]"
            val ps: PreparedStatement = MainActivity.connection!!.prepareStatement(query)
            val rs = ps.executeQuery()
            while (rs.next()) {
                val map: MutableMap<String?, String?> = HashMap()
                map["IdDiet"] = rs.getString("diet_id")
                map["NameDiet"] = rs.getString("diet_name")
                data.add(map)
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            Log.e("DB ERROR", e.message!!)
        }
        val adapter = ListAdapter(this, data, ListAdapter.SEARCHED_DIETS)
        listDiets!!.adapter = adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_by_diet)
        initViews()
        setOnClickViews()
        initList()
    }

    companion object {
        var selectedDietId: String? = ""
        var searchedRecipeName = ""
        var selectedDietPos = -1
        var btnCancel: Button? = null
        var btnSave: Button? = null
    }
}