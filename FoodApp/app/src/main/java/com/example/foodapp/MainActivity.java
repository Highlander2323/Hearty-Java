package com.example.foodapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.*;
import android.os.Bundle;

import com.example.foodapp.databinding.ActivityMainBinding;

import java.sql.Connection;


public class MainActivity extends AppCompatActivity {
    protected ActivityMainBinding binding;
    static protected Connection connection;
    static protected String idUser = "";
    private ImageView imgSearch;
    private TextView txtSearch;
    private ImageView imgRecipes;
    private TextView txtRecipes;
    private ImageView imgCooking;
    private TextView txtCooking;
    @Override
    public void onBackPressed(){
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
    private void setOnClickViews(){
        binding.searchBtn.setOnClickListener(btn -> {
            goSearch(new SearchFragment());
        });

        binding.recipesBtn.setOnClickListener(btn -> {
            goRecipes(new RecipesFragment());
        });

        binding.cookingBtn.setOnClickListener(btn -> {
            goCooking(new CookingFragment());
        });
    }
    private void initViews(){
        imgSearch = findViewById(R.id.icon_search);
        txtSearch = findViewById(R.id.txt_search);
        imgRecipes = findViewById(R.id.icon_recipes);
        txtRecipes = findViewById(R.id.txt_recipes);
        imgCooking = findViewById(R.id.icon_cooking);
        txtCooking = findViewById(R.id.txt_cooking);

        if(idUser.equals("")) {
            Bundle bundle = getIntent().getExtras();
            idUser = bundle.getString("id");
        }
    }
    private void replaceFragment(Fragment frg){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frameMain, frg);
        ft.commit();
    }
    private void goSearch(Fragment frg){

        imgSearch.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.search_selected));
        txtSearch.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.orange));
        imgRecipes.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.recipe));
        txtRecipes.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        imgCooking.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.cooking));
        txtCooking.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));

        replaceFragment(frg);
    }
    private void goRecipes(Fragment frg){

        imgSearch.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.search));
        txtSearch.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        imgRecipes.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.recipe_selected));
        txtRecipes.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.orange));
        imgCooking.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.cooking));
        txtCooking.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        replaceFragment(frg);
    }
    private void goCooking(Fragment frg){

        imgSearch.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.search));
        txtSearch.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        imgRecipes.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.recipe));
        txtRecipes.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        imgCooking.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.cooking_selected));
        txtCooking.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.orange));
        replaceFragment(frg);
    }
    @Override
    protected void onDestroy(){
        try {
            connection.close();
        }
        catch(Exception e){
            Toast.makeText(this, e.getMessage(),Toast.LENGTH_LONG).show();
            Log.e("ERROR", e.getMessage());
        }
        super.onDestroy();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // If there was previous data inserted into the create recipe form, we delete it upon
        // entering MainActivity
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("recipeDetails", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initViews();
        setOnClickViews();

            try {
                if(connection == null || connection.isClosed()) {
                    ConnectionDB con = new ConnectionDB();
                    connection = con.connect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
            }

        // APP STARTS ON THE RECIPES PAGE;
        replaceFragment(new RecipesFragment());

    }

}