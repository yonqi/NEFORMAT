package com.example.neformat;

public class FavoriteRequest {
    private String firebaseUid;
    private Long designId;

    public FavoriteRequest(String firebaseUid, Long designId) {
        this.firebaseUid = firebaseUid;
        this.designId = designId;
    }
}
