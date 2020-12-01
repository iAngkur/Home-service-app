package com.example.homeserviceapp.Model;

public class ReviewModel {
    String reviewerId, reviewerName, reviewText, reviewerImage, reviewDate;

    public ReviewModel() {
    }

    public ReviewModel(String reviewerId, String reviewerName, String reviewText, String reviewerImage, String reviewDate) {
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
        this.reviewText = reviewText;
        this.reviewerImage = reviewerImage;
        this.reviewDate = reviewDate;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public String getReviewerImage() {
        return reviewerImage;
    }

    public void setReviewerImage(String reviewerImage) {
        this.reviewerImage = reviewerImage;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(String reviewDate) {
        this.reviewDate = reviewDate;
    }
}
