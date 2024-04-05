package com.example.foodapp

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.sql.PreparedStatement

class CookbookPage : AppCompatActivity() {
    private lateinit var searchBar: SearchView
    private lateinit var txtTitle: TextView
    private lateinit var txtDescription: TextView
    private lateinit var btnCancel: Button
    private lateinit var btnActions: Button
    private lateinit var listRecipes: ListView

    // Functionality for the buttons inside the Delete Cookbook Dialog Interface
    var deleteCookbookListener = DialogInterface.OnClickListener { _: DialogInterface?, which: Int ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                deleteCookbook()
            }

            DialogInterface.BUTTON_NEGATIVE -> {}
        }
    }


    private fun initViews() {
        searchBar = findViewById(R.id.cookbook_page_search_bar)
        btnCancel = findViewById(R.id.cookbook_page_btn_cancel)
        btnActions = findViewById(R.id.cookbook_page_btn_actions)
        listRecipes = findViewById(R.id.cookbook_page_list_recipes)
        txtTitle = findViewById(R.id.cookbook_page_box_cookbook_title)
        txtDescription = findViewById(R.id.cookbook_page_box_cookbook_description)
        setListView()
        check()
    }

    private fun setOnClickViews() {
        btnCancel.setOnClickListener { click: View? -> clickCancel() }
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(search: String): Boolean {
                searchBar.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText != "") search(newText) else {
                    listRecipes.adapter = recipesAdapter
                }
                return true
            }
        })

        // If the cookbook page is for the favorites, don't make the Action button invisible
        if (RecipesFragment.Companion.selectedCookbookId != "") {
            btnActions.setOnClickListener { view: View? -> onActionsClick() }
        } else {
            btnActions.visibility = View.INVISIBLE
        }
    }

    private fun deleteCookbook() {
        val query = "delete from [Cookbook] where cb_id = ?"
        try {
            val ps: PreparedStatement = MainActivity.connection!!.prepareStatement(query)
            ps.setString(1, RecipesFragment.selectedCookbookId)
            ps.execute()
        } catch (e: Exception) {
            Log.e("DB Error: ", e.message!!)
            Toast.makeText(this, "PROBLEM!!!", Toast.LENGTH_LONG).show()
        }
        goBack()
    }

    private fun onActionsClick() {
        val popup = PopupMenu(this, btnActions)
        popup.menuInflater.inflate(R.menu.cookbook_actions_btn_popup_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.delete_cookbook -> {
                    ButtonChecks.deleteCookbookCheck(this, deleteCookbookListener)
                }

                R.id.edit_cookbook -> {
                    val prefs = getSharedPreferences("cookbookDetails", MODE_PRIVATE)
                    prefs.edit().putString("cookbookTitle", txtTitle.text.toString()).commit()
                    prefs.edit().putString("cookbookDescription", txtDescription.text.toString()).commit()
                    val goToCreateCookbook = Intent(this, CreateCookbook::class.java)
                    startActivity(goToCreateCookbook)
                    finish()
                }
            }
            true
        }
        popup.show()
    }

    private fun goBack() {
        if (isMyRecipes) {
            val goToProfile = Intent(this, Profile::class.java)
            startActivity(goToProfile)
            finish()
            return
        }
        RecipesFragment.Companion.selectedCookbookId = ""
        recipesAdapter = null
        val main = Intent(this, MainActivity::class.java)
        startActivity(main)
        finish()
    }

    private fun clickCancel() {
        goBack()
    }

    private fun setCookbookDetails() {
        val query = "select cb_name, cb_description from [Cookbook] where cb_id = ?"
        try {
            val ps: PreparedStatement = MainActivity.connection!!.prepareStatement(query)
            ps.setString(1, RecipesFragment.selectedCookbookId)
            val rs = ps.executeQuery()
            rs.next()
            txtTitle.text = rs.getString("cb_name")
            txtDescription.text = rs.getString("cb_description")
        } catch (e: Exception) {
            Log.e("DB Error:", e.message!!)
            Toast.makeText(this, "PROBLEM!!!", Toast.LENGTH_LONG).show()
        }
    }

    private fun setListView() {
        val data: MutableList<Map<String?, String?>> = ArrayList()
        val query: String
        if (isFavorites) {
            query = "select Recipe.recipe_id, Recipe.recipe_name, Recipe.recipe_cook_time" +
                    " from [Recipe] inner join [Favorites] on Favorites.fav_recipe_id =" + " Recipe.recipe_id where Favorites.fav_account_id = ?"
        } else if (isMyRecipes) {
            query = "select recipe_id, recipe_name, recipe_cook_time" +
                    " from [Recipe] where recipe_account_id = ?"
        } else {
            query = "select Recipe.recipe_id, Recipe.recipe_name, Recipe.recipe_cook_time" +
                    " from [Recipe] inner join LinkCookbookRecipe on" +
                    " LinkCookbookRecipe.link_recipe_id = " + "Recipe.recipe_id where LinkCookbookRecipe.link_cb_id = ?"
        }
        try {
            if (MainActivity.connection != null) {
                val ps: PreparedStatement = MainActivity.connection!!.prepareStatement(query)
                if (isFavorites || isMyRecipes) {
                    ps.setString(1, MainActivity.idUser)
                } else {
                    ps.setString(1, RecipesFragment.Companion.selectedCookbookId)
                }
                val rs = ps.executeQuery()
                while (rs.next()) {
                    val map: MutableMap<String?, String?> = HashMap()
                    map["Id"] = rs.getString("recipe_id")
                    map["Name"] = rs.getString("recipe_name")
                    map["Cook Time"] = rs.getString("recipe_cook_time")
                    data.add(map)
                }
            }
        } catch (e: Exception) {
            Log.e("ERROR DB:", e.message!!)
        }
        val adapter = ListAdapter(this, data, ListAdapter.RECIPES_IN_COOKBOOK, MainActivity.connection)
        recipesAdapter = adapter
        listRecipes.adapter = adapter
    }

    private fun search(search: String) {
        val data: MutableList<Map<String?, String?>> = ArrayList()
        val query: String
        if (isFavorites) {
            query = "select Recipe.recipe_id, Recipe.recipe_name, Recipe.recipe_cook_time" +
                    " from [Recipe] inner join Favorites on Favorites.fav_recipe_id = " +
                    "Recipe.recipe_id where Favorites.fav_account_id = ? and Recipe.recipe_name" + " like ? order by recipe_name"
        } else if (isMyRecipes) {
            query = "select recipe_id, recipe_name, recipe_cook_time" +
                    " from [Recipe] where recipe_account_id = ? and recipe_name" + " like ? order by recipe_name"
        } else {
            query = "select Recipe.recipe_id, Recipe.recipe_name, Recipe.recipe_cook_time" +
                    " from [Recipe] inner join LinkCookbookRecipe on" +
                    " LinkCookbookRecipe.link_recipe_id = Recipe.recipe_id where" +
                    " LinkCookbookRecipe.link_cb_id = ? and Recipe.recipe_name like ? order by" + " recipe_name"
        }
        try {
            if (MainActivity.connection != null) {
                val ps: PreparedStatement = MainActivity.connection!!.prepareStatement(query)
                if (isFavorites || isMyRecipes) {
                    ps.setString(1, MainActivity.idUser)
                } else {
                    ps.setString(1, RecipesFragment.selectedCookbookId)
                }
                ps.setString(2, "%$search%")
                val rs = ps.executeQuery()
                while (rs.next()) {
                    val map: MutableMap<String?, String?> = HashMap()
                    map["Id"] = rs.getString("recipe_id")
                    map["Name"] = rs.getString("recipe_name")
                    map["Cook Time"] = rs.getString("recipe_cook_time")
                    data.add(map)
                }
                rs.close()
            } else {
                Toast.makeText(this, "Error: Failed to connect to DB", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
        val adapter = ListAdapter(this, data, ListAdapter.RECIPES_IN_COOKBOOK, MainActivity.connection)
        listRecipes.adapter = adapter
    }

    private val isMyRecipes: Boolean
        get() = Profile.isActive
    private val isFavorites: Boolean
        get() = RecipesFragment.selectedCookbookId == "" && !isMyRecipes

    private fun check() {
        if (isFavorites) {
            txtTitle.text = "Favorites"
            txtDescription.text = ""
        } else if (isMyRecipes) {
            txtTitle.text = "My Recipes"
            txtDescription.text = ""
        } else {
            setCookbookDetails()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        clickCancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cookbook_page)
        initViews()
        setOnClickViews()
        check()
    }

    companion object {
        var selectedRecipeId: String? = ""
        var recipesAdapter: ListAdapter? = null
    }
}