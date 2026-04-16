package com.prediction.platform.match;

import com.prediction.platform.prediction.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final PredictionService predictionService;

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

    public Match updateMatchResult(Long id, Integer homeScore, Integer awayScore, String quarterResults) {
        Match match = getMatchById(id);

        match.setHomeScore(homeScore);
        match.setAwayScore(awayScore);
        match.setQuarterResults(quarterResults);
        match.setStatus(Match.MatchStatus.FINISHED);

        Match savedMatch = matchRepository.save(match);

        // 2. AUTOMATIZACIJA: Vos tik rungtynės baigiasi, iškart paskaičiuojame taškus visiems
        try {
            predictionService.calculatePoints(id);
            System.out.println("Taškai sėkmingai paskaičiuoti rungtynėms: " + id);
        } catch (Exception e) {
            // Loguojame klaidą, bet leidžiame rungtynėms išsisaugoti
            System.err.println("Nepavyko automatiškai paskaičiuoti taškų: " + e.getMessage());
        }

        return savedMatch;
    }
}