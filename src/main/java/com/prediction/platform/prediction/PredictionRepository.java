package com.prediction.platform.prediction;

import com.prediction.platform.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    List<Prediction> findByUserId(Long userId);

    List<Prediction> findByUser(User user);

    List<Prediction> findByMatchId(Long matchId);

    Optional<Prediction> findByUserIdAndMatchId(Long userId, Long matchId);

    List<Prediction> findByIsCalculatedFalse();

    void deleteByUser(User user);
}