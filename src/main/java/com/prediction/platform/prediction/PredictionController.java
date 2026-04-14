package com.prediction.platform.prediction;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    // Sukurti spėjimą
    @PostMapping("/api/matches/{matchId}/predictions")
    public ResponseEntity<Prediction> createPrediction(
            @PathVariable Long matchId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Prediction prediction) {
        prediction.getMatch().setId(matchId);
        prediction.getUser().setId(userId);
        return ResponseEntity.ok(predictionService.createPrediction(prediction));
    }

    // Gauti savo spėjimą konkrečioms rungtynėms
    @GetMapping("/api/matches/{matchId}/predictions/me")
    public ResponseEntity<Prediction> getMyPrediction(
            @PathVariable Long matchId,
            @RequestHeader("X-User-Id") Long userId) {
        Prediction p = predictionService.getMyPrediction(userId, matchId);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(p);
    }

    // Atnaujinti spėjimą
    @PatchMapping("/api/matches/{matchId}/predictions/{id}")
    public ResponseEntity<Prediction> updatePrediction(
            @PathVariable Long matchId,
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Prediction prediction) {
        return ResponseEntity.ok(predictionService.updatePrediction(id, userId, prediction));
    }

    // Ištrinti spėjimą
    @DeleteMapping("/api/matches/{matchId}/predictions/{id}")
    public ResponseEntity<Void> deletePrediction(
            @PathVariable Long matchId,
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        predictionService.deletePrediction(id, userId);
        return ResponseEntity.noContent().build();
    }

    // Visi vartotojo spėjimai
    @GetMapping("/api/users/{userId}/predictions")
    public ResponseEntity<List<Prediction>> getUserPredictions(
            @PathVariable Long userId) {
        return ResponseEntity.ok(predictionService.getUserPredictions(userId));
    }

    // Skaičiuoti taškus (admin)
    @PostMapping("/api/matches/{matchId}/predictions/calculate")
    public ResponseEntity<Void> calculatePoints(
            @PathVariable Long matchId,
            @RequestHeader("X-User-Role") String userRole) {
        if (!userRole.equals("ADMIN")) return ResponseEntity.status(403).build();
        predictionService.calculatePoints(matchId);
        return ResponseEntity.ok().build();
    }
}