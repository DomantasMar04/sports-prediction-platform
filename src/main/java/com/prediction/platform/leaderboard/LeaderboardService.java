package com.prediction.platform.leaderboard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;

    public List<Leaderboard> getLeagueLeaderboard(Long leagueId) {
        return leaderboardRepository.findByLeagueIdOrderByTotalPointsDesc(leagueId);
    }

    public List<Leaderboard> getGlobalLeaderboard() {
        return leaderboardRepository.findAllByOrderByTotalPointsDesc();
    }
}