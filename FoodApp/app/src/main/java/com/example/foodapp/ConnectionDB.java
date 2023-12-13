package com.example.foodapp;

import java.sql.Connection;
import java.sql.DriverManager;

import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.util.Log;

import java.sql.SQLException;


public class ConnectionDB {
    Connection connection;
    String ip, port, db, user, pass;
    @SuppressLint("NewApi")
    public Connection connect() {
        try {
            ip = "rombintel.go.ro";
            db = "FoodApp";
            port = "1433";
            user = "sa";
            pass = "tartar2323";
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            String connectionURL;
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            connectionURL = "jdbc:jtds:sqlserver://" + ip + ":" + port + "/" + db + ";Connection Timeout=10";
            connection = DriverManager.getConnection(connectionURL, user, pass);
        } catch (Exception e) {
            Log.e("Error : ", e.getMessage());
        }
        return connection;}}
