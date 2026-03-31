package com.prediction.platform.prediction;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/matches/{matchId}/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping
    public ResponseEntity<Prediction> createPrediction(
            @PathVariable Long matchId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Prediction prediction) {

        prediction.getMatch().setId(matchId);
        prediction.getUser().setId(userId);
        return ResponseEntity.ok(predictionService.createPrediction(prediction));
    }

    @GetMapping("/me")
    public ResponseEntity<Prediction> getMyPrediction(
            @PathVariable Long matchId,
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(predictionService.getMyPrediction(userId, matchId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Prediction> updatePrediction(
            @PathVariable Long matchId,
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Prediction prediction) {

        return ResponseEntity.ok(predictionService.updatePrediction(id, prediction));
    }

    @PostMapping("/calculate")
    public ResponseEntity<Void> calculatePoints(
            @PathVariable Long matchId,
            @RequestHeader("X-User-Role") String userRole) {

        if (!userRole.equals("ADMIN")) {
            return ResponseEntity.status(403).build();
        }

        predictionService.calculatePoints(matchId);
        return ResponseEntity.ok().build();
    }
}