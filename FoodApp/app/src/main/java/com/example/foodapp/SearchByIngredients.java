package com.example.foodapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchByIngredients extends AppCompatActivity {

    android.widget.SearchView searchBar;
    Button btnBack, btnSave;
    ListView listSearchedIngredients, listAddedIngredients;
    TextView txtInfo;
    protected String[] info = {"No results :/", "NO RECIPES FOUND!"};
    public static List<Map<String, String>> idIngredients = new ArrayList<>();
    private void initViews(){
        searchBar = findViewById(R.id.search_by_ingredients_searchBar);
        btnBack = findViewById(R.id.search_by_ingredients_btn_back);
        btnSave = findViewById(R.id.search_by_ingredient_btn_save);
        listSearchedIngredients = findViewById(R.id.search_by_ingredients_list_search_ingredients);
        listAddedIngredients = findViewById(R.id.search_by_ingredients_list_added_ingredients);
        txtInfo = findViewById(R.id.search_by_ingredients_txt_ingredients_info);
    }
    private void setOnClickViews(){
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
                    listSearchedIngredients.setAdapter(null);
                return true;
            }
        });
        btnBack.setOnClickListener(view -> goBack());
        btnSave.setOnClickListener(view -> searchRecipe());
    }
    private void goBack(){
        idIngredients.clear();
        finish();
    }
    public void checkIngredients(){
        if(idIngredients.size() > 0){
            btnSave.setEnabled(true);
            btnSave.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                    this, R.color.orange)));
            return;
        }
        btnSave.setEnabled(false);
        btnSave.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#66000000")));

    }
    private void searchRecipe(){
        List<Map<String,String>> data = new ArrayList<>();
        CookingFragment.txtCook.setText("");
        try{
            if(MainActivity.connection != null){
                String query = "select R.recipe_id, R.recipe_name, R.recipe_cook_time from Recipe R" +
                        " join RecipeIngredient RI on RI.link_recipe_id = R.recipe_id" +
                        " where RI.link_ing_id in (";
                for(int i = 0; i < idIngredients.size() - 1; i++) {
                    query += "?,";
                }
                query += " ?) group by R.recipe_id, R.recipe_name, R.recipe_cook_time" +
                        " having count(*) = ?";
                PreparedStatement ps = MainActivity.connection.prepareStatement(query);
                for(int i = 1 ; i <= idIngredients.size(); i++){
                    ps.setString(i, idIngredients.get(i - 1).get("Id"));
                }
                ps.setString(idIngredients.size() + 1, String.valueOf(idIngredients.size()));

                ResultSet rs = ps.executeQuery();
                if(!rs.next()) {
                    txtInfo.setText(info[1]);
                    rs.close();
                    return;
                }
                do{
                    Map<String,String> dtrecipe = new HashMap<>();
                    dtrecipe.put("Id", rs.getString("recipe_id"));
                    dtrecipe.put("Name", rs.getString("recipe_name"));
                    dtrecipe.put("Cook Time", rs.getString("recipe_cook_time"));
                    data.add(dtrecipe);
                }while(rs.next());
                CookingFragment.initListRecipes(data);
            }
            else{
                Toast.makeText(this, "Error: Failed to connect to DB", Toast.LENGTH_LONG).show();
            }
        }
        catch(Exception e){
            Log.e("DB ERROR", e.getMessage());
        }
        goBack();
    }
    public void initAddedIngredients(){
        ListAdapter adapter = new ListAdapter(this, idIngredients, ListAdapter.ADDED_INGREDIENTS);
        listAddedIngredients.setAdapter(adapter);
    }
    private void callSearch(String search) {
        txtInfo.setText("");
        listSearchedIngredients.setAdapter(null);
        List<Map<String,String>> data = new ArrayList<>();
        try{
            if(MainActivity.connection != null){
                String query = "select ing_id,ing_name from [Ingredient] where ing_name like ?" +
                        " order by ing_name offset 0 rows fetch next 15 rows only";
                PreparedStatement ps = MainActivity.connection.prepareStatement(query);
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
            Log.e("DB ERROR", e.getMessage());
        }

        ListAdapter adapter = new ListAdapter(this, data, ListAdapter.SEARCHED_INGREDIENTS);
        listSearchedIngredients.setAdapter(adapter);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_by_ingredients);
        initViews();
        setOnClickViews();
    }
}