package com.example.foodapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

public class Login extends AppCompatActivity {
    // Object that represents the connection to the database;
    private Connection connection;
    // Buttons for login and signup;
    private Button signup, login;

    // TextView that represents the area where the error is displayed;
    private TextView txtError;
    // Objects that represent the text boxes of the username and password;
    private EditText passEdit, userEdit;
    // Strings for retrieving username and password from text boxes;
    private String user, pass;
    // Error messages to display respective message;
    private String[] errorMessages = {"USERNAME NOT REGISTERED", "INVALID CREDENTIALS",
            "Error when connecting to the server"};
    public static String encryptPass(String input) {
        try {
            // getInstance() method is called with algorithm SHA-512
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

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
    // We check if the credentials are registered to an account;
    private boolean checkCredentials() {
        boolean ok = false;
        try {
            ConnectionDB db = new ConnectionDB();
            connection = db.connect();

            if (connection != null) {
                ResultSet rs;
                String query = "select account_username from dbo.Account where" +
                        "(account_username = ?  and account_password = ?)";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, user);
                ps.setString(2, encryptPass(pass));
                rs = ps.executeQuery();
                rs.next();
                //If account is found, then it means the user is registered in the DB
                if (rs.getRow() != 0) {
                    ok = true;
                }
                connection.close();
            } else {
                Toast.makeText(this, errorMessages[2], Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }
        return ok;
    }
    // We check if the Nickname field is set in the DB;
    // If the user has no Nickname, it means it is his first time logging in;
    // User is redirected to a first-time activity where he/she will choose it's Nickname;
    private String getId() {
        String id = "";
        try {
            ConnectionDB db = new ConnectionDB();
            connection = db.connect();

            if (connection != null) {
                ResultSet rs;
                String query = "select account_id from dbo.Account" +
                        " where account_username = ?";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, user);
                rs = ps.executeQuery();
                rs.next();
                id = rs.getString(1);
                connection.close();
            } else {
                Toast.makeText(this, errorMessages[2], Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return id;
    }
    private boolean hasNickname() {
        boolean hasNick = true;
        try {
            ConnectionDB db = new ConnectionDB();
            connection = db.connect();

            if (connection != null) {
                ResultSet rs;
                String query = "select account_nickname from dbo.Account" +
                        " where account_username = ?";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, user);
                rs = ps.executeQuery();
                rs.next();

                //If account is found, then it means the user is registered in the DB
                if (rs.getString(1).equals("")) {
                    hasNick = false;
                }

                connection.close();
            } else {
                Toast.makeText(this, errorMessages[2], Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }
        return hasNick;

    }
    // We check if the username is registered into the DB;
    private boolean checkUsername() {
        boolean ok = false;
        try {
            ConnectionDB db = new ConnectionDB();
            connection = db.connect();

            if (connection != null) {
                ResultSet rs;
                String query = "select account_username from dbo.Account" +
                        " where account_username = ?";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, user);
                rs = ps.executeQuery();
                rs.next();

                //If account is found, then it means the user is registered in the DB
                if (rs.getRow() != 0) {
                    ok = true;
                }
                connection.close();
            } else {
                Toast.makeText(this, errorMessages[2], Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }
        return ok;
    }
    private void setOnClickViews() {
        signup.setOnClickListener(view -> {
            Intent activitySignup = new Intent(this, Signup.class);
            startActivity(activitySignup);
        });

        login.setOnClickListener(view -> {
            loginClick();
        });
    }
    private void initViews() {
        signup = findViewById(R.id.btn_signup_login);
        login = findViewById(R.id.btn_login);
        userEdit = findViewById(R.id.box_username_login);
        passEdit = findViewById(R.id.box_password_login);
        txtError = findViewById(R.id.txt_error_login);
    }
    private short isFormValid() {
        user = userEdit.getText().toString();
        pass = passEdit.getText().toString();

        if (checkUsername()) {
            if (checkCredentials()) {
                return -1;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }
    private void setOnChangeText() {
        userEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int aft) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = userEdit.getText().toString();
                if (str.indexOf(' ') != -1) {
                    str = userEdit.getText().toString().replace(" ", "");
                    userEdit.setText(str);
                }
            }
        });

        passEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int aft) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = passEdit.getText().toString();
                if (str.indexOf(' ') != -1) {
                    str = passEdit.getText().toString().replace(" ", "");
                    passEdit.setText(str);
                }
            }
        });

    }
    // Method that describes the functionality of the Login button;
    private void loginClick() {
        short code = isFormValid();

        if (code != -1) {
            txtError.setText(errorMessages[code]);
        } else {
            Intent successLogin;
            String id = getId();
            Bundle bundle = new Bundle();
            bundle.putString("id", id);

            SharedPreferences prefs = getApplicationContext().getSharedPreferences("userPrefs", 0);
            prefs.edit().putString("idUser", id).commit();

            if (hasNickname()) {
                successLogin = new Intent(this, MainActivity.class);
            } else {
                successLogin = new Intent(this, ChooseNickname.class);
            }

            successLogin.putExtras(bundle);
            startActivity(successLogin);
            finish();
        }
    }
    private void checkAlreadyLogin(){
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("userPrefs", 0);
        String idUser = prefs.getString("idUser", "none");
        if(!idUser.equals("none")){
            Intent successLogin;
            Bundle bundle = new Bundle();
            bundle.putString("id", idUser);

            if (hasNickname()) {
                successLogin = new Intent(this, MainActivity.class);
            } else {
                successLogin = new Intent(this, ChooseNickname.class);
            }
            successLogin.putExtras(bundle);
            startActivity(successLogin);
            finish();
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
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.orange));
        setContentView(R.layout.activity_login);

        // We check the shared preferences to see if the user's id is stored in the internal file;
        // In the case it is present, we automatically log in the user and pass the id within a
        // bundle;
        checkAlreadyLogin();

        initViews();

        setOnClickViews();

        setOnChangeText();

    }
}