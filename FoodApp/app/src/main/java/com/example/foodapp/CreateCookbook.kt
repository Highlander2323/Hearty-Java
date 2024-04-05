package com.example.foodapp

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.sql.PreparedStatement

class CreateCookbook : AppCompatActivity() {
    protected var idUser: String? = MainActivity.Companion.idUser
    lateinit private var boxTitle: EditText
    lateinit private var boxDescription: EditText
    lateinit private var btnCreate: Button
    lateinit private var btnCancel: Button
    lateinit private var txtNameCharLimit: TextView
    lateinit private var txtDescriptionCharLimit: TextView

    private fun hasName(): Boolean {
        return boxTitle.text.toString() != ""
    }

    private val isChanged: Boolean
        get() {
            val prefs = getSharedPreferences("cookbookDetails", MODE_PRIVATE)
            return boxTitle.text.toString() != prefs.getString("cookbookTitle", "") ||
                    boxDescription.text.toString() != prefs.getString("cookbookDescription", "")
        }

    private fun initViews() {
        btnCreate = findViewById(R.id.btn_cookbook_create)
        btnCancel = findViewById(R.id.btn_cookbook_cancel)
        boxTitle = findViewById(R.id.box_cookbook_name)
        boxDescription = findViewById(R.id.box_cookbook_description)
        txtNameCharLimit = findViewById(R.id.txt_cookbook_name_char_limit)
        txtDescriptionCharLimit = findViewById(R.id.txt_cookbook_description_char_limit)
        if (RecipesFragment.selectedCookbookId != "") {
            val prefs = getSharedPreferences("cookbookDetails", MODE_PRIVATE)
            boxTitle.setText(prefs.getString("cookbookTitle", ""))
            boxDescription.setText(prefs.getString("cookbookDescription", ""))
        }
    }

    private fun checkFields() {
        if (RecipesFragment.selectedCookbookId == "") {
            if (hasName()) {
                btnCreate.isEnabled = true
                btnCreate.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(
                        this, R.color.orange))
                return
            }
        } else {
            if (isChanged) {
                btnCreate.isEnabled = true
                btnCreate.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(
                        this, R.color.orange))
                return
            }
        }
        btnCreate.isEnabled = false
        btnCreate.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#66000000"))
    }

    private fun onTextChange(box: EditText?) {
        box!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                checkFields()
            }
        })
    }

    private fun setOnTextChanged() {
        boxTitle!!.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, aft: Int) {}
            override fun afterTextChanged(s: Editable) {
                // this will show characters remaining
                if (s.toString().length > 35) {
                    txtNameCharLimit!!.text = s.toString().length.toString() + "/50"
                } else {
                    txtNameCharLimit!!.text = ""
                }
            }
        })
        boxDescription!!.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, aft: Int) {}
            override fun afterTextChanged(s: Editable) {
                // this will show characters remaining
                if (s.toString().length > 135) {
                    txtDescriptionCharLimit!!.text = s.toString().length.toString() + "/150"
                } else {
                    txtDescriptionCharLimit!!.text = ""
                }
            }
        })
    }

    protected fun setOnClickViews() {
        onTextChange(boxTitle)
        btnCancel!!.setOnClickListener { view: View? -> onClickBtnCancel() }
        btnCreate!!.setOnClickListener { view: View? -> onClickBtnCreate() }
        if (RecipesFragment.Companion.selectedCookbookId != "") {
            onTextChange(boxDescription)
        }
    }

    protected fun onClickBtnCreate() {
        if (RecipesFragment.Companion.selectedCookbookId != "") {
            val query = "update dbo.Cookbook set cb_name = ?, cb_description = ? where cb_id = ?"
            try {
                val ps: PreparedStatement = MainActivity.Companion.connection!!.prepareStatement(query)
                ps.setString(1, boxTitle!!.text.toString())
                ps.setString(2, boxDescription!!.text.toString())
                ps.setString(3, RecipesFragment.Companion.selectedCookbookId)
                ps.execute()
            } catch (e: Exception) {
                Log.e("DB Error:", e.message!!)
                Toast.makeText(this, "PROBLEM!!!", Toast.LENGTH_LONG).show()
            }
        } else {
            try {
                val con = ConnectionDB()
                val connection = con.connect()
                val query = "insert into dbo.Cookbook(cb_name, cb_description," +
                        " cb_account_id) values(?,?,?)"
                val ps = connection!!.prepareStatement(query)
                ps.setString(1, boxTitle!!.text.toString())
                ps.setString(2, boxDescription!!.text.toString())
                ps.setString(3, idUser)
                ps.execute()
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        }
        goBack()
    }

    private fun goBack() {
        if (RecipesFragment.Companion.selectedCookbookId != "") {
            val prefs = getSharedPreferences("cookbookDetails", MODE_PRIVATE)
            prefs.edit().clear().apply()
            val goBack = Intent(this, CookbookPage::class.java)
            startActivity(goBack)
            finish()
        } else {
            val goBack = Intent(this, MainActivity::class.java)
            startActivity(goBack)
            finish()
        }
    }

    protected fun onClickBtnCancel() {
        goBack()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_cookbook)

        // Initialize all views by id;
        initViews()
        setOnClickViews()
        setOnTextChanged()
    }
}