package com.example.foodapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ChooseNickname extends AppCompatActivity {

    Connection connection;
    TextView title;
    EditText boxNickname;
    Button btnContinue;
    String nickname;
    String [] titleTextError = {"NAME MUST HAVE AT LEAST 4 CHARACTERS!",
            "NAME CAN'T BE LONGER THAN 24 CHARACTERS!"};

    private void clickContinue(){
        ConnectionDB db = new ConnectionDB();
        connection = db.connect();

        nickname = boxNickname.getText().toString();
        Bundle bundle = getIntent().getExtras();
        String id = bundle.getString("id");

        if(nickname.length() < 4){
            title.setTextColor(getColor(R.color.red));
            title.setText(titleTextError[0]);
            return;
        }
        else if(nickname.length() > 24){
            title.setTextColor(getColor(R.color.red));
            title.setText(titleTextError[1]);
            return;
        }

        try{
            String query = "update dbo.Account set account_nickname = ? where account_id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1,nickname);
            ps.setString(2,id);
            ps.execute();
            connection.close();
            Intent success = new Intent(this, MainActivity.class);
            bundle = new Bundle();
            bundle.putString("id", id);
            success.putExtras(bundle);
            startActivity(success);
        }
        catch(Exception e){
            Log.e("Error: ", e.getMessage());
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_nickname);
        boxNickname = findViewById(R.id.box_nickname);
        btnContinue = findViewById(R.id.btn_nickname_continue);
        title = findViewById(R.id.txt_nickname_title);
        btnContinue.setOnClickListener(view->clickContinue());

    }
}