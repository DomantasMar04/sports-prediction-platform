package com.prediction.platform.config;

import com.prediction.platform.league.League;
import com.prediction.platform.league.LeagueRepository;
import com.prediction.platform.match.Match;
import com.prediction.platform.match.MatchRepository;
import com.prediction.platform.team.Team;
import com.prediction.platform.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.prediction.platform.prediction.PredictionService;

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
            return LocalDateTime.parse(timestamp);
        }

        String date = value(event.get("dateEvent"));
        String time = value(event.get("strTime"));

        if (date != null && time != null) {
            String normalizedTime = time.length() == 5 ? time + ":00" : time;
            return LocalDateTime.parse(date + "T" + normalizedTime);
        }

        return LocalDateTime.now();
    }



    public int refreshSavedMatchesResults() {
        List<Match> matches = matchRepository.findByStatusIn(
                List.of(Match.MatchStatus.UPCOMING, Match.MatchStatus.LIVE)
        );

        int updatedCount = 0;

        for (Match match : matches) {
            if (match.getExternalId() == null || match.getExternalId().isBlank()) {
                continue;
            }

            Map<String, Object> response = sportsDbClient.lookupEventById(match.getExternalId());

            if (response == null || response.get("events") == null) {
                continue;
            }

            List<Map<String, Object>> events = (List<Map<String, Object>>) response.get("events");
            if (events.isEmpty()) {
                continue;
            }

            Map<String, Object> event = events.get(0);

            String status = value(event.get("strStatus"));
            Integer homeScore = parseInteger(event.get("intHomeScore"));
            Integer awayScore = parseInteger(event.get("intAwayScore"));

            if (status != null) {
                String normalized = status.toLowerCase();

                if (normalized.contains("finished") || normalized.contains("ft") || normalized.contains("match finished")) {
                    boolean wasNotFinished = match.getStatus() != Match.MatchStatus.FINISHED;

                    match.setStatus(Match.MatchStatus.FINISHED);

                    if (homeScore != null) {
                        match.setHomeScore(homeScore);
                    }
                    if (awayScore != null) {
                        match.setAwayScore(awayScore);
                    }

                    matchRepository.save(match);

                    if (wasNotFinished) {
                        predictionService.calculatePoints(match.getId());
                    }

                    updatedCount++;
                } else if (
                        normalized.contains("live")
                                || normalized.contains("in play")
                                || normalized.contains("1st quarter")
                                || normalized.contains("2nd quarter")
                                || normalized.contains("3rd quarter")
                                || normalized.contains("4th quarter")
                ) {
                    match.setStatus(Match.MatchStatus.LIVE);

                    if (homeScore != null) {
                        match.setHomeScore(homeScore);
                    }
                    if (awayScore != null) {
                        match.setAwayScore(awayScore);
                    }

                    matchRepository.save(match);
                    updatedCount++;
                } else {
                    match.setStatus(Match.MatchStatus.UPCOMING);
                    matchRepository.save(match);
                }
            }
        }

        return updatedCount;
    }


    private Integer parseInteger(Object obj) {
        if (obj == null) {
            return null;
        }

        String value = obj.toString().trim();
        if (value.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String value(Object obj) {
        return obj != null ? obj.toString() : null;
    }
}