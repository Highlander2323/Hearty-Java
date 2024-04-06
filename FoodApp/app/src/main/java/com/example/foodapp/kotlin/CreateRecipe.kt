package com.example.foodapp.kotlin

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.foodapp.R
import java.sql.Connection

class CreateRecipe : AppCompatActivity() {
    private lateinit var btnCancel: Button
    private lateinit var btnCreate: Button
    private lateinit var btnIngredients: Button
    private var idUser: String? = MainActivity.idUser
    private var connection: Connection? = null
    private lateinit var boxName: EditText
    private lateinit var boxDirections: EditText
    private lateinit var txtNameCharLimit: TextView
    private lateinit var txtDirectionsCharLimit: TextView
    private lateinit var boxPreph: AutoCompleteTextView
    private lateinit var boxPrepm: AutoCompleteTextView
    private lateinit var boxCookh: AutoCompleteTextView
    private lateinit var boxCookm: AutoCompleteTextView
    private lateinit var boxServings: AutoCompleteTextView

    protected fun initViews() {
        // Buttons
        btnCreate = findViewById(R.id.btn_recipe_create)
        btnCancel = findViewById(R.id.btn_recipe_cancel)
        btnIngredients = findViewById(R.id.btn_recipe_ingredients)
        // EditTexts
        boxName = findViewById(R.id.box_recipe_name)
        boxDirections = findViewById(R.id.box_recipe_directions)
        // AutoCompleteTextViews
        boxPreph = findViewById(R.id.box_recipe_preptimeh)
        boxPrepm = findViewById(R.id.box_recipe_preptimem)
        boxCookh = findViewById(R.id.box_recipe_cooktimeh)
        boxCookm = findViewById(R.id.box_recipe_cooktimem)
        boxServings = findViewById(R.id.box_recipe_servings)
        // TextViews
        txtDirectionsCharLimit = findViewById(R.id.txt_recipe_directions_char_limit)
        txtNameCharLimit = findViewById(R.id.txt_recipe_name_char_limit)
        // Remember fields if they have been initialized before
        val prefs = getSharedPreferences("recipeDetails", 0)
        if (prefs.contains("recipeName")) {
            boxName.setText(prefs.getString("recipeName", ""))
            boxDirections.setText(prefs.getString("recipeDirections", ""))
            boxPreph.setText(prefs.getString("prepH", "00"))
            boxPrepm.setText(prefs.getString("prepM", "00"))
            boxCookh.setText(prefs.getString("cookH", "00"))
            boxCookm.setText(prefs.getString("cookM", "00"))
            boxServings.setText(prefs.getString("servings", "0"))
        }
    }

    protected fun initBoxesNull() {
        btnIngredients.text = btnIngredients.text.toString() + idIngredients.size
        // We initialize AutoCompleteTextViews to null so that user can't use keyboard for them
        boxPreph.inputType = InputType.TYPE_NULL
        boxPrepm.inputType = InputType.TYPE_NULL
        boxCookh.inputType = InputType.TYPE_NULL
        boxCookm.inputType = InputType.TYPE_NULL
        boxServings.inputType = InputType.TYPE_NULL
    }

    private fun hasName(): Boolean {
        return boxName.text.toString() != ""
    }

    private fun setOnTextChanged() {
        boxName.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, aft: Int) {}
            override fun afterTextChanged(s: Editable) {
                // this will show characters remaining
                if (s.toString().length > 35) {
                    txtNameCharLimit.text = s.toString().length.toString() + "/50"
                } else {
                    txtNameCharLimit.text = ""
                }
            }
        })
        boxDirections.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, aft: Int) {}
            override fun afterTextChanged(s: Editable) {
                // this will show characters remaining
                if (s.toString().length > 1350) {
                    txtDirectionsCharLimit.text = s.toString().length.toString() + "/1500"
                } else {
                    txtDirectionsCharLimit.text = ""
                }
            }
        })
    }

    private fun hasDirections(): Boolean {
        return if (boxDirections.text.toString() == "") {
            false
        } else true
    }

    //Checks if fields meet the requirements to unlock create recipe button.
    private fun checkFields() {
        if (hasName()) {
            if (hasDirections()) if (idIngredients.size >= 3) {
                btnCreate.isEnabled = true
                btnCreate.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(
                        this, R.color.orange
                ))
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

    protected fun onClickBtnCreate() {
        try {
            val db = ConnectionDB()
            connection = db.connect()
            val preptime = (boxPreph.text.toString().toInt() * 60
                    + boxPrepm.text.toString().toInt())
            val cooktime = boxCookh.text.toString().toInt() * 60 + boxCookm.text.toString().toInt()
            var query = "insert into dbo.Recipe(recipe_name, recipe_directions," +
                    " recipe_prep_time, recipe_cook_time, recipe_servings," +
                    " recipe_account_id) values(?,?,?,?,?,?)"
            var ps = connection!!.prepareStatement(query)
            ps.setString(1, boxName.text.toString())
            ps.setString(2, boxDirections.text.toString())
            ps.setString(3, preptime.toString())
            ps.setString(4, cooktime.toString())
            ps.setString(5, boxServings.text.toString())
            ps.setString(6, idUser)
            ps.execute()
            query = "select IDENT_CURRENT(?)"
            ps = connection!!.prepareStatement(query)
            ps.setString(1, "Recipe")
            val rs = ps.executeQuery()
            rs.next()
            val recipeId = rs.getString(1)
            for (i in idIngredients.indices) {
                query = "insert into dbo.RecipeIngredient values(?,?,?)"
                ps = connection!!.prepareStatement(query)
                ps.setString(1, recipeId)
                ps.setString(2, idIngredients[i]["Id"])
                ps.setString(3, idIngredients[i]["Amount"])
                ps.execute()
            }
            val check = CheckDiets(idIngredients, connection, boxServings.text.toString().toInt(), recipeId)
            check.calculateValues()
            check.checkDiets()
            connection!!.close()
            val prefs = applicationContext.getSharedPreferences("recipeDetails", MODE_PRIVATE)
            prefs.edit().clear().apply()

            // Clear the added ingredients
            idIngredients.clear()
            Toast.makeText(this, "Recipe created successfully!", Toast.LENGTH_SHORT).show()
            goBack()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            Log.e("AAAUUUGH", e.message!!)
        }
    }

    // Are you sure you want to cancel dialog box initializing
    var dialogClickListener = DialogInterface.OnClickListener { _: DialogInterface?, which: Int ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val prefs = applicationContext.getSharedPreferences("recipeDetails", MODE_PRIVATE)
                prefs.edit().clear().apply()
                idIngredients.clear()
                goBack()
            }

            DialogInterface.BUTTON_NEGATIVE -> {}
        }
    }

    protected fun goBack() {
        finish()
    }

    protected fun onClickBtnCancel(context: Context?) {
        ButtonChecks.recipeCancelCheck(context, dialogClickListener)
    }

    protected fun onClickBtnIngredients() {
        val goIngredients = Intent(this, AddIngredients::class.java)
        val prefs = applicationContext.getSharedPreferences("recipeDetails", 0)
        prefs.edit().putString("recipeName", boxName.text.toString()).apply()
        prefs.edit().putString("recipeDirections", boxDirections.text.toString()).apply()
        prefs.edit().putString("prepH", boxPreph.text.toString()).apply()
        prefs.edit().putString("prepM", boxPrepm.text.toString()).apply()
        prefs.edit().putString("cookH", boxCookh.text.toString()).apply()
        prefs.edit().putString("cookM", boxCookm.text.toString()).apply()
        prefs.edit().putString("servings", boxServings.text.toString()).apply()
        startActivity(goIngredients)
        finish()
    }

    protected fun initAdapters() {
        val adapterHours = ArrayAdapter(this, android.R.layout.simple_list_item_1, timesHours)
        val adapterMinutes = ArrayAdapter(this, android.R.layout.simple_list_item_1, timesMinutes)
        val adapterServings = ArrayAdapter(this, android.R.layout.simple_list_item_1, servingsChoices)
        boxPreph.setAdapter(adapterHours)
        boxPrepm.setAdapter(adapterMinutes)
        boxCookh.setAdapter(adapterHours)
        boxCookm.setAdapter(adapterMinutes)
        boxServings.setAdapter(adapterServings)
    }

    // For each AutoCompleteTextView, we set a listener for OnFocusChange and OnClick, because
    // we want to display the choices everytime the user taps on the boxes; additionally,
    // we set a OnClickListener for buttons, so we can run the proper function for each button;
    protected fun setOnClickViews() {
        boxPreph.onFocusChangeListener = OnFocusChangeListener { _: View?, _: Boolean -> boxPreph.showDropDown() }
        boxPreph.setOnClickListener { boxPreph.showDropDown() }
        boxPrepm.onFocusChangeListener = OnFocusChangeListener { _: View?, _: Boolean -> boxPrepm.showDropDown() }
        boxPrepm.setOnClickListener { boxPrepm.showDropDown() }
        boxCookh.onFocusChangeListener = OnFocusChangeListener { _: View?, _: Boolean -> boxCookh.showDropDown() }
        boxCookh.setOnClickListener { boxCookh.showDropDown() }
        boxCookm.onFocusChangeListener = OnFocusChangeListener { _: View?, _: Boolean -> boxCookm.showDropDown() }
        boxCookm.setOnClickListener { boxCookm.showDropDown() }
        boxServings.onFocusChangeListener = OnFocusChangeListener { _: View?, _: Boolean -> boxServings.showDropDown() }
        boxServings.setOnClickListener { boxServings.showDropDown() }
        onTextChange(boxName)
        onTextChange(boxDirections)
        btnCancel.setOnClickListener { onClickBtnCancel(this) }
        btnCreate.setOnClickListener { onClickBtnCreate() }
        btnIngredients.setOnClickListener { onClickBtnIngredients() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_recipe)

        // Initialize all views by id;
        initViews()

        // Initialize boxes with Null input type;
        initBoxesNull()

        // Initialize adapters, so each field shows hours, minutes and servings respectively
        initAdapters()

        // Because the hours, minutes and servings boxes don't allow input, we need to
        // automatically show all suggestions on clicking the field; Also set on clicks for the
        // buttons
        setOnClickViews()

        // Set OnTextChangedListeners for name and description, so the user cannot enter
        // more than the set limit of characters; also it displays a counter whenever
        // the user is close to the limit;
        setOnTextChanged()
        checkFields()
    }

    companion object {
        private val servingsChoices = arrayOf("1", "2", "3", "4", "5",
                "6", "7", "8")
        private val timesHours = arrayOf("00", "01", "02", "03", "04", "05", "06", "07",
                "08", "09", "10", "11", "12")
        private val timesMinutes = arrayOf("00", "05", "10", "15", "20", "25", "30", "35",
                "40", "45", "50", "55")
        var idIngredients: MutableList<MutableMap<String?, String?>> = ArrayList()
    }
}