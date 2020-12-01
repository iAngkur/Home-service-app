package com.example.homeserviceapp.Customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.homeserviceapp.LoginActivity;
import com.example.homeserviceapp.Model.ReviewModel;
import com.example.homeserviceapp.Model.Users;
import com.example.homeserviceapp.Prevalent.Prevalent;
import com.example.homeserviceapp.R;
import com.example.homeserviceapp.ViewHolder.ReviewViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class WriteReviewActivity extends AppCompatActivity {

    String service, spID, spName, spPhone;

    CircleImageView spImageView;
    TextView spNameTextView;
    RatingBar ratingBar;
    EditText editTextReview;
    Button buttonPost;
    ProgressDialog loadingBar;
    DatabaseReference reviewRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_write_review);

        spID = getIntent().getStringExtra("providerID");

        loadingBar = new ProgressDialog(this);

        ShowAllInfo();

        reviewRef = FirebaseDatabase.getInstance().getReference().child("Reviews");

        spImageView = findViewById(R.id.image_sp_write_review);
        spNameTextView = findViewById(R.id.name_sp_write_review);

        ratingBar = findViewById(R.id.rating_sp_write_review);
        editTextReview = findViewById(R.id.review_content);
        buttonPost = findViewById(R.id.post_review);

        buttonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveReview();
            }
        });

    }

    private void SaveReview() {

        String saveCurrentTime, saveCurrentDate;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calForDate.getTime());

        // reviewerId, reviewerName, reviewText, reviewerImage, reviewDate;
        HashMap<String, Object> reviewMap = new HashMap<>();
        reviewMap.put("reviewerId", Prevalent.currentOnlineUser.getPhone());
        reviewMap.put("reviewerName", Prevalent.currentOnlineUser.getName());
        reviewMap.put("reviewText", editTextReview.getText().toString());
        reviewMap.put("reviewerImage", Prevalent.currentOnlineUser.getImage());
        reviewMap.put("reviewDate", saveCurrentDate);

        String key = saveCurrentDate+saveCurrentTime;

        reviewRef.child(spPhone).child(key).updateChildren(reviewMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(getApplicationContext(), "Review Posted Successfully.", Toast.LENGTH_SHORT).show();

                            // Reset Input Fields
                            loadingBar.dismiss();
                            editTextReview.setText("");

                            Intent intent = new Intent(getApplicationContext(), CustomerHomeActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            loadingBar.dismiss();
                            Toast.makeText(getApplicationContext(), "Network Error: Please try again after some time.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void ShowAllInfo() {

        FirebaseDatabase.getInstance().getReference().child("ServiceProviders").child(spID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {

                            Users sp = snapshot.getValue(Users.class);

                            spName = sp.getName();
                            spPhone = sp.getPhone();

                            spNameTextView.setText(spName);
                            Picasso.get().load(sp.getImage()).into(spImageView);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    public void BackTo(View view) {
        Intent intent = new Intent(getApplicationContext(), MakeRequestActivity.class);
        intent.putExtra("providerID", spID);
        startActivity(intent);
        finish();
    }

}