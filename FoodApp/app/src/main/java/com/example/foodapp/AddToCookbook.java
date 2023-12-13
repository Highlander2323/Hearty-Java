package com.example.foodapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddToCookbook extends AppCompatActivity {
    Connection connection;
    public static String selectedCookbookId = "";
    public static int selectedCookbookPos = -1;
    protected static Button btnSave, btnCancel;
    protected ListView listCookbooks;
    private final String[] errorMsg = {"Recipe already in the Cookbook!"};
    protected void initViews(){
        btnSave = findViewById(R.id.btn_add_to_cookbook_save);
        btnCancel = findViewById(R.id.btn_add_to_cookbook_cancel);
        listCookbooks = findViewById(R.id.list_add_to_cookbook);
    }

    protected void setOnClickViews(){
        selectedCookbookId = "";
        selectedCookbookPos = -1;
        setListView();
        btnSave.setOnClickListener(view -> saveAddToCookbook());
        btnCancel.setOnClickListener(view -> cancelAddToCookBook());
    }

    private void goBack(){
        finish();
    }
    private boolean isInserted(){
        try{
            String query = "select * from [LinkCookbookRecipe] where link_recipe_id = ? and" +
                    " link_cb_id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1,SearchFragment.selectedRecipeId);
            ps.setString(2, selectedCookbookId);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                return false;
            }
        }
        catch(Exception e){
            Toast.makeText(AddToCookbook.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    protected void saveAddToCookbook(){
        if(isInserted()){
            Toast.makeText(AddToCookbook.this, errorMsg[0], Toast.LENGTH_LONG).show();
            return;
        }
        try{
            String query = "insert into [LinkCookbookRecipe] values (?,?)";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1,SearchFragment.selectedRecipeId);
            ps.setString(2, selectedCookbookId);
            ps.execute();
        }
        catch(Exception e){
            Toast.makeText(AddToCookbook.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        selectedCookbookId = "";
        selectedCookbookPos = -1;
        SearchFragment.selectedRecipeId = "";
        Toast.makeText(this, "Added to cookbook", Toast.LENGTH_SHORT).show();
        goBack();
    }

    protected void cancelAddToCookBook(){
        selectedCookbookId = "";
        selectedCookbookPos = -1;
        SearchFragment.selectedRecipeId = "";
        goBack();
    }
    protected void setListView(){
        List<Map<String,String>> data = new ArrayList<>();
        try{
            if(connection != null){
                String query = "select cb_id, cb_name from [Cookbook] where cb_account_id = ?";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, MainActivity.idUser);
                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    Map<String,String> dtrecipe = new HashMap<>();
                    dtrecipe.put("IdCookbook", rs.getString("cb_id"));
                    dtrecipe.put("NameCookbook", rs.getString("cb_name"));
                    data.add(dtrecipe);
                }
            }
            else{
                Toast.makeText(this, "Error: Failed to connect to DB", Toast.LENGTH_LONG).show();
            }
        }
        catch(Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        ListAdapter adapter = new ListAdapter(this, data, ListAdapter.ADD_TO_COOKBOOK, connection);
        listCookbooks.setAdapter(adapter);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_cookbook);
        connection = MainActivity.connection;
        initViews();
        setOnClickViews();
    }
}