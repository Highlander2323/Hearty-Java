package com.example.foodapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
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

class Signup : AppCompatActivity() {
    //Connection class for DB connection
    var connection: Connection? = null
    private var user: String? = null
    private var pass: String? = null
    private var re: String? = null
    private var email: String? = null
    private var error: TextView? = null
    private var userEdit: EditText? = null
    private var passEdit: EditText? = null
    private var reEdit: EditText? = null
    private var emailEdit: EditText? = null
    private val errorMessages = arrayOf("NOT ALL FIELDS COMPLETED",
            "USERNAME MUST BE BETWEEN 8 AND 24 CHARACTERS INCLUSIVE",
            "PASSWORD MUST BE BETWEEN 8 AND 32 CHARACTERS INCLUSIVE", "PASSWORDS DO NOT MATCH",
            "ENTER A VALID EMAIL", "USERNAME MUST BE DIFFERENT FROM THE PASSWORD",
            "USERNAME ALREADY IN USE", "Error when connecting to the server")
    private val successMessage = "ACCOUNT CREATED SUCCESSFULLY!"
    private fun checkUserAvailable(): Int {
        var count = 0
        try {
            val db = ConnectionDB()
            val con = db.connect()
            if (con != null) {
                val rs: ResultSet
                val query = "select account_username from dbo.Account where account_username = ?"
                val ps = con.prepareStatement(query)
                ps.setString(1, user)
                rs = ps.executeQuery()
                rs.next()
                count = rs.row
                con.close()
            } else {
                Toast.makeText(this, errorMessages[7], Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("Error: ", e.message!!)
        }
        return count
    }

    private val isFormValid: Int
        private get() = if (user!!.isEmpty() || pass!!.isEmpty() || re!!.isEmpty() || email!!.isEmpty()) {
            0
        } else {
            if (user!!.length > 7 && user!!.length < 25) {
                if (pass!!.length > 7 && pass!!.length < 33) {
                    if (pass == re) {
                        if (isEmailValid(email)) {
                            if (user != pass) {
                                if (checkUserAvailable() == 0) {
                                    -1
                                } else {
                                    6
                                }
                            } else {
                                5
                            }
                        } else {
                            4
                        }
                    } else {
                        3
                    }
                } else {
                    2
                }
            } else {
                1
            }
        }

    protected fun signupClick() {
        // String values from EditText;
        user = userEdit!!.text.toString()
        pass = passEdit!!.text.toString()
        re = reEdit!!.text.toString()
        email = emailEdit!!.text.toString()
        val code = isFormValid
        if (code != -1) {
            error!!.text = errorMessages[code]
        } else {
            error!!.text = ""
            try {
                val db = ConnectionDB()
                connection = db.connect()
                if (connection != null) {

                    // We insert the registered account into the database;
                    val query = "insert into dbo.Account values(0, ?, ?, ?, '')"
                    val st = connection!!.prepareStatement(query)
                    st.setString(1, user)
                    st.setString(2, hashPass(pass!!))
                    st.setString(3, email)
                    st.execute()
                    connection!!.close()
                    val successSignup = Intent(this, Login::class.java)
                    startActivity(successSignup)
                    finish()
                    Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show()
                } else {
                    Log.e("Error: ", "Failed to connect to DB")
                }
            } catch (e: Exception) {
                Log.e("Error: ", e.message!!)
            }
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
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        setContentView(R.layout.activity_signup)

        // Functionality for the <BACK TO LOGIN> button;
        val back = findViewById<TextView>(R.id.txt_back_signup)
        back.setOnClickListener { view: View? ->
            val activityLogin = Intent(this, Login::class.java)
            startActivity(activityLogin)
            finish()
        }

        // TextView for displaying errors;
        error = findViewById(R.id.txt_error_signup)

        // EditText boxes which contain the data the user inserted;
        userEdit = findViewById(R.id.box_username_signup)
        passEdit = findViewById(R.id.box_password_signup)
        reEdit = findViewById(R.id.box_repassword_signup)
        emailEdit = findViewById(R.id.box_email_signup)

        // Functionality for the <SIGN UP> button
        val btnSign = findViewById<Button>(R.id.btn_signup)
        btnSign.setOnClickListener { view: View? -> signupClick() }
    }

    companion object {
        fun hashPass(input: String): String {
            return try {
                // getInstance() method is called with algorithm SHA-512
                val md = MessageDigest.getInstance("SHA-512")

                // digest() method is called
                // to calculate message digest of the input string
                // returned as array of byte
                val messageDigest = md.digest(input.toByteArray())

                // Convert byte array into signum representation
                val nmbr = BigInteger(1, messageDigest)

                // Convert message digest into hex value
                var hashtext = nmbr.toString(16)

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

        private fun isEmailValid(e: CharSequence?): Boolean {
            return if (TextUtils.isEmpty(e)) {
                false
            } else {
                Patterns.EMAIL_ADDRESS.matcher(e).matches()
            }
        }
    }
}