package com.example.homeserviceapp.Customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.homeserviceapp.Model.ReviewModel;
import com.example.homeserviceapp.Model.Users;
import com.example.homeserviceapp.Prevalent.Prevalent;
import com.example.homeserviceapp.R;
import com.example.homeserviceapp.ViewHolder.ReviewViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MakeRequestActivity extends AppCompatActivity {

    private static final int REQUEST_CALL = 1;

    String service, spID, spName, spPhone;

    CircleImageView spImageView;
    TextView spNameTextView;
    RatingBar spRating;
    RecyclerView recyclerView;
    Button writeReview;
    DatabaseReference reviewRef;
    ImageButton makeCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_make_request);

        spID = getIntent().getStringExtra("providerID");


        ShowAllInfo();

        spImageView = findViewById(R.id.image_sp);
        spNameTextView = findViewById(R.id.name_sp);
        spRating = findViewById(R.id.rating_sp);
        recyclerView = findViewById(R.id.all_reviews_clicked_sp);
        writeReview = findViewById(R.id.write_review_btn_sp);
        makeCall = findViewById(R.id.make_call_sp);

        writeReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteReviewActivity.class);
                intent.putExtra("providerID", spID);
                startActivity(intent);
            }
        });

        makeCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MakeCall();
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


                            // getting reviews
                            reviewRef = FirebaseDatabase.getInstance().getReference().child("Reviews");

                            FirebaseRecyclerOptions<ReviewModel> options =
                                    new FirebaseRecyclerOptions.Builder<ReviewModel>()
                                            .setQuery(reviewRef.child(spPhone), ReviewModel.class)
                                            .build();

                            FirebaseRecyclerAdapter<ReviewModel, ReviewViewHolder> adapter =
                                    new FirebaseRecyclerAdapter<ReviewModel, ReviewViewHolder>(options) {
                                        @Override
                                        protected void onBindViewHolder(@NonNull ReviewViewHolder holder, int position, @NonNull final ReviewModel model) {

                                            holder.reviewerName.setText(model.getReviewerName());
                                            holder.reviewDate.setText(model.getReviewDate());
                                            holder.reviewText.setText(model.getReviewText());

                                            Picasso.get().load(model.getReviewerImage()).into(holder.reviewerImage);
                                        }

                                        @NonNull
                                        @Override
                                        public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_review_layout, parent, false);
                                            ReviewViewHolder holder = new ReviewViewHolder(view);
                                            return holder;
                                        }
                                    };
                            recyclerView.setAdapter(adapter);
                            adapter.startListening();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void BackTo(View view) {
        Intent intent = new Intent(getApplicationContext(), CustomerHomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), CustomerHomeActivity.class);
        startActivity(intent);
        finish();
    }

    public void MakeCall() {
        if (spPhone.trim().length() > 0) {
            if (ContextCompat.checkSelfPermission(MakeRequestActivity.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MakeRequestActivity.this,
                        new String[]
                                {
                                        Manifest.permission.CALL_PHONE
                                }, REQUEST_CALL);

            } else {
                String dial = "tel:" + spPhone;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                MakeCall();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}