package com.prediction.platform.match;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    public ResponseEntity<List<Match>> getMatches(
            @RequestParam(required = false) Long leagueId,
            @RequestParam(required = false) String status) {

        if (leagueId != null && status != null) {
            return ResponseEntity.ok(matchService.getMatchesByLeagueAndStatus(
                    leagueId, Match.MatchStatus.valueOf(status.toUpperCase())));
        }
        if (leagueId != null) {
            return ResponseEntity.ok(matchService.getMatchesByLeague(leagueId));
        }
        if (status != null) {
            return ResponseEntity.ok(matchService.getMatchesByStatus(
                    Match.MatchStatus.valueOf(status.toUpperCase())));
        }
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Match> getMatch(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchById(id));
    }

    @PostMapping
    public ResponseEntity<Match> createMatch(@RequestBody Match match) {
        return ResponseEntity.ok(matchService.createMatch(match));
    }

    @PatchMapping("/{id}/score")
    public ResponseEntity<Match> updateScore(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String userRole,
            @RequestBody MatchResultRequest request) {

        if (!userRole.equals("ADMIN")) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(matchService.updateMatchResult(
                id,
                request.getHomeScore(),
                request.getAwayScore(),
                request.getMvpPlayer(),
                request.getFirstScorer()));
    }
}