package com.example.foodapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddIngredients extends AppCompatActivity {

    protected ImageButton btnAdd, btnRemove;
    protected Button btnBack;
    protected SearchView searchBar;
    protected String idUser = MainActivity.idUser;
    protected String[] info = {"No results :/"};
    protected Connection connection;
    protected static Context ctx;
    protected ListView searchIngredients, addedIngredients;
    protected TextView txtInfo;
    public static String id, name;

    // Dialog box for entering amount when adding new ingredient
    public DialogInterface.OnClickListener dialogAddIngredient = (dialog, which) -> {
        switch (which){
            case DialogInterface.BUTTON_POSITIVE: {
                Map<String, String> data = new HashMap();
                data.put("Id", id);
                data.put("Name", name);
                data.put("Amount", ButtonChecks.boxIngredientAmount.getText().toString());
                CreateRecipe.idIngredients.add(data);
                searchBar.clearFocus();
                initAddedIngredients();
                break;
            }
            case DialogInterface.BUTTON_NEGATIVE:
                break;
        }
    };

    public void initAddedIngredients(){
        ListAdapter adapter = new ListAdapter(this, CreateRecipe.idIngredients, ListAdapter.INGREDIENT_ADDED);
        addedIngredients.setAdapter(adapter);
    }

    private void callSearch(String search) {
        txtInfo.setText("");
        searchIngredients.setAdapter(null);
        List<Map<String,String>> data = new ArrayList<>();
        try{
            if(connection != null){
                String query = "select ing_id,ing_name from [Ingredient] where ing_name like ?" +
                        " order by ing_name offset 0 rows fetch next 15 rows only";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1,  "%" + search + "%");
                ResultSet rs = ps.executeQuery();
                if(!rs.next()) {
                    txtInfo.setText(info[0]);
                    rs.close();
                    return;
                }
                do{
                    Map<String,String> dtrecipe = new HashMap<>();
                    dtrecipe.put("Id", rs.getString("ing_id"));
                    dtrecipe.put("Name", rs.getString("ing_name"));
                    data.add(dtrecipe);
                }while(rs.next());
            }
            else{
                Toast.makeText(this, "Error: Failed to connect to DB", Toast.LENGTH_LONG).show();
            }
        }
        catch(Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        ListAdapter adapter = new ListAdapter(this, data, ListAdapter.INGREDIENT_SEARCH);
        searchIngredients.setAdapter(adapter);
    }

    private void initViews(){
        btnAdd = findViewById(R.id.btn_search_ingredient_add);
        btnRemove = findViewById(R.id.btn_added_ingredient_remove);
        btnBack = findViewById(R.id.btn_ingredients_back);
        txtInfo = findViewById(R.id.txt_ingredients_info);
        searchBar = findViewById(R.id.ingredients_searchBar);
        searchIngredients = findViewById(R.id.ingredients_list_search_ingredients);
        addedIngredients = findViewById(R.id.ingredients_list_added_ingredients);
        initAddedIngredients();
    }

    private void setOnClickViews(){
        btnBack.setOnClickListener(view->goBack());
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String search) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!newText.equals(""))
                    callSearch(newText);
                else
                    searchIngredients.setAdapter(null);
                return true;
            }
        });
    }

    private void goBack(){
        Intent goBackRecipe = new Intent(this, CreateRecipe.class);
        startActivity(goBackRecipe);
        finish();
    }

    @Override
    public void onDestroy(){
        try {
            connection.close();
        } catch (Exception e) {
            Toast.makeText(ctx, e.getMessage(),Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredients);
        ctx = this;
        ConnectionDB con = new ConnectionDB();
        connection = con.connect();
        initViews();
        setOnClickViews();
    }
}