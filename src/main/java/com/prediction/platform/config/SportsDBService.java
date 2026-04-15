package com.prediction.platform.config;

import com.prediction.platform.league.League;
import com.prediction.platform.league.LeagueRepository;
import com.prediction.platform.match.Match;
import com.prediction.platform.match.MatchRepository;
import com.prediction.platform.team.Team;
import com.prediction.platform.team.TeamRepository;
import com.prediction.platform.prediction.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SportsDBService {

    private final SportsDBClient sportsDbClient;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;
    private final PredictionService predictionService;

    /**
     * Sinchronizuoja ateinančias rungtynes pagal lygos ID
     */
    @Transactional
    public int syncUpcomingMatches(String leagueExternalId) {
        Map<String, Object> response = sportsDbClient.getNextLeagueEvents(leagueExternalId);

        if (response == null || response.get("events") == null) {
            return 0;
        }

        List<Map<String, Object>> events = (List<Map<String, Object>>) response.get("events");
        int savedCount = 0;

        League league = leagueRepository.findByExternalId(leagueExternalId)
                .orElseGet(() -> {
                    League newLeague = new League();
                    newLeague.setName("League " + leagueExternalId);
                    newLeague.setExternalId(leagueExternalId);
                    newLeague.setType(League.LeagueType.REAL);
                    return leagueRepository.save(newLeague);
                });

        for (Map<String, Object> event : events) {
            String externalId = value(event.get("idEvent"));
            if (externalId == null || matchRepository.findByExternalId(externalId).isPresent()) {
                continue;
            }

            String homeTeamName = value(event.get("strHomeTeam"));
            String awayTeamName = value(event.get("strAwayTeam"));

            Team homeTeam = getOrCreateTeam(homeTeamName, league);
            Team awayTeam = getOrCreateTeam(awayTeamName, league);

            Match match = new Match();
            match.setExternalId(externalId);
            match.setLeague(league);
            match.setHomeTeam(homeTeam);
            match.setAwayTeam(awayTeam);
            match.setStartTime(parseDateTime(event));
            match.setStatus(Match.MatchStatus.UPCOMING);

            matchRepository.save(match);
            savedCount++;
        }

        return savedCount;
    }

    /**
     * Atnaujina išsaugotų rungtynių rezultatus (Live ir baigtų)
     */
    @Transactional
    public int refreshSavedMatchesResults() {
        // Tikriname tik tas, kurios dar nebaigtos
        List<Match> matches = matchRepository.findByStatusIn(
                List.of(Match.MatchStatus.UPCOMING, Match.MatchStatus.LIVE)
        );

        int updatedCount = 0;

        for (Match match : matches) {
            if (match.getExternalId() == null) continue;

            Map<String, Object> response = sportsDbClient.lookupEventById(match.getExternalId());
            if (response == null || response.get("events") == null) continue;

            List<Map<String, Object>> events = (List<Map<String, Object>>) response.get("events");
            if (events.isEmpty()) continue;

            Map<String, Object> event = events.get(0);

            String statusStr = value(event.get("strStatus"));
            Integer homeScore = parseInteger(event.get("intHomeScore"));
            Integer awayScore = parseInteger(event.get("intAwayScore"));
            String quarterResults = value(event.get("strResult")); // Rezultatai iš API (pvz. "20 20...")

            if (statusStr != null) {
                String normalized = statusStr.toLowerCase();

                // 1. Tikriname ar rungtynės baigtos
                if (normalized.contains("finished") || normalized.contains("ft")) {
                    boolean wasNotFinished = match.getStatus() != Match.MatchStatus.FINISHED;

                    match.setStatus(Match.MatchStatus.FINISHED);
                    match.setHomeScore(homeScore);
                    match.setAwayScore(awayScore);
                    match.setQuarterResults(quarterResults);
                    matchRepository.save(match);

                    // Jei statusas pasikeitė į FINISHED, iškart skaičiuojame taškus
                    if (wasNotFinished) {
                        try {
                            predictionService.calculatePoints(match.getId());
                        } catch (Exception e) {
                            // Log klaida, bet tęsiame kitas rungtynes
                            System.err.println("Klaida skaičiuojant taškus: " + e.getMessage());
                        }
                    }
                    updatedCount++;
                }
                // 2. Tikriname ar rungtynės vyksta (LIVE)
                else if (normalized.contains("live") || normalized.contains("play") || normalized.contains("quarter")) {
                    match.setStatus(Match.MatchStatus.LIVE);
                    match.setHomeScore(homeScore);
                    match.setAwayScore(awayScore);
                    match.setQuarterResults(quarterResults);
                    matchRepository.save(match);
                    updatedCount++;
                }
            }
        }
        return updatedCount;
    }

    private Team getOrCreateTeam(String teamName, League league) {
        return teamRepository.findByName(teamName)
                .orElseGet(() -> {
                    Team team = new Team();
                    team.setName(teamName);
                    team.setLeague(league);
                    return teamRepository.save(team);
                });
    }

    private LocalDateTime parseDateTime(Map<String, Object> event) {
        String timestamp = value(event.get("strTimestamp"));
        if (timestamp != null && !timestamp.isBlank()) {
            try {
                return LocalDateTime.parse(timestamp);
            } catch (Exception e) {
                // Jei formatas kitoks, bandome date + time
            }
        }

        String date = value(event.get("dateEvent"));
        String time = value(event.get("strTime"));

        if (date != null && time != null) {
            String normalizedTime = time.length() == 5 ? time + ":00" : time;
            try {
                return LocalDateTime.parse(date + "T" + normalizedTime);
            } catch (Exception e) {
                return LocalDateTime.now();
            }
        }

        return LocalDateTime.now();
    }

    private Integer parseInteger(Object obj) {
        if (obj == null) return null;
        String val = obj.toString().trim();
        if (val.isBlank() || val.equalsIgnoreCase("null")) return null;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String value(Object obj) {
        return (obj != null && !obj.toString().equalsIgnoreCase("null")) ? obj.toString() : null;
    }
}