package com.prediction.platform.external;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class ApiSportsController {

    private final ApiSportsService apiSportsService;

    @PostMapping("/games")
    public ResponseEntity<String> syncGames(
            @RequestHeader("X-User-Role") String userRole,
            @RequestParam String leagueId,
            @RequestParam String season) {

        if (!userRole.equals("ADMIN")) {
            return ResponseEntity.status(403).body("Access denied");
        }

        apiSportsService.syncGames(leagueId, season);
        return ResponseEntity.ok("Rungtynės sėkmingai sinchronizuotos!");
    }
}