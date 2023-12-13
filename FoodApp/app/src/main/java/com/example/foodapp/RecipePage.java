package com.example.foodapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RecipePage extends AppCompatActivity {
    TextView txtRecipeName, txtRecipeCookTime, txtRecipePrepTime, txtRecipeDirections,
            txtRecipeServings;
    Button btnBack;
    ImageButton btnFav;
    ListView ingredients;
    boolean isFav = false;
    private void changeStatusBarColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.black));
        }
    }
    protected void initViews(){
        btnBack = findViewById(R.id.recipe_page_btn_back);
        btnFav = findViewById(R.id.recipe_page_btn_favorite);
        ingredients = findViewById(R.id.recipe_page_list_ingredients);
        txtRecipeName = findViewById(R.id.recipe_page_txt_name);
        txtRecipeCookTime = findViewById(R.id.recipe_page_txt_cooktime_time);
        txtRecipePrepTime = findViewById(R.id.recipe_page_txt_preptime_time);
        txtRecipeDirections = findViewById(R.id.recipe_page_txt_directions);
        txtRecipeDirections.setMovementMethod(new ScrollingMovementMethod());
        txtRecipeServings = findViewById(R.id.recipe_page_txt_servings_number);
        // If recipe is already in favorites, button will be checked;
        setFavoriteBtn();
        // Set all text boxes with the recipe details;
        setTextViews();
    }
    protected void setTextViews(){
        try {
            if (MainActivity.connection != null) {
                String query = "select * from [Recipe] where recipe_id = ?";
                PreparedStatement ps = MainActivity.connection.prepareStatement(query);
                ps.setString(1, SearchFragment.selectedRecipeId);
                ResultSet rs = ps.executeQuery();
                rs.next();
                txtRecipeName.setText(rs.getString("recipe_name"));
                txtRecipeDirections.setText(rs.getString("recipe_directions"));

                // Format preparation time;
                String prepTime = "", cookTime = "";
                int prep = Integer.parseInt(rs.getString("recipe_prep_time"));
                if((prep / 60) < 10){
                    prepTime += "0";
                }
                prepTime +=  (prep / 60) + ":";
                if((prep % 60) < 10){
                    prepTime += "0";
                }
                prepTime += (prep % 60);
                txtRecipePrepTime.setText(prepTime);

                // Format cook time
                int cook = Integer.parseInt(rs.getString("recipe_cook_time"));
                if((cook / 60) < 10){
                    cookTime += "0";
                }
                cookTime +=  (cook / 60) + ":";
                if((cook % 60) < 10){
                    cookTime += "0";
                }
                cookTime += (cook % 60);
                txtRecipeCookTime.setText(cookTime);

                txtRecipeServings.setText(rs.getString("recipe_servings"));
            }
        }
        catch(Exception e){
            Log.e("DB Error", e.getMessage());
        }
    }
    protected void setOnClickViews(){
        btnBack.setOnClickListener(view -> goBack());
        setListView();
        btnFav.setOnClickListener(view -> setOnClickFav());
    }
    private void setOnClickFav(){
        Toast clickMessage = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        String query;
        if(isFav){
            query = "delete from [Favorites] where fav_account_id = ? and " +
                    "fav_recipe_id = ?";
            clickMessage.setText("Recipe removed!");
            clickMessage.show();
            btnFav.setImageResource(R.drawable.fav_unchecked);
            isFav = false;
        }
        else{
            query = "insert into [Favorites] values (?,?)";
            clickMessage.setText("Recipe added!");

            clickMessage.show();
            btnFav.setImageResource(R.drawable.fav_checked);
            isFav = true;
        }
        try{
            PreparedStatement ps = MainActivity.connection.prepareStatement(query);
            ps.setString(1, MainActivity.idUser);
            ps.setString(2, SearchFragment.selectedRecipeId);
            ps.execute();
        }
        catch(Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();}}
    private void setFavoriteBtn(){
        isFav = false;
        try {
            if (MainActivity.connection != null) {
                String query = "select * from [Favorites] where fav_account_id = ? and" +
                        " fav_recipe_id = ?";
                PreparedStatement ps = MainActivity.connection.prepareStatement(query);
                ps.setString(1, MainActivity.idUser);
                ps.setString(2, SearchFragment.selectedRecipeId);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    isFav = true;
                }
            }
        }
        catch(Exception e){
            Log.e("DB Error", e.getMessage());
        }

        // If reicpe is already in favorites, clicking the button will remove it from favorites
        if(isFav){
            btnFav.setImageResource(R.drawable.fav_checked);
        }
    }
    private void setListView(){
        List<Map<String,String>> data = new ArrayList<>();
        try{
            if(MainActivity.connection != null){
                String query = "select RecipeIngredient.link_amount, Ingredient.ing_name," +
                        " Ingredient.ing_id from [RecipeIngredient] INNER JOIN [Ingredient] ON" +
                        " RecipeIngredient.link_ing_id = Ingredient.ing_id where" +
                        " RecipeIngredient.link_recipe_id = ?";
                PreparedStatement ps = MainActivity.connection.prepareStatement(query);
                ps.setString(1, SearchFragment.selectedRecipeId);
                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    Map<String, String> map = new HashMap<>();
                    map.put("AmountIngredient", rs.getString("link_amount"));
                    map.put("IdIngredient", rs.getString("ing_id"));
                    map.put("NameIngredient", rs.getString("ing_name"));
                    data.add(map);
                }
            }
            else{
                Toast.makeText(this, "Error: Failed to connect to DB", Toast.LENGTH_LONG).show();
            }
        }
        catch(Exception e){
            Log.e("ERROR", e.getMessage());
        }

        ListAdapter adapter = new ListAdapter(this, data, ListAdapter.RECIPE_PAGE_INGREDIENTS);
        ingredients.setAdapter(adapter);
    }
    private void initSelectedRecipeId(){
        if(CookbookPage.selectedRecipeId != ""){
            SearchFragment.selectedRecipeId = CookbookPage.selectedRecipeId;
        }
    }
    private void goBack(){
        SearchFragment.selectedRecipeId = "";
        CookbookPage.selectedRecipeId = "";
        finish();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_page);

        try {
            if (MainActivity.connection.isClosed()) {
                ConnectionDB con = new ConnectionDB();
                MainActivity.connection = con.connect();
            }
        }
        catch(Exception e){
            Log.e("ERROR DB", e.getMessage());
        }

        changeStatusBarColor();
        initSelectedRecipeId();
        initViews();
        setOnClickViews();
    }
}