package com.prediction.platform.dto;

public class SyncResponse {
    private String message;
    private int savedCount;

    public SyncResponse() {
    }

    public SyncResponse(String message, int savedCount) {
        this.message = message;
        this.savedCount = savedCount;
    }

    public String getMessage() {
        return message;
    }

    public int getSavedCount() {
        return savedCount;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSavedCount(int savedCount) {
        this.savedCount = savedCount;
    }
}
