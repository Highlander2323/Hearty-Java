package com.example.foodapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Profile extends AppCompatActivity {
    protected Button btnBack, btnLogout, btnSave;
    public static boolean isActive = false;
    protected EditText boxName;
    protected String idUser = MainActivity.idUser, nameUser;
    protected LinearLayout myRecipesLayout;
    protected TextView myRecipesNumber;
    protected String []success = {"Name changed successfully"};
    protected String [] nameError = {"NAME MUST HAVE AT LEAST 4 CHARACTERS!",
            "NAME CAN'T BE LONGER THAN 24 CHARACTERS!"};
    @Override
    public void onBackPressed(){
        finish();
        isActive = false;
    }
    private void logout(){
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        idUser = "";
        Intent login = new Intent(this, Login.class);
        startActivity(login);
        finish();
    }
    //Functionality for 'Are you sure you want to quit?' buttons
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE: {
                    logout();
                    break;
                }
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };
    private void onClickBtnLogout(Context context){
        ButtonChecks.logoutCheck(context, dialogClickListener);
    }
    private void onClickBtnBack(){
        finish();
        isActive = false;
    }
    private boolean checkName(){
        String name = boxName.getText().toString();
        if(name.length() < 4){
            boxName.setError(nameError[0]);
            return false;
        }
        else if(name.length() > 24){
            boxName.setError(nameError[1]);
            return false;
        }
        return true;
    }
    private void onClickBtnSave(){
        if(!checkName()){
            return;
        }
        try{
            String query = "update dbo.Account set account_nickname = ? where account_id = ?";
            PreparedStatement ps = MainActivity.connection.prepareStatement(query);
            ps.setString(1, nameUser);
            ps.setString(2, idUser);
            ps.execute();
        }
        catch(Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        btnSave.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#66000000")));
        btnSave.setEnabled(false);
        Toast.makeText(this, success[0], Toast.LENGTH_SHORT).show();
        boxName.onEditorAction(EditorInfo.IME_ACTION_DONE);
        boxName.clearFocus();
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
                checkNameChange();
            }
        });
    }
    private void checkNameChange(){
        String newName = boxName.getText().toString();
        if(!newName.equals(nameUser)){
            btnSave.setEnabled(true);
            btnSave.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.orange)));
        }
        nameUser = newName;
    }
    private void onClickMyRecipes(){
        Intent goToMyRecipes = new Intent(this, CookbookPage.class);
        startActivity(goToMyRecipes);
        finish();
    }
    private void setOnClickViews(){
        myRecipesLayout.setOnClickListener(view -> onClickMyRecipes());
        btnBack.setOnClickListener(btn->onClickBtnBack());
        btnSave.setOnClickListener(btn->onClickBtnSave());
        btnLogout.setOnClickListener(btn->onClickBtnLogout(this));
        boxName.setOnFocusChangeListener((view, b) -> checkNameChange());
        onTextChange(boxName);
    }
    private void initViews(){
        myRecipesLayout = findViewById(R.id.profile_my_recipes_layout);
        myRecipesNumber = findViewById(R.id.profile_my_recipes_number);
        btnBack = findViewById(R.id.btn_profile_back);
        btnLogout = findViewById(R.id.btn_profile_logout);
        btnSave = findViewById(R.id.btn_profile_save);
        boxName = findViewById(R.id.btn_profile_name);
        String recipesNr = "";
        try{
            String query = "select account_nickname from dbo.Account where account_id = ?";
            PreparedStatement ps = MainActivity.connection.prepareStatement(query);
            ResultSet rs;
            ps.setString(1, idUser);
            rs = ps.executeQuery();
            rs.next();

            //If account is found, then it initializes the nameUser variable with the account's
            // nickname
            if (rs.getRow() != 0) {
                nameUser = rs.getString(1);
            }

            query = "select count(*) from Recipe where recipe_account_id = ?";
            ps = MainActivity.connection.prepareStatement(query);
            ps.setString(1, MainActivity.idUser);
            rs = ps.executeQuery();
            rs.next();

            if (rs.getRow() != 0) {
                recipesNr = rs.getString(1);
            }

        }
        catch(Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        boxName.setText(nameUser);
        myRecipesNumber.setText(recipesNr);
    }
    private void changeStatusBarColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.black));
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        changeStatusBarColor();
        initViews();
        setOnClickViews();
        isActive = true;
    }
}