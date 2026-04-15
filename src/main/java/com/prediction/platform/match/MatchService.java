package com.prediction.platform.match;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public List<Match> getMatchesByLeague(Long leagueId) {
        return matchRepository.findByLeagueId(leagueId);
    }

    public List<Match> getMatchesByStatus(Match.MatchStatus status) {
        return matchRepository.findByStatus(status);
    }

    public List<Match> getMatchesByLeagueAndStatus(Long leagueId, Match.MatchStatus status) {
        return matchRepository.findByLeagueIdAndStatus(leagueId, status);
    }

    public Match getMatchById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found: " + id));
    }

    public Match createMatch(Match match) {
        return matchRepository.save(match);
    }

    // ATNAUJINTAS METODAS: Pašalintas MVP/FirstScorer, pridėtas quarterResults
    public Match updateMatchResult(Long id, Integer homeScore, Integer awayScore, String quarterResults) {
        Match match = getMatchById(id);

        match.setHomeScore(homeScore);
        match.setAwayScore(awayScore);
        match.setQuarterResults(quarterResults); // Išsaugome ketvirčių duomenis
        match.setStatus(Match.MatchStatus.FINISHED);

        return matchRepository.save(match);
    }
}