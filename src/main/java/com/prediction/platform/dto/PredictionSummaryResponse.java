package com.prediction.platform.dto;

import java.time.LocalDateTime;

public class PredictionSummaryResponse {

    private Long id;
    private Long matchId;
    private String homeTeam;
    private String awayTeam;
    private LocalDateTime startTime;
    private String matchStatus;

    private String predictedWinner;
    private Integer predictedHomeScore;
    private Integer predictedAwayScore;

    private Integer actualHomeScore;
    private Integer actualAwayScore;

    private Integer pointsEarned;
    private Boolean isCalculated;
    private String breakdown;
    private LocalDateTime createdAt;

    public PredictionSummaryResponse() {
    }

    public PredictionSummaryResponse(Long id,
        Long matchId,
        String homeTeam,
        String awayTeam,
        LocalDateTime startTime,
        String matchStatus,
        String predictedWinner,
        Integer predictedHomeScore,
        Integer predictedAwayScore,
        Integer actualHomeScore,
        Integer actualAwayScore,
        Integer pointsEarned,
        Boolean isCalculated,
        String breakdown,
        LocalDateTime createdAt) {
        this.id = id;
        this.matchId = matchId;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.startTime = startTime;
        this.matchStatus = matchStatus;
        this.predictedWinner = predictedWinner;
        this.predictedHomeScore = predictedHomeScore;
        this.predictedAwayScore = predictedAwayScore;
        this.actualHomeScore = actualHomeScore;
        this.actualAwayScore = actualAwayScore;
        this.pointsEarned = pointsEarned;
        this.isCalculated = isCalculated;
        this.breakdown = breakdown;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getMatchId() {
        return matchId;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public String getMatchStatus() {
        return matchStatus;
    }

    public String getPredictedWinner() {
        return predictedWinner;
    }

    public Integer getPredictedHomeScore() {
        return predictedHomeScore;
    }

    public Integer getPredictedAwayScore() {
        return predictedAwayScore;
    }

    public Integer getActualHomeScore() {
        return actualHomeScore;
    }

    public Integer getActualAwayScore() {
        return actualAwayScore;
    }

    public Integer getPointsEarned() {
        return pointsEarned;
    }

    public Boolean getIsCalculated() {
        return isCalculated;
    }

    public String getBreakdown() {
        return breakdown;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setMatchStatus(String matchStatus) {
        this.matchStatus = matchStatus;
    }

    public void setPredictedWinner(String predictedWinner) {
        this.predictedWinner = predictedWinner;
    }

    public void setPredictedHomeScore(Integer predictedHomeScore) {
        this.predictedHomeScore = predictedHomeScore;
    }

    public void setPredictedAwayScore(Integer predictedAwayScore) {
        this.predictedAwayScore = predictedAwayScore;
    }

    public void setActualHomeScore(Integer actualHomeScore) {
        this.actualHomeScore = actualHomeScore;
    }

    public void setActualAwayScore(Integer actualAwayScore) {
        this.actualAwayScore = actualAwayScore;
    }

    public void setPointsEarned(Integer pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public void setIsCalculated(Boolean calculated) {
        isCalculated = calculated;
    }

    public void setBreakdown(String breakdown) {
        this.breakdown = breakdown;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}