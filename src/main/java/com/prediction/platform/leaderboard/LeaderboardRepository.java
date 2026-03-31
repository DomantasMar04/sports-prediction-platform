package com.prediction.platform.leaderboard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, Long> {

    List<Leaderboard> findByLeagueIdOrderByTotalPointsDesc(Long leagueId);

    List<Leaderboard> findAllByOrderByTotalPointsDesc();

    Optional<Leaderboard> findByUserIdAndLeagueId(Long userId, Long leagueId);
}