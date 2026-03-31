package com.prediction.platform.prediction;

import com.prediction.platform.match.Match;
import com.prediction.platform.match.MatchRepository;
import com.prediction.platform.leaderboard.Leaderboard;
import com.prediction.platform.leaderboard.LeaderboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final MatchRepository matchRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final PointsCalculator pointsCalculator;

    public Prediction createPrediction(Prediction prediction) {
        // Patikrink ar rungtynės dar nevyksta
        Match match = matchRepository.findById(prediction.getMatch().getId())
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (match.getStatus() != Match.MatchStatus.UPCOMING) {
            throw new RuntimeException("Cannot predict - match already started");
        }

        return predictionRepository.save(prediction);
    }

    public Prediction updatePrediction(Long id, Prediction updated) {
        Prediction existing = predictionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prediction not found"));

        Match match = existing.getMatch();
        if (match.getStatus() != Match.MatchStatus.UPCOMING) {
            throw new RuntimeException("Cannot update - match already started");
        }

        existing.setPredictedWinner(updated.getPredictedWinner());
        existing.setPredictedFirstScorer(updated.getPredictedFirstScorer());
        existing.setPredictedMvp(updated.getPredictedMvp());
        existing.setPredictedHomeScore(updated.getPredictedHomeScore());
        existing.setPredictedAwayScore(updated.getPredictedAwayScore());
        return predictionRepository.save(existing);
    }

    public List<Prediction> getUserPredictions(Long userId) {
        return predictionRepository.findByUserId(userId);
    }

    public List<Prediction> getMatchPredictions(Long matchId) {
        return predictionRepository.findByMatchId(matchId);
    }

    public Prediction getMyPrediction(Long userId, Long matchId) {
        return predictionRepository.findByUserIdAndMatchId(userId, matchId)
                .orElseThrow(() -> new RuntimeException("Prediction not found"));
    }

    public void calculatePoints(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (match.getStatus() != Match.MatchStatus.FINISHED) {
            throw new RuntimeException("Match not finished yet");
        }

        List<Prediction> predictions = predictionRepository.findByMatchId(matchId);

        for (Prediction prediction : predictions) {
            if (!prediction.getIsCalculated()) {
                int points = pointsCalculator.calculate(prediction, match);
                prediction.setPointsEarned(points);
                prediction.setIsCalculated(true);
                predictionRepository.save(prediction);

                updateLeaderboard(prediction.getUser().getId(),
                        match.getLeague().getId(), points);
            }
        }
    }

    private void updateLeaderboard(Long userId, Long leagueId, int points) {
        Leaderboard entry = leaderboardRepository
                .findByUserIdAndLeagueId(userId, leagueId)
                .orElse(new Leaderboard(null, null, null, 0, 0));

        entry.setTotalPoints(entry.getTotalPoints() + points);
        entry.setPredictionsCount(entry.getPredictionsCount() + 1);
        leaderboardRepository.save(entry);
    }
}