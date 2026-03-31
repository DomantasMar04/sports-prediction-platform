package com.prediction.platform.external;

import com.prediction.platform.match.Match;
import com.prediction.platform.match.MatchRepository;
import com.prediction.platform.team.Team;
import com.prediction.platform.team.TeamRepository;
import com.prediction.platform.league.League;
import com.prediction.platform.league.LeagueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApiSportsService {

    private final ApiSportsClient apiSportsClient;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;

    public void syncGames(String leagueId, String season) {
        Map<String, Object> response = apiSportsClient.getGames(leagueId, season);
        List<Map<String, Object>> games = (List<Map<String, Object>>) response.get("response");

        League league = leagueRepository.findByExternalId(leagueId)
                .orElseGet(() -> {
                    League l = new League();
                    l.setName("League " + leagueId);
                    l.setType(League.LeagueType.REAL);
                    l.setExternalId(leagueId);
                    return leagueRepository.save(l);
                });

        for (Map<String, Object> game : games) {
            Map<String, Object> teams = (Map<String, Object>) game.get("teams");
            Map<String, Object> homeTeamData = (Map<String, Object>) teams.get("home");
            Map<String, Object> awayTeamData = (Map<String, Object>) teams.get("away");
            Map<String, Object> scores = (Map<String, Object>) game.get("scores");
            Map<String, Object> gameDate = (Map<String, Object>) game.get("date");

            String externalId = String.valueOf(game.get("id"));

            if (matchRepository.findByExternalId(externalId).isPresent()) continue;

            Team homeTeam = getOrCreateTeam(homeTeamData, league);
            Team awayTeam = getOrCreateTeam(awayTeamData, league);

            Match match = new Match();
            match.setHomeTeam(homeTeam);
            match.setAwayTeam(awayTeam);
            match.setLeague(league);
            match.setExternalId(externalId);
            match.setStatus(Match.MatchStatus.UPCOMING);

            if (gameDate != null) {
                String dateStr = (String) gameDate.get("start");
                if (dateStr != null) {
                    match.setStartTime(LocalDateTime.parse(
                            dateStr.substring(0, 19),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
            }

            if (scores != null) {
                Map<String, Object> home = (Map<String, Object>) scores.get("home");
                Map<String, Object> away = (Map<String, Object>) scores.get("away");
                if (home != null && home.get("total") != null) {
                    match.setHomeScore((Integer) home.get("total"));
                    match.setAwayScore((Integer) away.get("total"));
                    match.setStatus(Match.MatchStatus.FINISHED);
                }
            }

            matchRepository.save(match);
        }
    }

    private Team getOrCreateTeam(Map<String, Object> teamData, League league) {
        String externalId = String.valueOf(teamData.get("id"));
        return teamRepository.findByExternalId(externalId)
                .orElseGet(() -> {
                    Team team = new Team();
                    team.setName((String) teamData.get("name"));
                    team.setExternalId(externalId);
                    team.setLeague(league);
                    return teamRepository.save(team);
                });
    }
}