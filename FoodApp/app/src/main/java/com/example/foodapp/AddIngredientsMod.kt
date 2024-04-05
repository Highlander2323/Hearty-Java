package com.example.foodapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.sql.PreparedStatement

class AddIngredientsMod : AppCompatActivity() {
    protected var boxName: EditText? = null
    protected var boxProtein: EditText? = null
    protected var boxFats: EditText? = null
    protected var boxCarbs: EditText? = null
    protected var boxSugars: EditText? = null
    protected var boxCalories: EditText? = null
    protected var btnAdd: Button? = null
    protected var btnCancel: Button? = null
    private fun initViews() {
        boxName = findViewById(R.id.add_ingredient_mod_box_ing_name)
        boxFats = findViewById(R.id.add_ingredient_mod_box_ing_fats)
        boxCarbs = findViewById(R.id.add_ingredient_mod_box_ing_carbs)
        boxProtein = findViewById(R.id.add_ingredient_mod_box_ing_protein)
        boxSugars = findViewById(R.id.add_ingredient_mod_box_ing_sugars)
        boxCalories = findViewById(R.id.add_ingredient_mod_box_ing_calories)
        btnAdd = findViewById(R.id.add_ingredient_mod_btn_add_ingredient)
        btnCancel = findViewById(R.id.add_ingredient_mod_btn_cancel_ingredient)
    }

    private fun setOnClickViews() {
        btnAdd!!.setOnClickListener { view: View? -> onClickAdd() }
        btnCancel!!.setOnClickListener { view: View? -> onClickCancel() }
    }

    private fun onClickAdd() {
        val query = "insert into [Ingredient] values (?,?,?,?,?,?)"
        try {
            val ps: PreparedStatement = MainActivity.connection!!.prepareStatement(query)
            ps.setString(1, boxName!!.text.toString())
            ps.setString(2, boxFats!!.text.toString())
            ps.setString(3, boxProtein!!.text.toString())
            ps.setString(4, boxCarbs!!.text.toString())
            ps.setString(5, boxSugars!!.text.toString())
            ps.setString(6, boxCalories!!.text.toString())
            ps.execute()
        } catch (e: Exception) {
            Log.e("DB Error:", e.message!!)
            Toast.makeText(this, "PROBLEM", Toast.LENGTH_SHORT).show()
        }
        boxName!!.setText("")
        boxFats!!.setText("")
        boxProtein!!.setText("")
        boxCarbs!!.setText("")
        boxSugars!!.setText("")
        boxCalories!!.setText("")
    }

    private fun checkConnection() {
        try {
            if (MainActivity.connection!!.isClosed() || MainActivity.connection == null) {
                val con = ConnectionDB()
                MainActivity.connection = con.connect()
            }
        } catch (e: Exception) {
            Log.e("DB ERROR: ", e.message!!)
        }
    }

    private fun onClickCancel() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_ingredients_mod)
        initViews()
        setOnClickViews()
        checkConnection()
    }
}