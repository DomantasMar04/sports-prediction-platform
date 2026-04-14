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

    /**
     * Kas 5 minutes tikrina ar yra baigtų rungtynių kurių taškai dar nesuskaičiuoti.
     * Skaičiuoja pusvalandžiu po rungtynių pabaigos (startTime + 3h + 30min buffer).
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000) // kas 5 min
    public void autoCalculatePoints() {
        LocalDateTime now = LocalDateTime.now();

        // Imame visas FINISHED rungtynes
        List<Match> finished = matchRepository.findByStatus(Match.MatchStatus.FINISHED);

        for (Match match : finished) {
            if (match.getStartTime() == null) continue;

            // Laukiam 30 min po rungtynių pradžios + ~2.5h (vidutinė trukmė) = ~3h po pradžios
            LocalDateTime calculateAfter = match.getStartTime().plusMinutes(30 + 150);

            if (now.isAfter(calculateAfter)) {
                try {
                    predictionService.calculatePoints(match.getId());
                } catch (Exception e) {
                    // "Match not finished" arba jau suskaičiuoti — ignoruojam
                    System.out.println("Scheduler skip match " + match.getId() + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Kas minutę tikrina ar UPCOMING rungtynės jau turėtų prasidėti
     * ir pakeičia statusą į LIVE (jei startTime praėjo).
     */
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