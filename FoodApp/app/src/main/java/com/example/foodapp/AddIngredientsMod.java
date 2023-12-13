package com.example.foodapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.PreparedStatement;

public class AddIngredientsMod extends AppCompatActivity {
    protected EditText boxName, boxProtein, boxFats, boxCarbs, boxSugars, boxCalories;
    protected Button btnAdd, btnCancel;

    private void initViews(){
        boxName = findViewById(R.id.add_ingredient_mod_box_ing_name);
        boxFats = findViewById(R.id.add_ingredient_mod_box_ing_fats);
        boxCarbs = findViewById(R.id.add_ingredient_mod_box_ing_carbs);
        boxProtein = findViewById(R.id.add_ingredient_mod_box_ing_protein);
        boxSugars = findViewById(R.id.add_ingredient_mod_box_ing_sugars);
        boxCalories = findViewById(R.id.add_ingredient_mod_box_ing_calories);
        btnAdd = findViewById(R.id.add_ingredient_mod_btn_add_ingredient);
        btnCancel = findViewById(R.id.add_ingredient_mod_btn_cancel_ingredient);
    }

    private void setOnClickViews(){
        btnAdd.setOnClickListener(view -> onClickAdd());
        btnCancel.setOnClickListener(view -> onClickCancel());
    }

    private void onClickAdd(){
        String query = "insert into [Ingredient] values (?,?,?,?,?,?)";
        try {
            PreparedStatement ps = MainActivity.connection.prepareStatement(query);
            ps.setString(1,boxName.getText().toString());
            ps.setString(2,boxFats.getText().toString());
            ps.setString(3,boxProtein.getText().toString());
            ps.setString(4,boxCarbs.getText().toString());
            ps.setString(5,boxSugars.getText().toString());
            ps.setString(6,boxCalories.getText().toString());
            ps.execute();
        }
        catch(Exception e){
            Log.e("DB Error:", e.getMessage());
            Toast.makeText(this, "PROBLEM", Toast.LENGTH_SHORT).show();
        }
        boxName.setText("");
        boxFats.setText("");
        boxProtein.setText("");
        boxCarbs.setText("");
        boxSugars.setText("");
        boxCalories.setText("");
    }
    private void checkConnection(){
        try {
            if (MainActivity.connection.isClosed() || MainActivity.connection == null) {
                ConnectionDB con = new ConnectionDB();
                MainActivity.connection = con.connect();
            }
        }
        catch (Exception e){
            Log.e("DB ERROR: ", e.getMessage());
        }
    }
    private void onClickCancel(){
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredients_mod);
        initViews();
        setOnClickViews();
        checkConnection();
    }
}