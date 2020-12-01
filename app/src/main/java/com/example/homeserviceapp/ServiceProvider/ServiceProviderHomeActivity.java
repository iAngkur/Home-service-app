package com.example.homeserviceapp.ServiceProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.homeserviceapp.Customer.CustomerHomeActivity;
import com.example.homeserviceapp.LoginActivity;
import com.example.homeserviceapp.Prevalent.Prevalent;
import com.example.homeserviceapp.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class ServiceProviderHomeActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentUserLocationMarker;

    DatabaseReference serviceProvidersRef;
    GeoFire geoFire;

    private static final int REQUEST_USER_LOCATION_CODE = 99;

    String currentUserService, currentUserPhone;


    CircleImageView profileImageView;
    TextView profileChangeTextBtn, userName, userService;
    RatingBar userRating;

    Uri imageUri;
    String myUrl = "";
    StorageTask uploadTask;
    StorageReference storageProfilePictureRef;

    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_service_provider_home);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkUserLocationPermission();
        }

        currentUserService = Prevalent.currentOnlineUser.getService();
        currentUserPhone = Prevalent.currentOnlineUser.getPhone();

        storageProfilePictureRef = FirebaseStorage.getInstance().getReference().child("Profile pictures");


        profileChangeTextBtn = findViewById(R.id.change_profile_image);
        profileImageView = findViewById(R.id.user_profile_image);
        userName = findViewById(R.id.user_name);
        userRating = findViewById(R.id.user_rating);
        userService = findViewById(R.id.user_services);

        userName.setText(Prevalent.currentOnlineUser.getName());
        userService.setText("Service: " + currentUserService);

        userPhotoDisplay();

        profileChangeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseProfilePhoto();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_service_provider);
        mapFragment.getMapAsync(this);
    }


    private void userPhotoDisplay() {

        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("ServiceProviders").child(currentUserPhone);

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("image").exists()) {
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

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {

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

        if (imageUri != null) {
            final StorageReference fileRef = storageProfilePictureRef.child(currentUserPhone + ".jpg");

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

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("ServiceProviders");

                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("image", myUrl);

                        ref.child(currentUserPhone).updateChildren(userMap);

                        progressDialog.dismiss();

                        startActivity(new Intent(getApplicationContext(), ServiceProviderHomeActivity.class));
                        Toast.makeText(getApplicationContext(), "Photo Uploaded Successfully.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Error.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "image is not selected.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            buildGoogleApiClient();

            mMap.setMyLocationEnabled(true);
        }
    }

    public boolean checkUserLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]
                        {
                                Manifest.permission.ACCESS_FINE_LOCATION
                        }, REQUEST_USER_LOCATION_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]
                        {
                                Manifest.permission.ACCESS_FINE_LOCATION
                        }, REQUEST_USER_LOCATION_CODE);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_USER_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (googleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1100);
        locationRequest.setFastestInterval(1100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

//        serviceProvidersRef = FirebaseDatabase.getInstance().getReference().child("AvailableServiceProviders").child(currentUserService);
//        geoFire = new GeoFire(serviceProvidersRef);
//        geoFire.setLocation(currentUserPhone, new GeoLocation(location.getLatitude(), location.getLongitude()));

        StoreToAvailableTable();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("You");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        currentUserLocationMarker = mMap.addMarker(markerOptions);
        currentUserLocationMarker.showInfoWindow();

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(14));

        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    private void StoreToAvailableTable() {
        serviceProvidersRef = FirebaseDatabase.getInstance().getReference().child("AvailableServiceProviders").child(currentUserService);

        String lat = String.valueOf(lastLocation.getLatitude());
        String lang = String.valueOf(lastLocation.getLongitude());

        final HashMap<String, Object> serviceProviders = new HashMap<>();
        serviceProviders.put("pid", currentUserPhone);
        serviceProviders.put("lat", lat);
        serviceProviders.put("lang", lang);

        serviceProvidersRef.child(currentUserPhone).updateChildren(serviceProviders)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                        } else {
                            Toast.makeText(getApplicationContext(), "Network Error.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    public void AddSkills(View view) {

    }

    public void SeeAllReviews(View view) {
        startActivity(new Intent(getApplicationContext(), AllReviewsActivity.class));
    }

    public void LogoutUser(View view) {

//        serviceProvidersRef = FirebaseDatabase.getInstance().getReference().child("AvailableServiceProviders").child(currentUserService);
//        geoFire = new GeoFire(serviceProvidersRef);
//        geoFire.removeLocation(currentUserPhone);


        SharedPreferences sharedPreferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("remember", "false");
        editor.apply();

        Paper.book().destroy();

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
            Toast.makeText(ServiceProviderHomeActivity.this, "Press back once more to exit.", Toast.LENGTH_SHORT).show();
        } else if(i>1) {
            finish();
            super.onBackPressed();
        }
    }
}