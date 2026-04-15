package com.prediction.platform.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionSummaryResponse {

    private Long id;
    private Long matchId;
    private String homeTeam;
    private String awayTeam;
    private LocalDateTime startTime;
    private String matchStatus;

    // Vartotojo spėjimai
    private String predictedWinner;
    private Integer predictedHomeScore;
    private Integer predictedAwayScore;
    private Integer predictedMostQuartersWinner; // Naujas laukas (1, 2 arba 0)

    // Tikri rezultatai iš API
    private Integer actualHomeScore;
    private Integer actualAwayScore;
    private String quarterResults; // Naujas laukas kėliniams

    // Rezultatų informacija
    private Integer pointsEarned;
    private Boolean isCalculated;
    private String breakdown;
    private LocalDateTime createdAt;
}