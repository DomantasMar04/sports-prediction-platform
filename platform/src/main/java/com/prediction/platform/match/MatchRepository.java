package com.prediction.platform.match;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByLeagueId(Long leagueId);

    List<Match> findByStatus(Match.MatchStatus status);

    List<Match> findByLeagueIdAndStatus(Long leagueId, Match.MatchStatus status);
    Optional<Match> findByExternalId(String externalId);
}