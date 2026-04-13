package com.prediction.platform.match;

import lombok.Data;

@Data
public class MatchResultRequest {
    private Integer homeScore;
    private Integer awayScore;
    private String mvpPlayer;
    private String firstScorer;
}