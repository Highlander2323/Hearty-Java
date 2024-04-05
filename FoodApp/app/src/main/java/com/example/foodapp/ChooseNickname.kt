package com.example.foodapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.sql.Connection

class ChooseNickname : AppCompatActivity() {
    lateinit var connection: Connection
    lateinit var title: TextView
    lateinit var boxNickname: EditText
    lateinit var btnContinue: Button
    var nickname: String? = null
    var titleTextError = arrayOf("NAME MUST HAVE AT LEAST 4 CHARACTERS!",
            "NAME CAN'T BE LONGER THAN 24 CHARACTERS!")

    private fun clickContinue() {
        val db = ConnectionDB()
        connection = db.connect()
        nickname = boxNickname!!.text.toString()
        var bundle = intent.extras
        val id = bundle!!.getString("id")
        if (nickname!!.length < 4) {
            title!!.setTextColor(getColor(R.color.red))
            title!!.text = titleTextError[0]
            return
        } else if (nickname!!.length > 24) {
            title!!.setTextColor(getColor(R.color.red))
            title!!.text = titleTextError[1]
            return
        }
        try {
            val query = "update dbo.Account set account_nickname = ? where account_id = ?"
            val ps = connection!!.prepareStatement(query)
            ps.setString(1, nickname)
            ps.setString(2, id)
            ps.execute()
            connection!!.close()
            val success = Intent(this, MainActivity::class.java)
            bundle = Bundle()
            bundle.putString("id", id)
            success.putExtras(bundle)
            startActivity(success)
        } catch (e: Exception) {
            Log.e("Error: ", e.message!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_nickname)
        boxNickname = findViewById(R.id.box_nickname)
        btnContinue = findViewById(R.id.btn_nickname_continue)
        title = findViewById(R.id.txt_nickname_title)
        btnContinue.setOnClickListener(View.OnClickListener { view: View? -> clickContinue() })
    }
}