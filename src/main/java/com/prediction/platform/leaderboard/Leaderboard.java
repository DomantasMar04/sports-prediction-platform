package com.prediction.platform.leaderboard;

import com.prediction.platform.league.League;
import com.prediction.platform.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "leaderboard")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Leaderboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "league_id")
    private League league;

    @Column(name = "total_points")
    private Integer totalPoints = 0;

    @Column(name = "predictions_count")
    private Integer predictionsCount = 0;
}