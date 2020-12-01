package com.example.homeserviceapp.Customer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.homeserviceapp.LoginActivity;
import com.example.homeserviceapp.Prevalent.Prevalent;
import com.example.homeserviceapp.R;
import com.example.homeserviceapp.ServiceProvider.ServiceProviderHomeActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class CustomerHomeActivity extends AppCompatActivity {

    // Variables
    private static final int MY_PERMISSIONS_REQUEST = 1;

    CardView tvService, carWash, acService, fridgeService, houseCleaning, electricService;

    private DatabaseReference userRef;


    CircleImageView profileImageView;
    TextView profileChangeTextBtn,  userName;

    Uri imageUri;
    String myUrl = "";
    StorageTask uploadTask;
    StorageReference storageProfilePictureRef;

    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_customer_home);

        storageProfilePictureRef = FirebaseStorage.getInstance().getReference().child("Profile pictures");

        // Hooks
        tvService = findViewById(R.id.tv_service_customer_home);
        carWash = findViewById(R.id.car_wash_customer_home);
        acService = findViewById(R.id.ac_service_customer_home);
        fridgeService = findViewById(R.id.fridge_service_customer_home);
        houseCleaning = findViewById(R.id.house_cleaning_customer_home);
        electricService = findViewById(R.id.electric_service_customer_home);

        profileChangeTextBtn = findViewById(R.id.change_profile_image_customer);
        profileImageView = findViewById(R.id.customer_profile_image);
        userName = findViewById(R.id.customer_name);

        userName.setText(Prevalent.currentOnlineUser.getName());

        userPhotoDisplay();

        profileChangeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseProfilePhoto();
            }
        });

        tvService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("serviceId", "TV Service");
                startActivity(intent);
            }
        });

        carWash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("serviceId", "Car Wash");
                startActivity(intent);
            }
        });

        acService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("serviceId", "AC Service");
                startActivity(intent);
            }
        });

        fridgeService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("serviceId", "Fridge Service");
                startActivity(intent);
            }
        });

        houseCleaning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("serviceId", "House Cleaning");
                startActivity(intent);
            }
        });

        electricService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("serviceId", "Electric Service");
                startActivity(intent);
            }
        });
    }

    private void userPhotoDisplay() {

        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("Customers").child(Prevalent.currentOnlineUser.getPhone());

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    if (dataSnapshot.child("image").exists())
                    {
                        String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();

                        Picasso.get().load(image).into(profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void chooseProfilePhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1  &&  resultCode == RESULT_OK  &&  data!=null && data.getData() != null) {

            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
            uploadPhoto();
        } else {
            Toast.makeText(this, "Error, Try Again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPhoto() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Upload Profile Photo");
        progressDialog.setMessage("Uploading...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        if (imageUri != null)
        {
            final StorageReference fileRef = storageProfilePictureRef.child(Prevalent.currentOnlineUser.getPhone() + ".jpg");

            uploadTask = fileRef.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUrl = task.getResult();
                        myUrl = downloadUrl.toString();

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Customers");

                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("image", myUrl);

                        ref.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);

                        progressDialog.dismiss();

                        startActivity(new Intent(getApplicationContext(), CustomerHomeActivity.class));
                        Toast.makeText(getApplicationContext(), "Photo Uploaded Successfully.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Error.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "image is not selected.", Toast.LENGTH_SHORT).show();
        }

    }


    public void LogoutUser(View view) {
        Paper.book().destroy();

        SharedPreferences sharedPreferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("remember", "false");
        editor.apply();

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        i = 0;
    }

    @Override
    public void onBackPressed() {
        i++;
        if (i == 1) {
            Toast.makeText(CustomerHomeActivity.this, "Press back once more to exit.", Toast.LENGTH_SHORT).show();
        } else if(i>1) {
            finish();
            super.onBackPressed();
        }
    }
}