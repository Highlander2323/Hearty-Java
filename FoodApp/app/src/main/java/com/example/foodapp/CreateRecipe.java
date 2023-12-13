package com.example.foodapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class CreateRecipe extends AppCompatActivity {

    private Button btnCancel, btnCreate, btnIngredients;
    protected String idUser = MainActivity.idUser;
    private Connection connection;
    private static final String[] servingsChoices = {"1", "2", "3", "4", "5",
            "6", "7", "8"};
    private static final String[] timesHours = {"00", "01", "02", "03", "04", "05", "06", "07",
            "08", "09", "10", "11", "12"};
    private static final String[] timesMinutes = {"00", "05", "10", "15", "20", "25", "30", "35",
            "40", "45", "50", "55"};
    public static List<Map<String, String>> idIngredients = new ArrayList<>();
    private EditText boxName, boxDirections;
    private TextView txtNameCharLimit, txtDirectionsCharLimit;
    private AutoCompleteTextView boxPreph, boxPrepm, boxCookh, boxCookm, boxServings;
    protected void initViews() {
        // Buttons
        btnCreate = findViewById(R.id.btn_recipe_create);
        btnCancel = findViewById(R.id.btn_recipe_cancel);
        btnIngredients = findViewById(R.id.btn_recipe_ingredients);
        // EditTexts
        boxName = findViewById(R.id.box_recipe_name);
        boxDirections = findViewById(R.id.box_recipe_directions);
        // AutoCompleteTextViews
        boxPreph = findViewById(R.id.box_recipe_preptimeh);
        boxPrepm = findViewById(R.id.box_recipe_preptimem);
        boxCookh = findViewById(R.id.box_recipe_cooktimeh);
        boxCookm = findViewById(R.id.box_recipe_cooktimem);
        boxServings = findViewById(R.id.box_recipe_servings);
        // TextViews
        txtDirectionsCharLimit = findViewById(R.id.txt_recipe_directions_char_limit);
        txtNameCharLimit = findViewById(R.id.txt_recipe_name_char_limit);
        // Remember fields if they have been initialized before
        SharedPreferences prefs = getSharedPreferences("recipeDetails", 0);
        if(prefs.contains("recipeName")){
            boxName.setText(prefs.getString("recipeName", ""));
            boxDirections.setText(prefs.getString("recipeDirections", ""));
            boxPreph.setText(prefs.getString("prepH", "00"));
            boxPrepm.setText(prefs.getString("prepM", "00"));
            boxCookh.setText(prefs.getString("cookH", "00"));
            boxCookm.setText(prefs.getString("cookM", "00"));
            boxServings.setText(prefs.getString("servings", "0"));
        }
    }
    protected void initBoxesNull() {
        btnIngredients.setText(btnIngredients.getText().toString() + idIngredients.size());
        // We initialize AutoCompleteTextViews to null so that user can't use keyboard for them
        boxPreph.setInputType(InputType.TYPE_NULL);
        boxPrepm.setInputType(InputType.TYPE_NULL);
        boxCookh.setInputType(InputType.TYPE_NULL);
        boxCookm.setInputType(InputType.TYPE_NULL);
        boxServings.setInputType(InputType.TYPE_NULL);

    }
    private boolean hasName(){
        return !boxName.getText().toString().equals("");

    }
    private void setOnTextChanged(){
        boxName.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int aft)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                // this will show characters remaining
                if(s.toString().length() > 35) {
                    txtNameCharLimit.setText(s.toString().length() + "/50");
                }
                else{
                    txtNameCharLimit.setText("");
                }
            }
        });


        boxDirections.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int aft)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                // this will show characters remaining
                if(s.toString().length() > 1350) {
                    txtDirectionsCharLimit.setText(s.toString().length() + "/1500");
                }
                else{
                    txtDirectionsCharLimit.setText("");
                }
            }
        });
    }
    private boolean hasDirections(){
        if(boxDirections.getText().toString().equals("")){
            return false;
        }
        return true;
    }
    //Checks if fields meet the requirements to unlock create recipe button.
    private void checkFields(){
        if(hasName()) {
            if (hasDirections())
            if(idIngredients.size() >= 3){
                btnCreate.setEnabled(true);
                btnCreate.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                        this, R.color.orange)));
                return;
            }
        }
        btnCreate.setEnabled(false);
        btnCreate.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#66000000")));
    }
    private void onTextChange(EditText box){
        box.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                checkFields();
            }
        });
    }
    protected void onClickBtnCreate(){
        try{
            ConnectionDB db = new ConnectionDB();
            connection = db.connect();

            int preptime = Integer.parseInt(boxPreph.getText().toString()) * 60
                    + Integer.parseInt(boxPrepm.getText().toString());
            int cooktime = Integer.parseInt(boxCookh.getText().toString()) * 60 +
                    Integer.parseInt(boxCookm.getText().toString());

            String query = "insert into dbo.Recipe(recipe_name, recipe_directions," +
                    " recipe_prep_time, recipe_cook_time, recipe_servings," +
                    " recipe_account_id) values(?,?,?,?,?,?)";
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1,boxName.getText().toString());
            ps.setString(2,boxDirections.getText().toString());
            ps.setString(3,Integer.toString(preptime));
            ps.setString(4,Integer.toString(cooktime));
            ps.setString(5,boxServings.getText().toString());
            ps.setString(6, idUser);
            ps.execute();

            query = "select IDENT_CURRENT(?)";
            ps = connection.prepareStatement(query);
            ps.setString(1, "Recipe");
            ResultSet rs = ps.executeQuery();
            rs.next();
            String recipeId = rs.getString(1);

            for(int i = 0; i < idIngredients.size(); i++){
                query = "insert into dbo.RecipeIngredient values(?,?,?)";
                ps = connection.prepareStatement(query);
                ps.setString(1, recipeId);
                ps.setString(2, idIngredients.get(i).get("Id"));
                ps.setString(3,idIngredients.get(i).get("Amount"));
                ps.execute();
            }
            CheckDiets check = new CheckDiets(idIngredients, connection, Integer.parseInt(boxServings.getText().toString()), recipeId);
            check.calculateValues();
            check.checkDiets();

            connection.close();

            SharedPreferences prefs = getApplicationContext().getSharedPreferences("recipeDetails", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();

            // Clear the added ingredients
            idIngredients.clear();
            Toast.makeText(this, "Recipe created successfully!", Toast.LENGTH_SHORT).show();
            goBack();

        }
        catch(Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("AAAUUUGH", e.getMessage());
        }
    }
    // Are you sure you want to cancel dialog box initializing
    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
        switch (which){
            case DialogInterface.BUTTON_POSITIVE: {
                SharedPreferences prefs = getApplicationContext().getSharedPreferences("recipeDetails", Context.MODE_PRIVATE);
                prefs.edit().clear().apply();
                idIngredients.clear();
                goBack();
                break;
            }
            case DialogInterface.BUTTON_NEGATIVE:
                break;
        }
    };
    protected void goBack(){
        finish();
    }
    protected void onClickBtnCancel(Context context){
        ButtonChecks.recipeCancelCheck(context, dialogClickListener);
    }
    protected void onClickBtnIngredients(){
        Intent goIngredients = new Intent(this, AddIngredients.class);
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("recipeDetails", 0);
        prefs.edit().putString("recipeName", boxName.getText().toString()).commit();
        prefs.edit().putString("recipeDirections", boxDirections.getText().toString()).commit();
        prefs.edit().putString("prepH", boxPreph.getText().toString()).commit();
        prefs.edit().putString("prepM", boxPrepm.getText().toString()).commit();
        prefs.edit().putString("cookH", boxCookh.getText().toString()).commit();
        prefs.edit().putString("cookM", boxCookm.getText().toString()).commit();
        prefs.edit().putString("servings", boxServings.getText().toString()).commit();
        startActivity(goIngredients);
        finish();
    }
    protected void initAdapters(){
        ArrayAdapter<String> adapterHours = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, timesHours);
        ArrayAdapter<String> adapterMinutes = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, timesMinutes);
        ArrayAdapter<String> adapterServings = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, servingsChoices);

        boxPreph.setAdapter(adapterHours);
        boxPrepm.setAdapter(adapterMinutes);
        boxCookh.setAdapter(adapterHours);
        boxCookm.setAdapter(adapterMinutes);
        boxServings.setAdapter(adapterServings);
    }
    // For each AutoCompleteTextView, we set a listener for OnFocusChange and OnClick, because
    // we want to display the choices everytime the user taps on the boxes; additionally,
    // we set a OnClickListener for buttons, so we can run the proper function for each button;
    protected void setOnClickViews(){
        boxPreph.setOnFocusChangeListener((view, b) -> boxPreph.showDropDown());
        boxPreph.setOnClickListener(view -> boxPreph.showDropDown());

        boxPrepm.setOnFocusChangeListener((view, b) -> boxPrepm.showDropDown());
        boxPrepm.setOnClickListener(view -> boxPrepm.showDropDown());

        boxCookh.setOnFocusChangeListener((view, b) -> boxCookh.showDropDown());
        boxCookh.setOnClickListener(view -> boxCookh.showDropDown());

        boxCookm.setOnFocusChangeListener((view, b) -> boxCookm.showDropDown());
        boxCookm.setOnClickListener(view -> boxCookm.showDropDown());

        boxServings.setOnFocusChangeListener((view, b) -> boxServings.showDropDown());
        boxServings.setOnClickListener(view -> boxServings.showDropDown());

        onTextChange(boxName);
        onTextChange(boxDirections);

        btnCancel.setOnClickListener(view->onClickBtnCancel(this));
        btnCreate.setOnClickListener(view->onClickBtnCreate());
        btnIngredients.setOnClickListener(view->onClickBtnIngredients());

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        // Initialize all views by id;
        initViews();

        // Initialize boxes with Null input type;
        initBoxesNull();

        // Initialize adapters, so each field shows hours, minutes and servings respectively
        initAdapters();

        // Because the hours, minutes and servings boxes don't allow input, we need to
        // automatically show all suggestions on clicking the field; Also set on clicks for the
        // buttons
        setOnClickViews();

        // Set OnTextChangedListeners for name and description, so the user cannot enter
        // more than the set limit of characters; also it displays a counter whenever
        // the user is close to the limit;
        setOnTextChanged();

        checkFields();
    }
}