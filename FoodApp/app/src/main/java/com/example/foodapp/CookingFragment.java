package com.example.foodapp;

import android.content.Intent;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CookingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CookingFragment extends Fragment {
    protected Connection connection;
    protected Button btnSearchByIngredients, btnSearchByDiet;
    public static ListView listRecipes;
    public static TextView txtCook;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    static protected MainActivity main;
    private String mParam1;
    private String mParam2;
    public CookingFragment() {
        // Required empty public constructor
    }
    public static CookingFragment newInstance(String param1, String param2) {
        CookingFragment fragment = new CookingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    private void changeStatusBarColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = main.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.orange));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.setStatusBarContrastEnforced(true);
            }
        }
    }
    private void initViews(View view){
        btnSearchByIngredients = view.findViewById(R.id.cooking_fragment_btn_search_by_ingredients);
        btnSearchByDiet = view.findViewById(R.id.cooking_fragment_btn_search_by_diets);
        listRecipes = view.findViewById(R.id.cooking_fragment_recipes_list);
        txtCook = view.findViewById(R.id.cooking_fragment_txt);
    }
    private void setOnClickViews(){
        btnSearchByIngredients.setOnClickListener(view -> clickSearchIngredients());
        btnSearchByDiet.setOnClickListener(view -> clickSearchDiet());
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_cooking, container, false);
    }
    private void clickSearchIngredients(){
        Intent goToSearchIngredients = new Intent(main, SearchByIngredients.class);
        startActivity(goToSearchIngredients);
    }
    private void clickSearchDiet(){
        Intent goToSearchDiet = new Intent(main, SearchByDiet.class);
        startActivity(goToSearchDiet);
    }
    public static void initListRecipes(List<Map<String,String>> data){
        ListAdapter adapter = new ListAdapter(main, data, ListAdapter.RECIPE_SEARCH, MainActivity.connection);
        listRecipes.setAdapter(adapter);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If connection is not established, establish it inside the fragment;
        // If connection is not established, establish it inside the fragment;
        connection = MainActivity.connection;
        try {
            if (connection.isClosed()) {
                ConnectionDB con = new ConnectionDB();
                connection = con.connect();
            }
        }
        catch(Exception e){
            Log.e("ERROR DB", e.getMessage());
        }

        // Inflate the layout for this fragment
        main = (MainActivity) getActivity();
        initViews(view);
        setOnClickViews();
        changeStatusBarColor();
    }
}