package com.example.foodapp.kotlin

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.R
import java.sql.Connection

class AddToCookbook : AppCompatActivity() {
    var connection: Connection? = null
    protected var listCookbooks: ListView? = null
    private val errorMsg = arrayOf("Recipe already in the Cookbook!")
    protected fun initViews() {
        btnSave = findViewById(R.id.btn_add_to_cookbook_save)
        btnCancel = findViewById(R.id.btn_add_to_cookbook_cancel)
        listCookbooks = findViewById(R.id.list_add_to_cookbook)
    }

    protected fun setOnClickViews() {
        selectedCookbookId = ""
        selectedCookbookPos = -1
        setListView()
        btnSave!!.setOnClickListener { view: View? -> saveAddToCookbook() }
        btnCancel!!.setOnClickListener { view: View? -> cancelAddToCookBook() }
    }

    private fun goBack() {
        finish()
    }

    private val isInserted: Boolean
        private get() {
            try {
                val query = "select * from [LinkCookbookRecipe] where link_recipe_id = ? and" +
                        " link_cb_id = ?"
                val ps = connection!!.prepareStatement(query)
                ps.setString(1, SearchFragment.selectedRecipeId)
                ps.setString(2, selectedCookbookId)
                val rs = ps.executeQuery()
                if (!rs.next()) {
                    return false
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddToCookbook, e.message, Toast.LENGTH_SHORT).show()
            }
            return true
        }

    protected fun saveAddToCookbook() {
        if (isInserted) {
            Toast.makeText(this@AddToCookbook, errorMsg[0], Toast.LENGTH_LONG).show()
            return
        }
        try {
            val query = "insert into [LinkCookbookRecipe] values (?,?)"
            val ps = connection!!.prepareStatement(query)
            ps.setString(1, SearchFragment.selectedRecipeId)
            ps.setString(2, selectedCookbookId)
            ps.execute()
        } catch (e: Exception) {
            Toast.makeText(this@AddToCookbook, e.message, Toast.LENGTH_SHORT).show()
        }
        selectedCookbookId = ""
        selectedCookbookPos = -1
        SearchFragment.selectedRecipeId = ""
        Toast.makeText(this, "Added to cookbook", Toast.LENGTH_SHORT).show()
        goBack()
    }

    protected fun cancelAddToCookBook() {
        selectedCookbookId = ""
        selectedCookbookPos = -1
        SearchFragment.selectedRecipeId = ""
        goBack()
    }

    protected fun setListView() {
        val data: MutableList<Map<String?, String?>> = ArrayList()
        try {
            if (connection != null) {
                val query = "select cb_id, cb_name from [Cookbook] where cb_account_id = ?"
                val ps = connection!!.prepareStatement(query)
                ps.setString(1, MainActivity.idUser)
                val rs = ps.executeQuery()
                while (rs.next()) {
                    val dtrecipe: MutableMap<String?, String?> = HashMap()
                    dtrecipe["IdCookbook"] = rs.getString("cb_id")
                    dtrecipe["NameCookbook"] = rs.getString("cb_name")
                    data.add(dtrecipe)
                }
            } else {
                Toast.makeText(this, "Error: Failed to connect to DB", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
        val adapter = ListAdapter(this, data, ListAdapter.ADD_TO_COOKBOOK, connection)
        listCookbooks!!.adapter = adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_to_cookbook)
        connection = MainActivity.connection
        initViews()
        setOnClickViews()
    }

    companion object {
        var selectedCookbookId: String? = ""
        var selectedCookbookPos = -1
        var btnSave: Button? = null
        protected var btnCancel: Button? = null
    }
}