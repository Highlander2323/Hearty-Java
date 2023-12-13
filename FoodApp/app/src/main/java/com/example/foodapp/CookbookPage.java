package com.example.foodapp;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlinx.coroutines.MainCoroutineDispatcher;

public class CookbookPage extends AppCompatActivity {
    protected android.widget.SearchView searchBar;
    protected TextView txtTitle, txtDescription;
    protected Button btnCancel, btnActions;
    protected ListView listRecipes;
    public static String selectedRecipeId = "";
    protected static ListAdapter recipesAdapter = null;
    // Functionality for the buttons inside the Delete Cookbook Dialog Interface
    DialogInterface.OnClickListener deleteCookbookListener = (dialog, which) -> {
        switch (which){
            case DialogInterface.BUTTON_POSITIVE: {
                deleteCookbook();
                break;
            }
            case DialogInterface.BUTTON_NEGATIVE:
                break;
        }
    };
    private void changeStatusBarColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.black));
        }
    }
    private void initViews(){
        searchBar = findViewById(R.id.cookbook_page_search_bar);
        btnCancel = findViewById(R.id.cookbook_page_btn_cancel);
        btnActions = findViewById(R.id.cookbook_page_btn_actions);
        listRecipes = findViewById(R.id.cookbook_page_list_recipes);
        txtTitle = findViewById(R.id.cookbook_page_box_cookbook_title);
        txtDescription = findViewById(R.id.cookbook_page_box_cookbook_description);
        setListView();
        check();
    }
    private void setOnClickViews(){
        btnCancel.setOnClickListener(click -> clickCancel());
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String search) {
                searchBar.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!newText.equals(""))
                    search(newText);
                else{
                    listRecipes.setAdapter(recipesAdapter);
                }
                return true;
            }
        });

        // If the cookbook page is for the favorites, don't make the Action button invisible
        if(!RecipesFragment.selectedCookbookId.equals("")) {
            btnActions.setOnClickListener(view -> onActionsClick());
        }
        else{
            btnActions.setVisibility(View.INVISIBLE);
        }
    }
    private void deleteCookbook(){
        String query = "delete from [Cookbook] where cb_id = ?";
        try{
            PreparedStatement ps = MainActivity.connection.prepareStatement(query);
            ps.setString(1, RecipesFragment.selectedCookbookId);
            ps.execute();
        }
        catch(Exception e){
            Log.e("DB Error: ", e.getMessage());
            Toast.makeText(this, "PROBLEM!!!", Toast.LENGTH_LONG).show();
        }
        goBack();
    }
    private void onActionsClick(){
        PopupMenu popup = new PopupMenu(this, btnActions);
        popup.getMenuInflater().inflate(R.menu.cookbook_actions_btn_popup_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(menuItem -> {
            switch(menuItem.getItemId()) {
                case R.id.delete_cookbook: {
                    ButtonChecks.deleteCookbookCheck(this, deleteCookbookListener);
                    break;
                }

                case R.id.edit_cookbook: {
                    SharedPreferences prefs = getSharedPreferences("cookbookDetails", Context.MODE_PRIVATE);
                    prefs.edit().putString("cookbookTitle", txtTitle.getText().toString()).commit();
                    prefs.edit().putString("cookbookDescription", txtDescription.getText().toString()).commit();
                    Intent goToCreateCookbook = new Intent(this, CreateCookbook.class);
                    startActivity(goToCreateCookbook);
                    finish();
                    break;
                }
            }
            return true;
        });
        popup.show();
    }
    private void goBack(){
        if(isMyRecipes()){
            Intent goToProfile = new Intent(this, Profile.class);
            startActivity(goToProfile);
            finish();
            return;
        }
        RecipesFragment.selectedCookbookId = "";
        recipesAdapter = null;
        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
        finish();
    }
    private void clickCancel(){
        goBack();
    }
    private void setCookbookDetails(){
        String query = "select cb_name, cb_description from [Cookbook] where cb_id = ?" ;
        try{
            PreparedStatement ps = MainActivity.connection.prepareStatement(query);
            ps.setString(1, RecipesFragment.selectedCookbookId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            txtTitle.setText(rs.getString("cb_name"));
            txtDescription.setText(rs.getString("cb_description"));
        }
        catch(Exception e){
            Log.e("DB Error:", e.getMessage());
            Toast.makeText(this, "PROBLEM!!!", Toast.LENGTH_LONG).show();
        }
    }
    private void setListView(){
        List<Map<String,String>> data = new ArrayList<>();
        String query;

        if(isFavorites()){
            query = "select Recipe.recipe_id, Recipe.recipe_name, Recipe.recipe_cook_time" +
                    " from [Recipe] inner join [Favorites] on Favorites.fav_recipe_id ="+
                    " Recipe.recipe_id where Favorites.fav_account_id = ?";
        }
        else if(isMyRecipes()){
            query = "select recipe_id, recipe_name, recipe_cook_time" +
                    " from [Recipe] where recipe_account_id = ?";
        }
        else{
            query = "select Recipe.recipe_id, Recipe.recipe_name, Recipe.recipe_cook_time" +
                    " from [Recipe] inner join LinkCookbookRecipe on" +
                    " LinkCookbookRecipe.link_recipe_id = " +
                    "Recipe.recipe_id where LinkCookbookRecipe.link_cb_id = ?";
        }

        try{
            if(MainActivity.connection != null) {
                PreparedStatement ps = MainActivity.connection.prepareStatement(query);
                if(isFavorites() || isMyRecipes()){
                    ps.setString(1, MainActivity.idUser);
                }
                else {
                    ps.setString(1, RecipesFragment.selectedCookbookId);
                }
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String, String> map = new HashMap<>();
                    map.put("Id", rs.getString("recipe_id"));
                    map.put("Name", rs.getString("recipe_name"));
                    map.put("Cook Time", rs.getString("recipe_cook_time"));
                    data.add(map);
                }
            }
        }
        catch(Exception e){
            Log.e("ERROR DB:", e.getMessage());
        }

        ListAdapter adapter = new ListAdapter(this, data, ListAdapter.RECIPES_IN_COOKBOOK, MainActivity.connection);
        recipesAdapter = adapter;
        listRecipes.setAdapter(adapter);
    }
    private void search(String search) {
        List<Map<String,String>> data = new ArrayList<>();
        String query;

        if(isFavorites()) {
            query = "select Recipe.recipe_id, Recipe.recipe_name, Recipe.recipe_cook_time" +
                    " from [Recipe] inner join Favorites on Favorites.fav_recipe_id = "+
                    "Recipe.recipe_id where Favorites.fav_account_id = ? and Recipe.recipe_name" +
                    " like ? order by recipe_name";
        }
        else if(isMyRecipes()){
            query = "select recipe_id, recipe_name, recipe_cook_time" +
                    " from [Recipe] where recipe_account_id = ? and recipe_name" +
                    " like ? order by recipe_name";
        }
        else{
            query = "select Recipe.recipe_id, Recipe.recipe_name, Recipe.recipe_cook_time" +
                    " from [Recipe] inner join LinkCookbookRecipe on" +
                    " LinkCookbookRecipe.link_recipe_id = Recipe.recipe_id where" +
                    " LinkCookbookRecipe.link_cb_id = ? and Recipe.recipe_name like ? order by" +
                    " recipe_name";
        }

        try{
            if(MainActivity.connection != null){
                PreparedStatement ps = MainActivity.connection.prepareStatement(query);
                if(isFavorites() || isMyRecipes()){
                    ps.setString(1, MainActivity.idUser);
                }
                else{
                    ps.setString(1, RecipesFragment.selectedCookbookId);
                }
                ps.setString(2,  "%" + search + "%");

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String, String> map = new HashMap<>();
                    map.put("Id", rs.getString("recipe_id"));
                    map.put("Name", rs.getString("recipe_name"));
                    map.put("Cook Time", rs.getString("recipe_cook_time"));
                    data.add(map);
                }
                rs.close();
            }
            else{
                Toast.makeText(this, "Error: Failed to connect to DB", Toast.LENGTH_LONG).show();
            }
        }
        catch(Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        ListAdapter adapter = new ListAdapter(this, data, ListAdapter.RECIPES_IN_COOKBOOK, MainActivity.connection);
        listRecipes.setAdapter(adapter);
    }
    private boolean isMyRecipes(){
        return Profile.isActive;
    }
    private boolean isFavorites(){
        return RecipesFragment.selectedCookbookId.equals("") && !isMyRecipes();
    }
    private void check(){
        if(isFavorites()){
            txtTitle.setText("Favorites");
            txtDescription.setText("");
        }
        else if(isMyRecipes()){
            txtTitle.setText("My Recipes");
            txtDescription.setText("");
        }
            else{
                setCookbookDetails();
            }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        clickCancel();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusBarColor();
        setContentView(R.layout.activity_cookbook_page);
        initViews();
        setOnClickViews();
        check();
    }
}