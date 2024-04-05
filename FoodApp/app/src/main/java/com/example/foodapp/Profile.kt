package com.example.foodapp

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.sql.PreparedStatement
import java.sql.ResultSet

open class Profile : AppCompatActivity() {
    private lateinit var btnBack: Button
    private lateinit var btnLogout: Button
    private lateinit var btnSave: Button
    private lateinit var boxName: EditText
    protected var idUser: String? = MainActivity.idUser
    private var nameUser: String? = null
    private lateinit var myRecipesLayout: LinearLayout
    private lateinit var myRecipesNumber: TextView
    private var success = arrayOf("Name changed successfully")
    private var nameError = arrayOf("NAME MUST HAVE AT LEAST 4 CHARACTERS!",
            "NAME CAN'T BE LONGER THAN 24 CHARACTERS!")

    override fun onBackPressed() {
        finish()
        isActive = false
    }

    private fun logout() {
        val prefs = applicationContext.getSharedPreferences("userPrefs", MODE_PRIVATE)
        prefs.edit().clear().apply()
        idUser = ""
        val login = Intent(this, Login::class.java)
        startActivity(login)
        finish()
    }

    //Functionality for 'Are you sure you want to quit?' buttons
    var dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                logout()
            }

            DialogInterface.BUTTON_NEGATIVE -> {}
        }
    }

    private fun onClickBtnLogout(context: Context) {
        ButtonChecks.logoutCheck(context, dialogClickListener)
    }

    private fun onClickBtnBack() {
        finish()
        isActive = false
    }

    private fun checkName(): Boolean {
        val name = boxName.text.toString()
        if (name.length < 4) {
            boxName.error = nameError[0]
            return false
        } else if (name.length > 24) {
            boxName.error = nameError[1]
            return false
        }
        return true
    }

    private fun onClickBtnSave() {
        if (!checkName()) {
            return
        }
        try {
            val query = "update dbo.Account set account_nickname = ? where account_id = ?"
            val ps: PreparedStatement = MainActivity.connection!!.prepareStatement(query)
            ps.setString(1, nameUser)
            ps.setString(2, idUser)
            ps.execute()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
        btnSave.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#66000000"))
        btnSave.isEnabled = false
        Toast.makeText(this, success[0], Toast.LENGTH_SHORT).show()
        boxName.onEditorAction(EditorInfo.IME_ACTION_DONE)
        boxName.clearFocus()
    }

    private fun onTextChange(box: EditText?) {
        box!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                checkNameChange()
            }
        })
    }

    private fun checkNameChange() {
        val newName = boxName.text.toString()
        if (newName != nameUser) {
            btnSave.isEnabled = true
            btnSave.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.orange))
        }
        nameUser = newName
    }

    private fun onClickMyRecipes() {
        val goToMyRecipes = Intent(this, CookbookPage::class.java)
        startActivity(goToMyRecipes)
        finish()
    }

    private fun setOnClickViews() {
        myRecipesLayout.setOnClickListener { onClickMyRecipes() }
        btnBack.setOnClickListener { onClickBtnBack() }
        btnSave.setOnClickListener { onClickBtnSave() }
        btnLogout.setOnClickListener { onClickBtnLogout(this) }
        boxName.onFocusChangeListener = OnFocusChangeListener { _: View?, _: Boolean -> checkNameChange() }
        onTextChange(boxName)
    }

    private fun initViews() {
        myRecipesLayout = findViewById(R.id.profile_my_recipes_layout)
        myRecipesNumber = findViewById(R.id.profile_my_recipes_number)
        btnBack = findViewById(R.id.btn_profile_back)
        btnLogout = findViewById(R.id.btn_profile_logout)
        btnSave = findViewById(R.id.btn_profile_save)
        boxName = findViewById(R.id.btn_profile_name)
        var recipesNr = ""
        try {
            var query = "select account_nickname from dbo.Account where account_id = ?"
            var ps: PreparedStatement = MainActivity.connection!!.prepareStatement(query)
            ps.setString(1, idUser)
            var rs: ResultSet = ps.executeQuery()
            rs.next()

            //If account is found, then it initializes the nameUser variable with the account's
            // nickname
            if (rs.row != 0) {
                nameUser = rs.getString(1)
            }
            query = "select count(*) from Recipe where recipe_account_id = ?"
            ps = MainActivity.connection!!.prepareStatement(query)
            ps.setString(1, MainActivity.idUser)
            rs = ps.executeQuery()
            rs.next()
            if (rs.row != 0) {
                recipesNr = rs.getString(1)
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
        boxName.setText(nameUser)
        myRecipesNumber.text = recipesNr
    }

    private fun changeStatusBarColor() {
        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.black)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        changeStatusBarColor()
        initViews()
        setOnClickViews()
        isActive = true
    }

    companion object {
        var isActive = false
    }
}