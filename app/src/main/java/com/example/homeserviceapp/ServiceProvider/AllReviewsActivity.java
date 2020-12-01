package com.example.homeserviceapp.ServiceProvider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toolbar;

import com.example.homeserviceapp.Model.ReviewModel;
import com.example.homeserviceapp.Prevalent.Prevalent;
import com.example.homeserviceapp.R;
import com.example.homeserviceapp.ViewHolder.ReviewViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class AllReviewsActivity extends AppCompatActivity {

    DatabaseReference reviewRef;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_all_reviews);

        reviewRef = FirebaseDatabase.getInstance().getReference().child("Reviews");

        recyclerView = findViewById(R.id.all_reviews);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ReviewModel> options =
                new FirebaseRecyclerOptions.Builder<ReviewModel>()
                        .setQuery(reviewRef.child(Prevalent.currentOnlineUser.getPhone()), ReviewModel.class)
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), ServiceProviderHomeActivity.class));
        finish();
    }
}