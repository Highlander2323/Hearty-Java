package com.example.foodapp.kotlin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.foodapp.R
import com.example.foodapp.databinding.ActivityMainBinding
import java.sql.Connection

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private lateinit var imgSearch: ImageView
    private lateinit var txtSearch: TextView
    private lateinit var imgRecipes: ImageView
    private lateinit var txtRecipes: TextView
    private lateinit var imgCooking: ImageView
    private lateinit var txtCooking: TextView
    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setOnClickViews() {
        binding!!.searchBtn.setOnClickListener { goSearch(SearchFragment()) }
        binding!!.recipesBtn.setOnClickListener { goRecipes(RecipesFragment()) }
        binding!!.cookingBtn.setOnClickListener { goCooking(CookingFragment()) }
    }

    private fun initViews() {
        imgSearch = findViewById(R.id.icon_search)
        txtSearch = findViewById(R.id.txt_search)
        imgRecipes = findViewById(R.id.icon_recipes)
        txtRecipes = findViewById(R.id.txt_recipes)
        imgCooking = findViewById(R.id.icon_cooking)
        txtCooking = findViewById(R.id.txt_cooking)
        if (idUser == "") {
            val bundle = intent.extras
            idUser = bundle!!.getString("id")
        }
    }

    private fun replaceFragment(frg: Fragment) {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.replace(R.id.frameMain, frg)
        ft.commit()
    }

    private fun goSearch(frg: Fragment) {
        imgSearch.setImageDrawable(ContextCompat.getDrawable(applicationContext,
            R.drawable.search_selected
        ))
        txtSearch.setTextColor(ContextCompat.getColor(applicationContext, R.color.orange))
        imgRecipes.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.recipe))
        txtRecipes.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
        imgCooking.setImageDrawable(ContextCompat.getDrawable(applicationContext,
            R.drawable.cooking
        ))
        txtCooking.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
        replaceFragment(frg)
    }

    private fun goRecipes(frg: Fragment) {
        imgSearch.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.search))
        txtSearch.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
        imgRecipes.setImageDrawable(ContextCompat.getDrawable(applicationContext,
            R.drawable.recipe_selected
        ))
        txtRecipes.setTextColor(ContextCompat.getColor(applicationContext, R.color.orange))
        imgCooking.setImageDrawable(ContextCompat.getDrawable(applicationContext,
            R.drawable.cooking
        ))
        txtCooking.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
        replaceFragment(frg)
    }

    private fun goCooking(frg: Fragment) {
        imgSearch.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.search))
        txtSearch.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
        imgRecipes.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.recipe))
        txtRecipes.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
        imgCooking.setImageDrawable(ContextCompat.getDrawable(applicationContext,
            R.drawable.cooking_selected
        ))
        txtCooking.setTextColor(ContextCompat.getColor(applicationContext, R.color.orange))
        replaceFragment(frg)
    }

    override fun onDestroy() {
        try {
            connection!!.close()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            Log.e("ERROR", e.message!!)
        }
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // If there was previous data inserted into the create recipe form, we delete it upon
        // entering MainActivity
        val prefs = applicationContext.getSharedPreferences("recipeDetails", MODE_PRIVATE)
        prefs.edit().clear().apply()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        initViews()
        setOnClickViews()
        try {
            if (connection == null || connection!!.isClosed) {
                val con = ConnectionDB()
                connection = con.connect()
            }
        } catch (e: Exception) {
            Log.e("ERROR", e.message!!)
        }

        // APP STARTS ON THE RECIPES PAGE;
        replaceFragment(RecipesFragment())
    }

    companion object {
        var connection: Connection? = null
        var idUser: String? = ""
    }
}