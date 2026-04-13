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
    private String predictedFirstScorer;
    private String predictedMvp;
    private Integer predictedHomeScore;
    private Integer predictedAwayScore;

    @Column(name = "points_earned")
    private Integer pointsEarned = 0;

    @Column(name = "is_calculated")
    private Boolean isCalculated = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}