package com.example.foodapp;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecipesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecipesFragment extends Fragment {
    protected Button btnAdd, btnProfile, btnAddMod, btnFavs;
    protected ImageView imgInfo;
    protected TextView txtInfo;
    protected MainActivity main;
    protected ListView cookbooks;
    public static String selectedCookbookId = "";
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    public RecipesFragment() {
        // Required empty public constructor
    }
    public static RecipesFragment newInstance(String param1, String param2) {
        RecipesFragment fragment = new RecipesFragment();
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
            window.setStatusBarColor(getResources().getColor(R.color.white));
        }
    }
    private void clickAddIngMod(){
        Intent goToAddIngMod = new Intent(main, AddIngredientsMod.class);
        startActivity(goToAddIngMod);
    }
    private void clickAdd(){
        PopupMenu popup = new PopupMenu(main, btnAdd);
        popup.getMenuInflater().inflate(R.menu.add_popup_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(menuItem -> {
            if(menuItem.getItemId() == R.id.add_cookbook){
                Intent createCookbook = new Intent(main,CreateCookbook.class);
                startActivity(createCookbook);
                main.finish();
            }
            else{
                Intent createRecipe = new Intent(main,CreateRecipe.class);
                startActivity(createRecipe);
            }
            return true;
        });

        popup.show();
    }
    private void clickFavorites(){
        Intent goToFavs = new Intent(main, CookbookPage.class);
        startActivity(goToFavs);
    }
    private void clickProfile(){
        Intent profile = new Intent(main, Profile.class);
        startActivity(profile);
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recipes, container, false);
    }
    private void setListView(){
        List<Map<String,String>> data = new ArrayList<>();
        try{
            if(MainActivity.connection != null) {
                String query = "select * from [Cookbook] where cb_account_id = ?";
                PreparedStatement ps = MainActivity.connection.prepareStatement(query);
                ps.setString(1, MainActivity.idUser);
                ResultSet rs = ps.executeQuery();
                if(!rs.next()){
                    imgInfo.setVisibility(View.VISIBLE);
                    txtInfo.setVisibility(View.VISIBLE);
                    return;
                }
                else{
                    imgInfo.setVisibility(View.INVISIBLE);
                    txtInfo.setVisibility(View.INVISIBLE);
                }
                do {
                    Map<String, String> map = new HashMap<>();
                    map.put("IdCookbook", rs.getString("cb_id"));
                    map.put("NameCookbook", rs.getString("cb_name"));
                    data.add(map);
                }while(rs.next());
            }
        }
        catch(Exception e){
            Log.e("ERROR DB:", e.getMessage());
        }

        ListAdapter adapter = new ListAdapter(getActivity(), data, ListAdapter.COOKBOOKS, MainActivity.connection);
        cookbooks.setAdapter(adapter);
    }
    protected void initViews(View view){
        btnAdd = view.findViewById(R.id.fragment_recipes_btn_add);
        btnProfile = view.findViewById(R.id.fragment_recipes_btn_profile);
        btnAddMod = view.findViewById(R.id.fragment_recipes_btn_add_ingredients_mod);
        btnFavs = view.findViewById(R.id.fragment_recipes_btn_favorites);
        cookbooks = view.findViewById(R.id.fragment_recipes_cookbooks_list);
        txtInfo = view.findViewById(R.id.fragment_recipes_txt_info);
        imgInfo = view.findViewById(R.id.fragment_recipes_img_info);
        setListView();
    }
    protected void setOnClickViews(){
        btnAdd.setOnClickListener(click->clickAdd());
        btnProfile.setOnClickListener(click->clickProfile());
        btnAddMod.setOnClickListener(click->clickAddIngMod());
        btnFavs.setOnClickListener(click->clickFavorites());
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

        initViews(view);
        main = (MainActivity) getActivity();
        setOnClickViews();
        changeStatusBarColor();
    }
}