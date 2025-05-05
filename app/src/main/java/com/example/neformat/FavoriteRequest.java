package com.example.neformat;

public class FavoriteRequest {
    private String firebaseUid;
    private String imageUrl;  // Изменено с Long designId на String imageUrl

    public FavoriteRequest(String firebaseUid, String imageUrl) {
        this.firebaseUid = firebaseUid;
        this.imageUrl = imageUrl;  // Инициализация imageUrl
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
