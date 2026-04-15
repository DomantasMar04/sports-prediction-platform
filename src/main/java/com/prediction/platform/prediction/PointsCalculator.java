package com.prediction.platform.prediction;

import com.prediction.platform.match.Match;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PointsCalculator {

    public int calculate(Prediction prediction, Match match) {
        int points = 0;
        PointsBreakdown breakdown = new PointsBreakdown();

        // 1. Galutinis nugalėtojas (20 tšk)
        String actualWinner = getWinnerName(match);
        if (actualWinner != null && actualWinner.equals(prediction.getPredictedWinner())) {
            points += 20;
            breakdown.setWinnerPoints(20);
        }

        // 2. Individualūs komandų taškai (Intervalai jau yra tavo kode - paliekam/patobulinam)
        points += calculateScorePoints(prediction.getPredictedHomeScore(), match.getHomeScore(), "home", breakdown);
        points += calculateScorePoints(prediction.getPredictedAwayScore(), match.getAwayScore(), "away", breakdown);

        // 3. Bendra taškų suma (Nauja: Intervalai)
        int totalPred = prediction.getPredictedHomeScore() + prediction.getPredictedAwayScore();
        int totalActual = match.getHomeScore() + match.getAwayScore();
        int totalDiff = Math.abs(totalPred - totalActual);
        int totalPoints = totalDiff == 0 ? 20 : totalDiff <= 5 ? 10 : totalDiff <= 10 ? 5 : 0;
        points += totalPoints;
        breakdown.setTotalPoints(totalPoints);

        // 4. Taškų skirtumas (Nauja: Intervalai)
        int predDiff = prediction.getPredictedHomeScore() - prediction.getPredictedAwayScore();
        int actualDiff = match.getHomeScore() - match.getAwayScore();
        int diffError = Math.abs(predDiff - actualDiff);
        int diffPoints = diffError == 0 ? 30 : diffError <= 4 ? 15 : diffError <= 8 ? 5 : 0;
        points += diffPoints;
        breakdown.setDiffPoints(diffPoints);

        // 5. Ketvirčių nugalėtojas (Patobulinta: Intervalas už "beveik" atspėtą santykį)
        if (prediction.getPredictedMostQuartersWinner() != null && match.getQuarterResults() != null) {
            int actualQWinner = parseMostQuartersWinner(match.getQuarterResults());
            if (prediction.getPredictedMostQuartersWinner() == actualQWinner) {
                points += 20;
                breakdown.setQuartersPoints(20);
            } else if (actualQWinner == 0) { // Jei realybėje lygios, o tu spėjai kažką laimint - duodam 10
                points += 10;
                breakdown.setQuartersPoints(10);
            }
        }

        prediction.setBreakdown(breakdown.toJson());
        return points;
    }

    // Tavo originalus metodas su Regex (veikia puikiai)
    private int parseMostQuartersWinner(String raw) {
        List<Integer> nums = new ArrayList<>();
        Matcher m = Pattern.compile("\\d+").matcher(raw);
        while (m.find()) nums.add(Integer.parseInt(m.group()));
        if (nums.size() < 8) return 0;
        int hWins = 0, aWins = 0;
        for (int i = 0; i < 4; i++) {
            if (nums.get(i) > nums.get(i + 4)) hWins++;
            else if (nums.get(i + 4) > nums.get(i)) aWins++;
        }
        return hWins > aWins ? 1 : (aWins > hWins ? 2 : 0);
    }

    private int calculateScorePoints(Integer pred, Integer actual, String side, PointsBreakdown b) {
        if (pred == null || actual == null) return 0;
        int diff = Math.abs(pred - actual);
        // Intervalai: Tiksliai=50, iki 3tšk=30, iki 7tšk=15, iki 12tšk=5
        int p = diff == 0 ? 50 : diff <= 3 ? 30 : diff <= 7 ? 15 : diff <= 12 ? 5 : 0;
        if (side.equals("home")) b.setHomePoints(p); else b.setAwayPoints(p);
        return p;
    }

    private String getWinnerName(Match m) {
        if (m.getHomeScore() == null || m.getAwayScore() == null) return null;
        if (m.getHomeScore() > m.getAwayScore()) return m.getHomeTeam().getName();
        if (m.getAwayScore() > m.getHomeScore()) return m.getAwayTeam().getName();
        return "DRAW";
    }

    // Papildyta Breakdown klasė, kad JSON matytųsi visi nauji laukai
    public static class PointsBreakdown {
        private int winner, home, away, quarters, total, diff;
        public void setWinnerPoints(int v) { this.winner = v; }
        public void setHomePoints(int v) { this.home = v; }
        public void setAwayPoints(int v) { this.away = v; }
        public void setQuartersPoints(int v) { this.quarters = v; }
        public void setTotalPoints(int v) { this.total = v; }
        public void setDiffPoints(int v) { this.diff = v; }

        public String toJson() {
            return String.format(
                    "{\"winner\":%d,\"home\":%d,\"away\":%d,\"quarters\":%d,\"totalSum\":%d,\"difference\":%d}",
                    winner, home, away, quarters, total, diff
            );
        }
    }
}