package com.prediction.platform.prediction;

import com.prediction.platform.match.Match;
import org.springframework.stereotype.Component;

@Component
public class PointsCalculator {

    public int calculate(Prediction prediction, Match match) {
        int points = 0;
        PointsBreakdown breakdown = new PointsBreakdown();

        // 1. Laimėtoja komanda — 20 taškų
        String actualWinner = getWinner(match);
        if (actualWinner != null && actualWinner.equals(prediction.getPredictedWinner())) {
            points += 20;
            breakdown.setWinnerPoints(20);
        }

        // 2. Namų komandos taškai — iki 50 taškų
        if (prediction.getPredictedHomeScore() != null && match.getHomeScore() != null) {
            int diff = Math.abs(prediction.getPredictedHomeScore() - match.getHomeScore());
            int p = diff == 0 ? 50 : diff <= 5 ? 30 : diff <= 10 ? 20 : 10;
            points += p;
            breakdown.setHomeScorePoints(p);
        }

        // 3. Svečių komandos taškai — iki 50 taškų
        if (prediction.getPredictedAwayScore() != null && match.getAwayScore() != null) {
            int diff = Math.abs(prediction.getPredictedAwayScore() - match.getAwayScore());
            int p = diff == 0 ? 50 : diff <= 5 ? 30 : diff <= 10 ? 20 : 10;
            points += p;
            breakdown.setAwayScorePoints(p);
        }

        // 4. Taškų skirtumas — iki 50 taškų
        if (prediction.getPredictedHomeScore() != null && prediction.getPredictedAwayScore() != null
                && match.getHomeScore() != null && match.getAwayScore() != null) {
            int predDiff = Math.abs(prediction.getPredictedHomeScore() - prediction.getPredictedAwayScore());
            int realDiff = Math.abs(match.getHomeScore() - match.getAwayScore());
            int diff = Math.abs(predDiff - realDiff);
            int p = diff == 0 ? 50 : diff <= 5 ? 30 : diff <= 15 ? 20 : 10;
            points += p;
            breakdown.setDiffPoints(p);
        }

        // 5. Bendras rezultatyvumas — iki 50 taškų
        if (prediction.getPredictedHomeScore() != null && prediction.getPredictedAwayScore() != null
                && match.getHomeScore() != null && match.getAwayScore() != null) {
            int predTotal = prediction.getPredictedHomeScore() + prediction.getPredictedAwayScore();
            int realTotal = match.getHomeScore() + match.getAwayScore();
            int diff = Math.abs(predTotal - realTotal);
            int p = diff == 0 ? 50 : diff <= 10 ? 40 : diff <= 20 ? 30 : 20;
            points += p;
            breakdown.setTotalPoints(p);
        }

        // 6. Bonus — tikslus galutinis rezultatas
        if (prediction.getPredictedHomeScore() != null && prediction.getPredictedAwayScore() != null
                && prediction.getPredictedHomeScore().equals(match.getHomeScore())
                && prediction.getPredictedAwayScore().equals(match.getAwayScore())) {
            points += 50;
            breakdown.setExactScoreBonus(50);
        }

        prediction.setBreakdown(breakdown.toJson());
        return points;
    }

    private String getWinner(Match match) {
        if (match.getHomeScore() == null || match.getAwayScore() == null) return null;
        if (match.getHomeScore() > match.getAwayScore()) return match.getHomeTeam().getName();
        if (match.getAwayScore() > match.getHomeScore()) return match.getAwayTeam().getName();
        return "DRAW";
    }

    public static class PointsBreakdown {
        private int winnerPoints;
        private int homeScorePoints;
        private int awayScorePoints;
        private int diffPoints;
        private int totalPoints;
        private int exactScoreBonus;

        public void setWinnerPoints(int v)    { this.winnerPoints = v; }
        public void setHomeScorePoints(int v) { this.homeScorePoints = v; }
        public void setAwayScorePoints(int v) { this.awayScorePoints = v; }
        public void setDiffPoints(int v)      { this.diffPoints = v; }
        public void setTotalPoints(int v)     { this.totalPoints = v; }
        public void setExactScoreBonus(int v) { this.exactScoreBonus = v; }

        public String toJson() {
            return String.format(
                    "{\"winner\":%d,\"homeScore\":%d,\"awayScore\":%d,\"diff\":%d,\"total\":%d,\"exactBonus\":%d}",
                    winnerPoints, homeScorePoints, awayScorePoints, diffPoints, totalPoints, exactScoreBonus
            );
        }
    }
}