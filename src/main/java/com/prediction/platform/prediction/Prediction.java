package com.prediction.platform.prediction;

import com.prediction.platform.match.Match;
import com.prediction.platform.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "predictions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    private String predictedWinner;
    private Integer predictedHomeScore;
    private Integer predictedAwayScore;
    private Integer predictedMostQuartersWinner;

    @Column(name = "points_earned")
    private Integer pointsEarned = 0;

    @Column(name = "is_calculated")
    private Boolean isCalculated = false;

    // JSON breakdown: {"winner":20,"homeScore":30,...}
    @Column(name = "breakdown", length = 500)
    private String breakdown;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}