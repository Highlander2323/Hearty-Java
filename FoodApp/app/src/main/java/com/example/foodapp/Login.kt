package com.example.foodapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.sql.Connection
import java.sql.ResultSet

class Login : AppCompatActivity() {
    // Object that represents the connection to the database;
    private var connection: Connection? = null

    // Buttons for login and signup;
    private var signup: Button? = null
    private var login: Button? = null

    // TextView that represents the area where the error is displayed;
    private var txtError: TextView? = null

    // Objects that represent the text boxes of the username and password;
    private var passEdit: EditText? = null
    private var userEdit: EditText? = null

    // Strings for retrieving username and password from text boxes;
    private var user: String? = null
    private var pass: String? = null

    // Error messages to display respective message;
    private val errorMessages = arrayOf("USERNAME NOT REGISTERED", "INVALID CREDENTIALS",
            "Error when connecting to the server")

    // We check if the credentials are registered to an account;
    private fun checkCredentials(): Boolean {
        var ok = false
        try {
            val db = ConnectionDB()
            connection = db.connect()
            if (connection != null) {
                val rs: ResultSet
                val query = "select account_username from dbo.Account where" +
                        "(account_username = ?  and account_password = ?)"
                val ps = connection!!.prepareStatement(query)
                ps.setString(1, user)
                ps.setString(2, encryptPass(pass))
                rs = ps.executeQuery()
                rs.next()
                //If account is found, then it means the user is registered in the DB
                if (rs.row != 0) {
                    ok = true
                }
                connection!!.close()
            } else {
                Toast.makeText(this, errorMessages[2], Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("Error: ", e.message!!)
        }
        return ok
    }

    // We check if the Nickname field is set in the DB;
    // If the user has no Nickname, it means it is his first time logging in;
    // User is redirected to a first-time activity where he/she will choose it's Nickname;
    private val id: String
        private get() {
            var id = ""
            try {
                val db = ConnectionDB()
                connection = db.connect()
                if (connection != null) {
                    val rs: ResultSet
                    val query = "select account_id from dbo.Account" +
                            " where account_username = ?"
                    val ps = connection!!.prepareStatement(query)
                    ps.setString(1, user)
                    rs = ps.executeQuery()
                    rs.next()
                    id = rs.getString(1)
                    connection!!.close()
                } else {
                    Toast.makeText(this, errorMessages[2], Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("Error: ", e.message!!)
            }
            return id
        }

    private fun hasNickname(): Boolean {
        var hasNick = true
        try {
            val db = ConnectionDB()
            connection = db.connect()
            if (connection != null) {
                val rs: ResultSet
                val query = "select account_nickname from dbo.Account" +
                        " where account_username = ?"
                val ps = connection!!.prepareStatement(query)
                ps.setString(1, user)
                rs = ps.executeQuery()
                rs.next()

                //If account is found, then it means the user is registered in the DB
                if (rs.getString(1) == "") {
                    hasNick = false
                }
                connection!!.close()
            } else {
                Toast.makeText(this, errorMessages[2], Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("Error: ", e.message!!)
        }
        return hasNick
    }

    // We check if the username is registered into the DB;
    private fun checkUsername(): Boolean {
        var ok = false
        try {
            val db = ConnectionDB()
            connection = db.connect()
            if (connection != null) {
                val rs: ResultSet
                val query = "select account_username from dbo.Account" +
                        " where account_username = ?"
                val ps = connection!!.prepareStatement(query)
                ps.setString(1, user)
                rs = ps.executeQuery()
                rs.next()

                //If account is found, then it means the user is registered in the DB
                if (rs.row != 0) {
                    ok = true
                }
                connection!!.close()
            } else {
                Toast.makeText(this, errorMessages[2], Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("Error: ", e.message!!)
        }
        return ok
    }

    private fun setOnClickViews() {
        signup!!.setOnClickListener { view: View? ->
            val activitySignup = Intent(this, Signup::class.java)
            startActivity(activitySignup)
        }
        login!!.setOnClickListener { view: View? -> loginClick() }
    }

    private fun initViews() {
        signup = findViewById(R.id.btn_signup_login)
        login = findViewById(R.id.btn_login)
        userEdit = findViewById(R.id.box_username_login)
        passEdit = findViewById(R.id.box_password_login)
        txtError = findViewById(R.id.txt_error_login)
    }

    private val isFormValid: Short
        private get() {
            user = userEdit!!.text.toString()
            pass = passEdit!!.text.toString()
            return if (checkUsername()) {
                if (checkCredentials()) {
                    -1
                } else {
                    1
                }
            } else {
                0
            }
        }

    private fun setOnChangeText() {
        userEdit!!.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, aft: Int) {}
            override fun afterTextChanged(s: Editable) {
                var str = userEdit!!.text.toString()
                if (str.indexOf(' ') != -1) {
                    str = userEdit!!.text.toString().replace(" ", "")
                    userEdit!!.setText(str)
                }
            }
        })
        passEdit!!.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, aft: Int) {}
            override fun afterTextChanged(s: Editable) {
                var str = passEdit!!.text.toString()
                if (str.indexOf(' ') != -1) {
                    str = passEdit!!.text.toString().replace(" ", "")
                    passEdit!!.setText(str)
                }
            }
        })
    }

    // Method that describes the functionality of the Login button;
    private fun loginClick() {
        val code = isFormValid
        if (code.toInt() != -1) {
            txtError!!.text = errorMessages[code.toInt()]
        } else {
            val successLogin: Intent
            val id = id
            val bundle = Bundle()
            bundle.putString("id", id)
            val prefs = applicationContext.getSharedPreferences("userPrefs", 0)
            prefs.edit().putString("idUser", id).commit()
            successLogin = if (hasNickname()) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, ChooseNickname::class.java)
            }
            successLogin.putExtras(bundle)
            startActivity(successLogin)
            finish()
        }
    }

    private fun checkAlreadyLogin() {
        val prefs = applicationContext.getSharedPreferences("userPrefs", 0)
        val idUser = prefs.getString("idUser", "none")
        if (idUser != "none") {
            val successLogin: Intent
            val bundle = Bundle()
            bundle.putString("id", idUser)
            successLogin = if (hasNickname()) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, ChooseNickname::class.java)
            }
            successLogin.putExtras(bundle)
            startActivity(successLogin)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = this.window

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        window.statusBarColor = ContextCompat.getColor(this, R.color.orange)
        setContentView(R.layout.activity_login)

        // We check the shared preferences to see if the user's id is stored in the internal file;
        // In the case it is present, we automatically log in the user and pass the id within a
        // bundle;
        checkAlreadyLogin()
        initViews()
        setOnClickViews()
        setOnChangeText()
    }

    companion object {
        fun encryptPass(input: String?): String {
            return try {
                // getInstance() method is called with algorithm SHA-512
                val md = MessageDigest.getInstance("SHA-512")

                // digest() method is called
                // to calculate message digest of the input string
                // returned as array of byte
                val messageDigest = md.digest(input!!.toByteArray())

                // Convert byte array into signum representation
                val no = BigInteger(1, messageDigest)

                // Convert message digest into hex value
                var hashtext = no.toString(16)

                // Add preceding 0s to make it 32 bit
                while (hashtext.length < 32) {
                    hashtext = "0$hashtext"
                }

                // return the HashText
                hashtext
            } // For specifying wrong message digest algorithms
            catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e)
            }
        }
    }
}