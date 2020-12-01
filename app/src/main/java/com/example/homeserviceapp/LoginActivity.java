package com.example.homeserviceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.homeserviceapp.Customer.CustomerHomeActivity;
import com.example.homeserviceapp.Model.Users;
import com.example.homeserviceapp.Prevalent.Prevalent;
import com.example.homeserviceapp.ServiceProvider.ServiceProviderHomeActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {

    // Variables
    private EditText phoneEditText, passwordEditText;
    private CheckBox checkBox;
    private ImageButton loginButton;
    Button signupButton;

    RadioGroup radioGroup;
    RadioButton radioButton;

    ProgressDialog loadingBar;
    String parentDbName = "";

    String phone, password, who;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        // Hooks
        phoneEditText = (EditText) findViewById(R.id.phone_login);
        passwordEditText = (EditText) findViewById(R.id.password_login);
        checkBox = (CheckBox) findViewById(R.id.checkbox_login);
        loginButton = (ImageButton) findViewById(R.id.login);
        signupButton = (Button) findViewById(R.id.signup_login);
        radioGroup = findViewById(R.id.radio_group_login);

        Paper.init(this);

        loadingBar = new ProgressDialog(this);

        sharedPreferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        String check = sharedPreferences.getString("remember", "");

        if (check.equals("true")) {

            parentDbName = Paper.book().read("WhoIsThis");

            if (parentDbName.equals("ServiceProviders")) {
                Toast.makeText(LoginActivity.this, "Welcome, Logged in Successfully...", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();

                Intent intent = new Intent(LoginActivity.this, ServiceProviderHomeActivity.class);
                startActivity(intent);
                finish();
            } else if (parentDbName.equals("Customers")) {
                Toast.makeText(LoginActivity.this, "Welcome, Logged in Successfully...", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();

                Intent intent = new Intent(LoginActivity.this, CustomerHomeActivity.class);
                startActivity(intent);
                finish();
            }

        }

        loginButton.setClickable(false);
        checkBox.setClickable(false);

        phone = phoneEditText.getText().toString().trim();
        password = passwordEditText.getText().toString().trim();

        if (isValidInput()) {
            loginButton.setClickable(true);
            checkBox.setClickable(true);
        } else {
            loginButton.setClickable(false);
            checkBox.setClickable(false);
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowAccessToLogin();
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(i);
            }
        });

    }

    public boolean isValidInput() {
        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Please Write Phone Number");
            phoneEditText.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Please Enter Password");
            passwordEditText.requestFocus();
            return false;
        }
        return true;
    }

    private void AllowAccessToLogin() {
        // Remembering the user
        if (checkBox.isChecked()) {

            sharedPreferences = getSharedPreferences("checkbox", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("remember", "true");
            editor.apply();

            Paper.book().write(Prevalent.UserPhoneKey, phone);
            Paper.book().write(Prevalent.UserPasswordKey, password);
            Paper.book().write(Prevalent.Who, parentDbName);
            Paper.book().write("WhoIsThis", parentDbName);
        } else {
            sharedPreferences = getSharedPreferences("checkbox", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("remember", "false");
            editor.apply();
        }

        final DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(parentDbName).child(phone).exists()) {

                    Users usersData = dataSnapshot.child(parentDbName).child(phone).getValue(Users.class);

                    if (usersData.getPhone().equals(phone)) {
                        if (usersData.getPassword().equals(password)) {
                            if (parentDbName.equals("ServiceProviders")) {
                                Toast.makeText(LoginActivity.this, "Welcome Admin, Logged in Successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent intent = new Intent(LoginActivity.this, ServiceProviderHomeActivity.class);
                                Prevalent.currentOnlineUser = usersData;
                                startActivity(intent);
                                finish();
                            } else if (parentDbName.equals("Customers")) {
                                Toast.makeText(LoginActivity.this, "Logged in Successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent intent = new Intent(LoginActivity.this, CustomerHomeActivity.class);
                                Prevalent.currentOnlineUser = usersData;
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            loadingBar.dismiss();
                            passwordEditText.setError("Incorrect Password");
                            passwordEditText.requestFocus();
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Account doesn't exist.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void WhoIsThis(View view) {
        int whoIsThis = radioGroup.getCheckedRadioButtonId();

        radioButton = findViewById(whoIsThis);

        who = radioButton.getText().toString();

        if (who.equals("User")) {
            parentDbName = "Customers";
        } else {
            parentDbName = "ServiceProviders";
        }
    }
}