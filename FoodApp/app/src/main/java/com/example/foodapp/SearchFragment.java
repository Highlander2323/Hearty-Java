package com.example.foodapp;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class SearchFragment extends Fragment {

    //ATTRIBUTES
    protected ListView recipes;
    protected SearchView searchBar;
    protected TextView txtInfo;
    protected ImageView imgInfo;
    protected MainActivity main;
    protected static String selectedRecipeId = "";
    private void initViews(View view){
        recipes = view.findViewById(R.id.search_recipes_list);
        searchBar = view.findViewById(R.id.search_search_bar);
        txtInfo = view.findViewById(R.id.search_recipes_txt);
        imgInfo = view.findViewById(R.id.search_recipes_img);
    }
    private void setOnClickViews(){
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String search) {
                if(!search.equals("")){
                    txtInfo.setVisibility(View.INVISIBLE);
                    imgInfo.setVisibility(View.INVISIBLE);
                    setListView(search);
                }
                else {
                    recipes.setAdapter(null);
                    imgInfo.setImageResource(R.drawable.search_recipe);
                    txtInfo.setText("SEARCH FOR A RECIPE");
                    txtInfo.setVisibility(View.VISIBLE);
                    imgInfo.setVisibility(View.VISIBLE);
                }
                searchBar.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.equals("")){
                    recipes.setAdapter(null);
                    imgInfo.setImageResource(R.drawable.search_recipe);
                    txtInfo.setText("SEARCH FOR A RECIPE");
                    txtInfo.setVisibility(View.VISIBLE);
                    imgInfo.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });
    }
    private void changeStatusBarColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = main.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.orange));
        }
    }
    public SearchFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }
    private void setListView(String name){
        List<Map<String,String>> data = new ArrayList<>();
        try{
            if(MainActivity.connection != null){
                String query = "select recipe_id,recipe_name,recipe_cook_time from [Recipe] where" +
                        " recipe_name like ? order by recipe_name offset 0 rows fetch next 15 rows " +
                        "only";
                PreparedStatement ps = MainActivity.connection.prepareStatement(query);
                ps.setString(1, "%" + name + "%");
                ResultSet rs = ps.executeQuery();
                if(!rs.next()){
                    imgInfo.setVisibility(View.VISIBLE);
                    txtInfo.setVisibility(View.VISIBLE);
                    imgInfo.setImageResource(R.drawable.no_results);
                    txtInfo.setText("NO RECIPES FOUND");
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
                Toast.makeText(main, "Error: Failed to connect to DB", Toast.LENGTH_LONG).show();
            }
        }
        catch(Exception e){
            Toast.makeText(main, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("ERROR", e.getMessage());
        }

        ListAdapter adapter = new ListAdapter(getActivity(), data, ListAdapter.RECIPE_SEARCH, MainActivity.connection);
        recipes.setAdapter(adapter);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If connection is not established, establish it inside the fragment;
        try {
            if (MainActivity.connection.isClosed()) {
                ConnectionDB con = new ConnectionDB();
                MainActivity.connection = con.connect();
            }
        }
        catch(Exception e){
            Log.e("ERROR DB", e.getMessage());
        }

        // Initializations
        main = (MainActivity) getActivity();
        changeStatusBarColor();
        initViews(view);
        setOnClickViews();
    }
}