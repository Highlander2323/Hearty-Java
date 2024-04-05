package com.example.foodapp

import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.sql.PreparedStatement

class RecipePage : AppCompatActivity() {
    lateinit var txtRecipeName: TextView
    lateinit var txtRecipeCookTime: TextView
    lateinit var txtRecipePrepTime: TextView
    lateinit  var txtRecipeDirections: TextView
    lateinit var txtRecipeServings: TextView
    lateinit var btnBack: Button
    lateinit var btnFav: ImageButton
    lateinit var ingredients: ListView
    var isFav = false

    protected fun initViews() {
        btnBack = findViewById(R.id.recipe_page_btn_back)
        btnFav = findViewById(R.id.recipe_page_btn_favorite)
        ingredients = findViewById(R.id.recipe_page_list_ingredients)
        txtRecipeName = findViewById(R.id.recipe_page_txt_name)
        txtRecipeCookTime = findViewById(R.id.recipe_page_txt_cooktime_time)
        txtRecipePrepTime = findViewById(R.id.recipe_page_txt_preptime_time)
        txtRecipeDirections = findViewById(R.id.recipe_page_txt_directions)
        txtRecipeDirections.setMovementMethod(ScrollingMovementMethod())
        txtRecipeServings = findViewById(R.id.recipe_page_txt_servings_number)
        // If recipe is already in favorites, button will be checked;
        setFavoriteBtn()
        // Set all text boxes with the recipe details;
        setTextViews()
    }

    protected fun setTextViews() {
        try {
            if (MainActivity.connection != null) {
                val query = "select * from [Recipe] where recipe_id = ?"
                val ps: PreparedStatement = MainActivity.connection!!.prepareStatement(query)
                ps.setString(1, SearchFragment.selectedRecipeId)
                val rs = ps.executeQuery()
                rs.next()
                txtRecipeName!!.text = rs.getString("recipe_name")
                txtRecipeDirections!!.text = rs.getString("recipe_directions")

                // Format preparation time;
                var prepTime = ""
                var cookTime = ""
                val prep = rs.getString("recipe_prep_time").toInt()
                if (prep / 60 < 10) {
                    prepTime += "0"
                }
                prepTime += (prep / 60).toString() + ":"
                if (prep % 60 < 10) {
                    prepTime += "0"
                }
                prepTime += prep % 60
                txtRecipePrepTime!!.text = prepTime

                // Format cook time
                val cook = rs.getString("recipe_cook_time").toInt()
                if (cook / 60 < 10) {
                    cookTime += "0"
                }
                cookTime += (cook / 60).toString() + ":"
                if (cook % 60 < 10) {
                    cookTime += "0"
                }
                cookTime += cook % 60
                txtRecipeCookTime!!.text = cookTime
                txtRecipeServings!!.text = rs.getString("recipe_servings")
            }
        } catch (e: Exception) {
            Log.e("DB Error", e.message!!)
        }
    }

    protected fun setOnClickViews() {
        btnBack!!.setOnClickListener { view: View? -> goBack() }
        setListView()
        btnFav!!.setOnClickListener { view: View? -> setOnClickFav() }
    }

    private fun setOnClickFav() {
        val clickMessage = Toast.makeText(this, "", Toast.LENGTH_SHORT)
        val query: String
        if (isFav) {
            query = "delete from [Favorites] where fav_account_id = ? and " +
                    "fav_recipe_id = ?"
            clickMessage.setText("Recipe removed!")
            clickMessage.show()
            btnFav!!.setImageResource(R.drawable.fav_unchecked)
            isFav = false
        } else {
            query = "insert into [Favorites] values (?,?)"
            clickMessage.setText("Recipe added!")
            clickMessage.show()
            btnFav!!.setImageResource(R.drawable.fav_checked)
            isFav = true
        }
        try {
            val ps: PreparedStatement = MainActivity.connection!!.prepareStatement(query)
            ps.setString(1, MainActivity.idUser)
            ps.setString(2, SearchFragment.selectedRecipeId)
            ps.execute()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setFavoriteBtn() {
        isFav = false
        try {
            if (MainActivity.connection != null) {
                val query = "select * from [Favorites] where fav_account_id = ? and" +
                        " fav_recipe_id = ?"
                val ps: PreparedStatement = MainActivity.connection!!.prepareStatement(query)
                ps.setString(1, MainActivity.idUser)
                ps.setString(2, SearchFragment.selectedRecipeId)
                val rs = ps.executeQuery()
                if (rs.next()) {
                    isFav = true
                }
            }
        } catch (e: Exception) {
            Log.e("DB Error", e.message!!)
        }

        // If reicpe is already in favorites, clicking the button will remove it from favorites
        if (isFav) {
            btnFav!!.setImageResource(R.drawable.fav_checked)
        }
    }

    private fun setListView() {
        val data: MutableList<Map<String?, String?>> = ArrayList()
        try {
            if (MainActivity.connection != null) {
                val query = "select RecipeIngredient.link_amount, Ingredient.ing_name," +
                        " Ingredient.ing_id from [RecipeIngredient] INNER JOIN [Ingredient] ON" +
                        " RecipeIngredient.link_ing_id = Ingredient.ing_id where" +
                        " RecipeIngredient.link_recipe_id = ?"
                val ps: PreparedStatement = MainActivity.connection!!.prepareStatement(query)
                ps.setString(1, SearchFragment.selectedRecipeId)
                val rs = ps.executeQuery()
                while (rs.next()) {
                    val map: MutableMap<String?, String?> = HashMap()
                    map["AmountIngredient"] = rs.getString("link_amount")
                    map["IdIngredient"] = rs.getString("ing_id")
                    map["NameIngredient"] = rs.getString("ing_name")
                    data.add(map)
                }
            } else {
                Toast.makeText(this, "Error: Failed to connect to DB", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("ERROR", e.message!!)
        }
        val adapter = ListAdapter(this, data, ListAdapter.RECIPE_PAGE_INGREDIENTS)
        ingredients!!.adapter = adapter
    }

    private fun initSelectedRecipeId() {
        if (CookbookPage.selectedRecipeId !== "") {
            SearchFragment.selectedRecipeId = CookbookPage.selectedRecipeId
        }
    }

    private fun goBack() {
        SearchFragment.selectedRecipeId = ""
        CookbookPage.selectedRecipeId = ""
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        goBack()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_page)
        try {
            if (MainActivity.connection!!.isClosed()) {
                val con = ConnectionDB()
                MainActivity.connection = con.connect()
            }
        } catch (e: Exception) {
            Log.e("ERROR DB", e.message!!)
        }
        initSelectedRecipeId()
        initViews()
        setOnClickViews()
    }
}