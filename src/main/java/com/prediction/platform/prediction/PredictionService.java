package com.prediction.platform.prediction;

import com.prediction.platform.match.Match;
import com.prediction.platform.match.MatchRepository;
import com.prediction.platform.leaderboard.Leaderboard;
import com.prediction.platform.leaderboard.LeaderboardRepository;
import com.prediction.platform.user.User;
import com.prediction.platform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final MatchRepository matchRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final UserRepository userRepository;
    private final PointsCalculator pointsCalculator;

    public Prediction createPrediction(Prediction prediction) {
        Match match = matchRepository.findById(prediction.getMatch().getId())
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (match.getStatus() != Match.MatchStatus.UPCOMING) {
            throw new RuntimeException("Cannot predict - match already started");
        }

        predictionRepository.findByUserIdAndMatchId(
                        prediction.getUser().getId(), prediction.getMatch().getId())
                .ifPresent(p -> { throw new RuntimeException("Prediction already exists"); });

        prediction.setCreatedAt(LocalDateTime.now());
        prediction.setUpdatedAt(LocalDateTime.now());
        return predictionRepository.save(prediction);
    }

    public Prediction updatePrediction(Long id, Long userId, Prediction updated) {
        Prediction existing = predictionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prediction not found"));

        if (!existing.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not your prediction");
        }

        Match match = existing.getMatch();
        if (match.getStatus() != Match.MatchStatus.UPCOMING) {
            throw new RuntimeException("Cannot update - match already started");
        }

        if (match.getStartTime() != null &&
                LocalDateTime.now().isAfter(match.getStartTime().minusMinutes(10))) {
            throw new RuntimeException("Cannot update - deadline passed");
        }

        existing.setPredictedWinner(updated.getPredictedWinner());
        existing.setPredictedHomeScore(updated.getPredictedHomeScore());
        existing.setPredictedAwayScore(updated.getPredictedAwayScore());
        existing.setUpdatedAt(LocalDateTime.now());
        return predictionRepository.save(existing);
    }

    public void deletePrediction(Long id, Long userId) {
        Prediction existing = predictionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prediction not found"));

        if (!existing.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not your prediction");
        }

        Match match = existing.getMatch();
        if (match.getStatus() != Match.MatchStatus.UPCOMING) {
            throw new RuntimeException("Cannot delete - match already started");
        }

        if (match.getStartTime() != null &&
                LocalDateTime.now().isAfter(match.getStartTime().minusMinutes(10))) {
            throw new RuntimeException("Cannot delete - deadline passed");
        }

        predictionRepository.delete(existing);
    }

    public List<Prediction> getUserPredictions(Long userId) {
        return predictionRepository.findByUserId(userId);
    }

    public List<Prediction> getMatchPredictions(Long matchId) {
        return predictionRepository.findByMatchId(matchId);
    }

    public Prediction getMyPrediction(Long userId, Long matchId) {
        return predictionRepository.findByUserIdAndMatchId(userId, matchId)
                .orElse(null);
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
                updateLeaderboard(prediction.getUser().getId(), match.getLeague().getId(), points);
            }
        }
    }

    private void updateLeaderboard(Long userId, Long leagueId, int points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Leaderboard entry = leaderboardRepository
                .findByUserIdAndLeagueId(userId, leagueId)
                .orElseGet(() -> {
                    Leaderboard l = new Leaderboard();
                    l.setUser(user);
                    // league bus set žemiau
                    l.setTotalPoints(0);
                    l.setPredictionsCount(0);
                    return l;
                });

        if (entry.getId() == null) {
            com.prediction.platform.league.League league =
                    new com.prediction.platform.league.League();
            league.setId(leagueId);
            entry.setLeague(league);
        }

        entry.setTotalPoints(entry.getTotalPoints() + points);
        entry.setPredictionsCount(entry.getPredictionsCount() + 1);
        leaderboardRepository.save(entry);
    }
}