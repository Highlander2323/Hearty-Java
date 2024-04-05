package com.example.foodapp

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.sql.Connection

class AddIngredients : AppCompatActivity() {
    protected var btnAdd: ImageButton? = null
    protected var btnRemove: ImageButton? = null
    protected var btnBack: Button? = null
    @JvmField
    var searchBar: SearchView? = null
    protected var idUser = MainActivity.idUser
    protected var info = arrayOf("No results :/")
    protected var connection: Connection? = null
    protected var searchIngredients: ListView? = null
    protected var addedIngredients: ListView? = null
    @JvmField
    var txtInfo: TextView? = null

    // Dialog box for entering amount when adding new ingredient
    @JvmField
    var dialogAddIngredient = DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val data: MutableMap<String?, String?> = HashMap<Any?, Any?>()
                data["Id"] = id
                data["Name"] = name
                data["Amount"] = ButtonChecks.boxIngredientAmount.text.toString()
                CreateRecipe.idIngredients.add(data)
                searchBar!!.clearFocus()
                initAddedIngredients()
            }

            DialogInterface.BUTTON_NEGATIVE -> {}
        }
    }

    fun initAddedIngredients() {
        val adapter = ListAdapter(this, CreateRecipe.idIngredients, ListAdapter.INGREDIENT_ADDED)
        addedIngredients!!.adapter = adapter
    }

    private fun callSearch(search: String) {
        txtInfo!!.text = ""
        searchIngredients!!.adapter = null
        val data: MutableList<Map<String, String>> = ArrayList()
        try {
            if (connection != null) {
                val query = "select ing_id,ing_name from [Ingredient] where ing_name like ?" +
                        " order by ing_name offset 0 rows fetch next 15 rows only"
                val ps = connection!!.prepareStatement(query)
                ps.setString(1, "%$search%")
                val rs = ps.executeQuery()
                if (!rs.next()) {
                    txtInfo!!.text = info[0]
                    rs.close()
                    return
                }
                do {
                    val dtrecipe: MutableMap<String, String> = HashMap()
                    dtrecipe["Id"] = rs.getString("ing_id")
                    dtrecipe["Name"] = rs.getString("ing_name")
                    data.add(dtrecipe)
                } while (rs.next())
            } else {
                Toast.makeText(this, "Error: Failed to connect to DB", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
        val adapter = ListAdapter(this, data, ListAdapter.INGREDIENT_SEARCH)
        searchIngredients!!.adapter = adapter
    }

    private fun initViews() {
        btnAdd = findViewById(R.id.btn_search_ingredient_add)
        btnRemove = findViewById(R.id.btn_added_ingredient_remove)
        btnBack = findViewById(R.id.btn_ingredients_back)
        txtInfo = findViewById(R.id.txt_ingredients_info)
        searchBar = findViewById(R.id.ingredients_searchBar)
        searchIngredients = findViewById(R.id.ingredients_list_search_ingredients)
        addedIngredients = findViewById(R.id.ingredients_list_added_ingredients)
        initAddedIngredients()
    }

    private fun setOnClickViews() {
        btnBack!!.setOnClickListener { view: View? -> goBack() }
        searchBar!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(search: String): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText != "") callSearch(newText) else searchIngredients!!.adapter = null
                return true
            }
        })
    }

    private fun goBack() {
        val goBackRecipe = Intent(this, CreateRecipe::class.java)
        startActivity(goBackRecipe)
        finish()
    }

    public override fun onDestroy() {
        try {
            connection!!.close()
        } catch (e: Exception) {
            Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
            throw RuntimeException(e)
        }
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_ingredients)
        ctx = this
        val con = ConnectionDB()
        connection = con.connect()
        initViews()
        setOnClickViews()
    }

    companion object {
        protected var ctx: Context? = null
        @JvmField
        var id: String? = null
        @JvmField
        var name: String? = null
    }
}