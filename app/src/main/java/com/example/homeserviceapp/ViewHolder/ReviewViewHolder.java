package com.example.homeserviceapp.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeserviceapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewViewHolder extends RecyclerView.ViewHolder{

    public CircleImageView reviewerImage;
    public TextView reviewerName, reviewText, reviewDate;



    public ReviewViewHolder(View itemView) {
        super(itemView);

        reviewerImage = itemView.findViewById(R.id.reviewer_profile_image);
        reviewerName = (TextView) itemView.findViewById(R.id.reviewer_name);
        reviewText = (TextView) itemView.findViewById(R.id.review);
        reviewDate = (TextView) itemView.findViewById(R.id.review_date);
    }
}
