package com.example.foodapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Signup extends AppCompatActivity {

    //Connection class for DB connection
    Connection connection;
    private String user, pass, re, email;
    private TextView error;
    private EditText userEdit, passEdit, reEdit, emailEdit;
    private final String[] errorMessages = {"NOT ALL FIELDS COMPLETED",
            "USERNAME MUST BE BETWEEN 8 AND 24 CHARACTERS INCLUSIVE",
            "PASSWORD MUST BE BETWEEN 8 AND 32 CHARACTERS INCLUSIVE","PASSWORDS DO NOT MATCH",
            "ENTER A VALID EMAIL", "USERNAME MUST BE DIFFERENT FROM THE PASSWORD",
            "USERNAME ALREADY IN USE","Error when connecting to the server"};

    private final String successMessage= "ACCOUNT CREATED SUCCESSFULLY!";
    public static String hashPass(String input)
    {
        try {
            // getInstance() method is called with algorithm SHA-512
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger nmbr = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = nmbr.toString(16);

            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    private static boolean isEmailValid(CharSequence e){
        if(TextUtils.isEmpty(e)){
            return false;
        }
        else{
            return Patterns.EMAIL_ADDRESS.matcher(e).matches();
        }
    }
    private int checkUserAvailable(){
        int count = 0;
        try {
            ConnectionDB db = new ConnectionDB();
            Connection con = db.connect();
            if (con != null) {
                ResultSet rs;
                String query = "select account_username from dbo.Account where account_username = ?";
                PreparedStatement ps = con.prepareStatement(query);
                ps.setString(1,user);
                rs = ps.executeQuery();
                rs.next();
                count = rs.getRow();
                con.close();
            }
            else{
                Toast.makeText(this, errorMessages[7], Toast.LENGTH_SHORT).show();
            }
        }
        catch(Exception e){
            Log.e("Error: ", e.getMessage());
        }
        return count;
    }
    private int isFormValid(){

        if(user.isEmpty() || pass.isEmpty() || re.isEmpty() || email.isEmpty()){
            return 0;
        }
        else {
            if (user.length() > 7 && user.length() < 25) {
                if(pass.length() > 7 && pass.length() < 33) {
                    if (pass.equals(re)) {
                        if (isEmailValid(email)) {
                            if(!user.equals(pass)) {
                                if(checkUserAvailable() == 0) {
                                    return -1;
                                }
                                else{
                                    return 6;
                                }
                            }
                            else{
                                return 5;
                            }
                        } else {
                            return 4;
                        }
                    } else {
                        return 3;
                    }
                }
                else{
                    return 2;
                }
            }
            else{
                return 1;
            }
        }
    }
    protected void signupClick(){
        // String values from EditText;
        user = userEdit.getText().toString();
        pass = passEdit.getText().toString();
        re = reEdit.getText().toString();
        email = emailEdit.getText().toString();

        int code = isFormValid();

        if(code != -1) {
            error.setText(errorMessages[code]);
        }
        else {
            error.setText("");
            try {
                ConnectionDB db = new ConnectionDB();
                connection = db.connect();
                if (connection != null) {

                    // We insert the registered account into the database;
                    String query = "insert into dbo.Account values(0, ?, ?, ?, '')";

                    PreparedStatement st = connection.prepareStatement(query);
                    st.setString(1, user);
                    st.setString(2, hashPass(pass));
                    st.setString(3, email);
                    st.execute();

                    connection.close();

                    Intent successSignup = new Intent(this,Login.class);
                    startActivity(successSignup);
                    finish();
                    Toast.makeText(this, successMessage,Toast.LENGTH_LONG).show();

                } else {
                    Log.e("Error: ", "Failed to connect to DB");
                }
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.white));
        setContentView(R.layout.activity_signup);

        // Functionality for the <BACK TO LOGIN> button;
        TextView back = findViewById(R.id.txt_back_signup);
        back.setOnClickListener(view -> {
            Intent activityLogin = new Intent(this,Login.class);
            startActivity(activityLogin);
            finish();
        });

        // TextView for displaying errors;
        error = findViewById(R.id.txt_error_signup);

        // EditText boxes which contain the data the user inserted;
        userEdit = findViewById(R.id.box_username_signup);
        passEdit = findViewById(R.id.box_password_signup);
        reEdit = findViewById(R.id.box_repassword_signup);
        emailEdit = findViewById(R.id.box_email_signup);

        // Functionality for the <SIGN UP> button
        Button btnSign = findViewById(R.id.btn_signup);
        btnSign.setOnClickListener(view-> signupClick());

    }
}