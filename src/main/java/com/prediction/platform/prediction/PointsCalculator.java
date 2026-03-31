package com.prediction.platform.prediction;

import com.prediction.platform.match.Match;
import org.springframework.stereotype.Component;

@Component
public class PointsCalculator {

    public int calculate(Prediction prediction, Match match) {
        int points = 0;

        // Laimėtoja komanda — 20 taškų
        String actualWinner = getWinner(match);
        if (actualWinner != null && actualWinner.equals(prediction.getPredictedWinner())) {
            points += 20;
        }

        // Pirmieji taškai — 25 taškai
        if (match.getFirstScorer() != null &&
                match.getFirstScorer().equals(prediction.getPredictedFirstScorer())) {
            points += 25;
        }

        // MVP — 30 taškų
        if (match.getMvpPlayer() != null &&
                match.getMvpPlayer().equals(prediction.getPredictedMvp())) {
            points += 30;
        }

        // Komandos taškai (home team)
        if (prediction.getPredictedHomeScore() != null && match.getHomeScore() != null) {
            int diff = Math.abs(prediction.getPredictedHomeScore() - match.getHomeScore());
            if (diff == 0)       points += 50;
            else if (diff <= 5)  points += 30;
            else if (diff <= 10) points += 20;
            else                 points += 10;
        }

        // Taškų skirtumas
        if (prediction.getPredictedHomeScore() != null &&
                prediction.getPredictedAwayScore() != null &&
                match.getHomeScore() != null && match.getAwayScore() != null) {

            int predDiff = Math.abs(prediction.getPredictedHomeScore() - prediction.getPredictedAwayScore());
            int realDiff = Math.abs(match.getHomeScore() - match.getAwayScore());
            int diff = Math.abs(predDiff - realDiff);

            if (diff == 0)        points += 50;
            else if (diff <= 5)   points += 30;
            else if (diff <= 15)  points += 20;
            else                  points += 10;
        }

        // Bendras rezultatyvumas
        if (prediction.getPredictedHomeScore() != null &&
                prediction.getPredictedAwayScore() != null &&
                match.getHomeScore() != null && match.getAwayScore() != null) {

            int predTotal = prediction.getPredictedHomeScore() + prediction.getPredictedAwayScore();
            int realTotal = match.getHomeScore() + match.getAwayScore();
            int diff = Math.abs(predTotal - realTotal);

            if (diff == 0)        points += 50;
            else if (diff <= 10)  points += 40;
            else if (diff <= 20)  points += 30;
            else                  points += 20;
        }

        // Bonus — tikslus rezultatas
        if (prediction.getPredictedHomeScore() != null &&
                prediction.getPredictedAwayScore() != null &&
                prediction.getPredictedHomeScore().equals(match.getHomeScore()) &&
                prediction.getPredictedAwayScore().equals(match.getAwayScore())) {
            points += 50;
        }

        return points;
    }

    private String getWinner(Match match) {
        if (match.getHomeScore() == null || match.getAwayScore() == null) return null;
        if (match.getHomeScore() > match.getAwayScore()) return match.getHomeTeam().getName();
        if (match.getAwayScore() > match.getHomeScore()) return match.getAwayTeam().getName();
        return "DRAW";
    }
}