package com.example.homeserviceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.homeserviceapp.Prevalent.Prevalent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    // Variables
    private EditText phoneEditText,passwordEditText,nameEditText;
    ImageButton signupButton;
    Button loginButton;
    RadioGroup radioGroup;
    RadioButton radioButton;
    private ProgressDialog loadingBar;
    private String parentDbName = "Customers";

    Spinner spinner;
    String service = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_up);

        phoneEditText = (EditText)findViewById(R.id.phone_signup);
        passwordEditText = (EditText)findViewById(R.id.password_signup);
        nameEditText = (EditText)findViewById(R.id.name_signup);
        signupButton =(ImageButton)findViewById(R.id.signup);
        loginButton = (Button) findViewById(R.id.login_signup);
        radioGroup = findViewById(R.id.radio_group_signup);
        spinner = findViewById(R.id.service_list_signup);

        loadingBar = new ProgressDialog(this);

        signupButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                String name = nameEditText.getText().toString().trim();
                String phone = phoneEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (parentDbName.equals("ServiceProviders")) {
                    service = spinner.getSelectedItem().toString();
                }

                if (isInputValid(name, phone, password)) {

                        loadingBar.setTitle("Create Account");
                        loadingBar.setMessage("Loading...");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        ValidatePhoneNumber(name, phone, password);
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void ValidatePhoneNumber(final String name, final String phone, final String password) {

        final DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!(dataSnapshot.child(parentDbName).child(phone).exists())) {

                    // We didn't use User Data Model rather used HashMap
                    HashMap<String, Object> userdataMap = new HashMap<>();
                    userdataMap.put("phone", phone);
                    userdataMap.put("password", password);
                    userdataMap.put("name", name);
                    userdataMap.put("service", service);

                    RootRef.child(parentDbName).child(phone).updateChildren(userdataMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        Toast.makeText(getApplicationContext(), "Congratulations, your account has been created.", Toast.LENGTH_SHORT).show();

                                        // Reset Input Fields
                                        loadingBar.dismiss();
                                        nameEditText.setText("");
                                        phoneEditText.setText("");
                                        passwordEditText.setText("");
                                        service = "";

                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(intent);

                                    } else {
                                        loadingBar.dismiss();
                                        Toast.makeText(getApplicationContext(), "Network Error: Please try again after some time.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {

                    phoneEditText.setError("Phone Number Already Exists");
                    phoneEditText.requestFocus();
                    loadingBar.dismiss();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private boolean isInputValid(String name, String phone, String password) {

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Provide Your Name");
            nameEditText.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Provide Mobile Number");
            phoneEditText.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Provide Password");
            passwordEditText.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(service) && parentDbName.equals("ServiceProviders")) {
            Toast.makeText(this, "Please Select Service You Want to Provide", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void WhoIsThis(View view) {
        int whoIsThis = radioGroup.getCheckedRadioButtonId();

        radioButton = findViewById(whoIsThis);

        String who = radioButton.getText().toString();

        if (who.equals("User")) {
            parentDbName = "Customers";
            spinner.setVisibility(View.INVISIBLE);
        } else {
            parentDbName = "ServiceProviders";
            spinner.setVisibility(View.VISIBLE);
        }
    }

}