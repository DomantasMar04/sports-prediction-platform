package com.prediction.platform.leaderboard;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/api/leagues/{leagueId}/leaderboard")
    public ResponseEntity<List<Leaderboard>> getLeagueLeaderboard(
            @PathVariable Long leagueId,
            @RequestHeader("X-League-Type") String leagueType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(leaderboardService.getLeagueLeaderboard(leagueId));
    }

    @GetMapping("/api/leaderboard/global")
    public ResponseEntity<List<Leaderboard>> getGlobalLeaderboard(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(leaderboardService.getGlobalLeaderboard());
    }
}