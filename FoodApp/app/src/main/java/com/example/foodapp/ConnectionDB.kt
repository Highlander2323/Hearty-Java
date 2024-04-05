package com.example.foodapp

import android.annotation.SuppressLint
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import java.sql.Connection
import java.sql.DriverManager

class ConnectionDB {
    lateinit var connection: Connection
    var ip: String? = null
    var port: String? = null
    var db: String? = null
    var user: String? = null
    var pass: String? = null
    @SuppressLint("NewApi")
    fun connect(): Connection {
        try {
            ip = "rombintel.go.ro"
            db = "FoodApp"
            port = "1433"
            user = "sa"
            pass = "tartar2323"
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            val connectionURL: String
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            connectionURL = "jdbc:jtds:sqlserver://$ip:$port/$db;Connection Timeout=10"
            connection = DriverManager.getConnection(connectionURL, user, pass)
        } catch (e: Exception) {
            Log.e("Error : ", e.message!!)
        }
        return connection
    }
}