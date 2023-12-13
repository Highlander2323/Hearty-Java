package com.example.foodapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class CreateCookbook extends AppCompatActivity {
    protected String idUser = MainActivity.idUser;
    private EditText boxTitle, boxDescription;
    private Button btnCreate, btnCancel;
    private TextView txtNameCharLimit, txtDescriptionCharLimit;
    private boolean hasName(){
        if(boxTitle.getText().toString().equals("")){
            return false;
        }
        return true;
    }
    private boolean isChanged(){
        SharedPreferences prefs = getSharedPreferences("cookbookDetails", Context.MODE_PRIVATE);
        return (!boxTitle.getText().toString().equals(prefs.getString("cookbookTitle", "")) ||
        !boxDescription.getText().toString().equals(prefs.getString("cookbookDescription", "")));
    }
    private void initViews(){
        btnCreate = findViewById(R.id.btn_cookbook_create);
        btnCancel = findViewById(R.id.btn_cookbook_cancel);
        boxTitle = findViewById(R.id.box_cookbook_name);
        boxDescription = findViewById(R.id.box_cookbook_description);
        txtNameCharLimit = findViewById(R.id.txt_cookbook_name_char_limit);
        txtDescriptionCharLimit = findViewById(R.id.txt_cookbook_description_char_limit);

        if(!RecipesFragment.selectedCookbookId.equals("")){
            SharedPreferences prefs = getSharedPreferences("cookbookDetails", Context.MODE_PRIVATE);
            boxTitle.setText(prefs.getString("cookbookTitle", ""));
            boxDescription.setText(prefs.getString("cookbookDescription", ""));
        }
    }
    private void checkFields(){
        if(RecipesFragment.selectedCookbookId.equals("")) {
            if (hasName()) {
                btnCreate.setEnabled(true);
                btnCreate.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                        this, R.color.orange)));
                return;
            }
        }
        else{
            if(isChanged()){
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
    private void setOnTextChanged(){
        boxTitle.addTextChangedListener(new TextWatcher()
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

        boxDescription.addTextChangedListener(new TextWatcher()
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
                if(s.toString().length() > 135) {
                    txtDescriptionCharLimit.setText(s.toString().length() + "/150");
                }
                else{
                    txtDescriptionCharLimit.setText("");
                }
            }
        });
    }
    protected void setOnClickViews(){
        onTextChange(boxTitle);
        btnCancel.setOnClickListener(view->onClickBtnCancel());
        btnCreate.setOnClickListener(view->onClickBtnCreate());

        if(!RecipesFragment.selectedCookbookId.equals("")){
            onTextChange(boxDescription);
        }
    }
    protected void onClickBtnCreate(){
        if(!RecipesFragment.selectedCookbookId.equals("")){
            String query = "update dbo.Cookbook set cb_name = ?, cb_description = ? where cb_id = ?";
            try {
                PreparedStatement ps = MainActivity.connection.prepareStatement(query);
                ps.setString(1, boxTitle.getText().toString());
                ps.setString(2, boxDescription.getText().toString());
                ps.setString(3, RecipesFragment.selectedCookbookId);
                ps.execute();
            }
            catch(Exception e){
                Log.e("DB Error:", e.getMessage());
                Toast.makeText(this, "PROBLEM!!!", Toast.LENGTH_LONG).show();
            }

        }

        else {
            try {
                ConnectionDB con = new ConnectionDB();
                Connection connection = con.connect();
                String query = "insert into dbo.Cookbook(cb_name, cb_description," +
                        " cb_account_id) values(?,?,?)";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, boxTitle.getText().toString());
                ps.setString(2, boxDescription.getText().toString());
                ps.setString(3, idUser);
                ps.execute();
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        goBack();
    }
    private void goBack(){
        if(!RecipesFragment.selectedCookbookId.equals("")){
            SharedPreferences prefs = getSharedPreferences("cookbookDetails", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
            Intent goBack = new Intent(this, CookbookPage.class);
            startActivity(goBack);
            finish();
        }
        else {
            Intent goBack = new Intent(this, MainActivity.class);
            startActivity(goBack);
            finish();
        }
    }
    protected void onClickBtnCancel(){
        goBack();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_cookbook);

        // Initialize all views by id;
        initViews();

        setOnClickViews();

        setOnTextChanged();
    }
}