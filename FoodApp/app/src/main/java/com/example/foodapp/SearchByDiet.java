package com.example.foodapp;

import androidx.appcompat.app.AppCompatActivity;

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

public class SearchByDiet extends AppCompatActivity {
    public static String selectedDietId = "", searchedRecipeName = "";
    public static int selectedDietPos = -1;
    public static Button btnCancel, btnSave;
    private TextView txtInfo;
    private String [] info = {"NO RECIPES FOUND!"};
    private ListView listDiets;
    private android.widget.SearchView searchBar;
    private void goBack(){
        selectedDietId = "";
        selectedDietPos = -1;
        searchedRecipeName = "";
        finish();
    }
    private void initViews(){
        btnCancel = findViewById(R.id.search_by_diet_btn_back);
        btnSave = findViewById(R.id.search_by_diet_btn_save);
        listDiets = findViewById(R.id.search_by_diet_list_search_diets);
        txtInfo = findViewById(R.id.search_by_diet_txt_info);
        searchBar = findViewById(R.id.search_by_diet_searchBar);
    }
    private void setOnClickViews(){
        btnCancel.setOnClickListener(view -> goBack());
        btnSave.setOnClickListener(view -> searchRecipes());
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String search) {
                searchedRecipeName = search;
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                searchedRecipeName = newText;
                return true;
            }
        });
    }
    private void searchRecipes(){
        List<Map<String,String>> data = new ArrayList<>();
        CookingFragment.txtCook.setText("");
        try{
            if(MainActivity.connection != null){
                String query = "select R.recipe_id, R.recipe_name, R.recipe_cook_time from Recipe R" +
                        " join DietRecipeLink DRL on DRL.link_recipe_id = R.recipe_id" +
                        " where DRL.link_diet_id = ? and R.recipe_name like ?"+
                        " order by R.recipe_name offset 0 rows fetch next 30 rows " +
                        "only";
                PreparedStatement ps = MainActivity.connection.prepareStatement(query);

                ps.setString(1, selectedDietId);
                ps.setString(2, "%" + searchedRecipeName + "%");

                ResultSet rs = ps.executeQuery();
                if(!rs.next()) {
                    txtInfo.setText(info[0]);
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

            }
            else{
                Toast.makeText(this, "Error: Failed to connect to DB", Toast.LENGTH_LONG).show();
            }
        }
        catch(Exception e){
            Log.e("DB ERROR", e.getMessage());
        }
        CookingFragment.initListRecipes(data);
        goBack();
    }
    private void initList(){
        List<Map<String,String>> data = new ArrayList<>();
        try{
            String query = "select diet_id, diet_name from [Diet]";
            PreparedStatement ps = MainActivity.connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Map<String,String> map = new HashMap<>();
                map.put("IdDiet", rs.getString("diet_id"));
                map.put("NameDiet", rs.getString("diet_name"));
                data.add(map);
            }

        }
        catch(Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("DB ERROR", e.getMessage());
        }
        ListAdapter adapter = new ListAdapter(this, data, ListAdapter.SEARCHED_DIETS);
        listDiets.setAdapter(adapter);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_by_diet);
        initViews();
        setOnClickViews();
        initList();
    }
}