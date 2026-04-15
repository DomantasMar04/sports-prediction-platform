package com.prediction.platform.token;

import com.prediction.platform.dto.AuthorizationResponse;
import com.prediction.platform.dto.LoginRequest;
import com.prediction.platform.dto.RegisterRequest;
import com.prediction.platform.user.User;
import com.prediction.platform.user.UserRepository;
import com.prediction.platform.dto.UserResponse;
import com.prediction.platform.dto.ProfileResponse;
import com.prediction.platform.dto.PredictionSummaryResponse;
import com.prediction.platform.prediction.Prediction;
import com.prediction.platform.prediction.PredictionRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtService;
    private final PredictionRepository predictionRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenService jwtService, PredictionRepository predictionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.predictionRepository = predictionRepository;
    }

    public AuthorizationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already in use");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.USER);

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(
                savedUser.getEmail(),
                savedUser.getId(),
                savedUser.getRole().name()
        );

        return new AuthorizationResponse(
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );
    }

    public AuthorizationResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!passwordMatches) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getId(),
                user.getRole().name()
        );

        return new AuthorizationResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    public ProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Prediction> userPredictions = predictionRepository.findByUser(user);

        userPredictions.sort(Comparator.comparing(Prediction::getCreatedAt).reversed());

        int totalScore = userPredictions.stream()
                .map(Prediction::getPointsEarned)
                .filter(points -> points != null)
                .mapToInt(Integer::intValue)
                .sum();

        List<PredictionSummaryResponse> predictionResponses = userPredictions.stream()
                .map(prediction -> new PredictionSummaryResponse(
                        prediction.getId(),
                        prediction.getMatch().getId(),
                        prediction.getMatch().getHomeTeam().getName(),
                        prediction.getMatch().getAwayTeam().getName(),
                        prediction.getMatch().getStartTime(),
                        prediction.getMatch().getStatus().name(),
                        prediction.getPredictedWinner(),
                        prediction.getPredictedHomeScore(),
                        prediction.getPredictedAwayScore(),
                        prediction.getPredictedMostQuartersWinner(), // 10-as argumentas (NAUJAS)
                        prediction.getMatch().getHomeScore(),
                        prediction.getMatch().getAwayScore(),
                        prediction.getMatch().getQuarterResults(),   // 13-as argumentas (NAUJAS)
                        prediction.getPointsEarned(),
                        prediction.getIsCalculated(),
                        prediction.getBreakdown(),
                        prediction.getCreatedAt()
                ))
                .toList();

        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                totalScore,
                userPredictions.size(),
                predictionResponses
        );
    }

    @Transactional
    public void deleteCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        predictionRepository.deleteByUser(user);
        userRepository.delete(user);
    }
}