package com.prediction.platform.match;

import com.prediction.platform.config.SportsDBService;
import com.prediction.platform.dto.SyncResponse;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final SportsDBService sportsDbService;

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

    /**
     * Admino įrankis rankiniu būdu atnaujinti rezultatą.
     * Naudojamas quarterResults vietoj mvpPlayer ir firstScorer.
     */
    @PatchMapping("/{id}/score")
    public ResponseEntity<Match> updateScore(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String userRole,
            @RequestBody MatchResultRequest request) {

        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(403).build();
        }

        // Kviečiame atnaujintą MatchService metodą
        return ResponseEntity.ok(matchService.updateMatchResult(
                id,
                request.getHomeScore(),
                request.getAwayScore(),
                request.getQuarterResults())); // Perduodame naują lauką
    }

    @PostMapping("/sync-upcoming")
    public ResponseEntity<SyncResponse> syncUpcomingMatches(
            @RequestParam String leagueId,
            Authentication authentication) {

        // LAIKINAI UŽKOMENTUOK ŠITĄ BLOKĄ:
    /*
    if (authentication == null) {
        return ResponseEntity.status(401).build();
    }
    */

        int savedCount = sportsDbService.syncUpcomingMatches(leagueId);

        return ResponseEntity.ok(
                new SyncResponse("Matches synced successfully", savedCount)
        );
    }

    @PostMapping("/refresh-results")
    public ResponseEntity<SyncResponse> refreshMatchResults(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        int updatedCount = sportsDbService.refreshSavedMatchesResults();

        return ResponseEntity.ok(
                new SyncResponse("Match results refreshed successfully", updatedCount)
        );
    }
}