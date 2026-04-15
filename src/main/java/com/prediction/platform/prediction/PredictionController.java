package com.prediction.platform.prediction;


import com.prediction.platform.user.User;
import com.prediction.platform.user.UserRepository;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;
    private final UserRepository userRepository;

    // Sukurti spėjimą
    @PostMapping("/api/matches/{matchId}/predictions")
    public ResponseEntity<Prediction> createPrediction(
            @PathVariable Long matchId,
            Authentication authentication,
            @RequestBody Prediction prediction) {

        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (prediction.getMatch() == null) {
            prediction.setMatch(new com.prediction.platform.match.Match());
        }
        if (prediction.getUser() == null) {
            prediction.setUser(new User());
        }

        prediction.getMatch().setId(matchId);
        prediction.getUser().setId(currentUser.getId());

        return ResponseEntity.ok(predictionService.createPrediction(prediction));
    }

    // Gauti savo spėjimą konkrečioms rungtynėms
    @GetMapping("/api/matches/{matchId}/predictions/me")
    public ResponseEntity<Prediction> getMyPrediction(
            @PathVariable Long matchId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Prediction p = predictionService.getMyPrediction(currentUser.getId(), matchId);
        if (p == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(p);
    }

    // Atnaujinti spėjimą
    @PatchMapping("/api/matches/{matchId}/predictions/{id}")
    public ResponseEntity<Prediction> updatePrediction(
            @PathVariable Long matchId,
            @PathVariable Long id,
            Authentication authentication,
            @RequestBody Prediction prediction) {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(predictionService.updatePrediction(id, currentUser.getId(), prediction));
    }

    // Ištrinti spėjimą
    @DeleteMapping("/api/matches/{matchId}/predictions/{id}")
    public ResponseEntity<Void> deletePrediction(
            @PathVariable Long matchId,
            @PathVariable Long id,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        predictionService.deletePrediction(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    // Visi vartotojo spėjimai
    @GetMapping("/api/users/{userId}/predictions")
    public ResponseEntity<List<Prediction>> getUserPredictions(
            @PathVariable Long userId) {
        return ResponseEntity.ok(predictionService.getUserPredictions(userId));
    }

    // Skaičiuoti taškus (admin)
    // Skaičiuoti taškus (Sutvarkytas, kad priimtų rankinį headerį)
    @PostMapping("/api/matches/{matchId}/predictions/calculate")
    public ResponseEntity<String> calculatePoints(
            @PathVariable Long matchId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            Authentication authentication) {

        // 1. Patikriname, ar yra Authentication (tokenas)
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        // 2. Tikriname rolę: arba iš tokeno (ROLE_ADMIN), arba iš tavo rankinio Headerio (ADMIN)
        boolean hasAdminRoleInToken = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        boolean hasAdminHeader = "ADMIN".equals(userRole);

        // Jei nei tokene nėra ADMIN, nei headerio nepridėjai - tada meta 403
        if (!hasAdminRoleInToken && !hasAdminHeader) {
            return ResponseEntity.status(403).body("Forbidden: Reikalinga ADMIN role arba X-User-Role: ADMIN headeris");
        }

        predictionService.calculatePoints(matchId);
        return ResponseEntity.ok("Taskai sekmingai apskaiciuoti!");
    }
}