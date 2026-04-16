package com.prediction.platform.match;

import com.prediction.platform.prediction.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchScheduler {

    private final MatchRepository matchRepository;
    private final PredictionService predictionService;

    @Scheduled(fixedDelay = 5 * 60 * 1000) // kas 5 min
    public void autoCalculatePoints() {
        LocalDateTime now = LocalDateTime.now();

        List<Match> finished = matchRepository.findByStatus(Match.MatchStatus.FINISHED);

        for (Match match : finished) {
            if (match.getStartTime() == null) continue;

            LocalDateTime calculateAfter = match.getStartTime().plusMinutes(30 + 150);

            if (now.isAfter(calculateAfter)) {
                try {
                    predictionService.calculatePoints(match.getId());
                } catch (Exception e) {
                    System.out.println("Scheduler skip match " + match.getId() + ": " + e.getMessage());
                }
            }
        }
    }


    @Scheduled(fixedDelay = 60 * 1000) // kas minutę
    public void updateMatchStatuses() {
        LocalDateTime now = LocalDateTime.now();
        List<Match> upcoming = matchRepository.findByStatus(Match.MatchStatus.UPCOMING);

        for (Match match : upcoming) {
            if (match.getStartTime() != null && now.isAfter(match.getStartTime())) {
                match.setStatus(Match.MatchStatus.LIVE);
                matchRepository.save(match);
                System.out.println("Match " + match.getId() + " perjungtas į LIVE");
            }
        }
    }
}