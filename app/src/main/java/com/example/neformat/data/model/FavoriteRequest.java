package com.example.neformat.data.model;

public class FavoriteRequest {
    private String firebaseUid;
    private String imageUrl;

    public FavoriteRequest(String firebaseUid, String imageUrl) {
        this.firebaseUid = firebaseUid;
        this.imageUrl = imageUrl;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
