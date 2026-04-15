package com.prediction.platform.dto;

import java.util.List;

public class ProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private Integer totalScore;
    private long predictionsCount;
    private List<PredictionSummaryResponse> predictions;

    public ProfileResponse() {
    }

    public ProfileResponse(Long id,
       String username,
       String email,
       String role,
       Integer totalScore,
       long predictionsCount,
        List<PredictionSummaryResponse> predictions) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.totalScore = totalScore;
        this.predictionsCount = predictionsCount;
        this.predictions = predictions;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public long getPredictionsCount() {
        return predictionsCount;
    }

    public List<PredictionSummaryResponse> getPredictions() {
        return predictions;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public void setPredictionsCount(long predictionsCount) {
        this.predictionsCount = predictionsCount;
    }

    public void setPredictions(List<PredictionSummaryResponse> predictions) {
        this.predictions = predictions;
    }
}
