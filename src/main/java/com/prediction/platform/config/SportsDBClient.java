package com.prediction.platform.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SportsDBClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${sportsdb.base-url}")
    private String baseUrl;

    @Value("${sportsdb.api-key}")
    private String apiKey;

    public Map<String, Object> getNextLeagueEvents(String leagueId) {
        String url = String.format(
                "%s/%s/eventsnextleague.php?id=%s",
                baseUrl,
                apiKey,
                leagueId
        );

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        ).getBody();
    }

    public Map<String, Object> lookupEventById(String eventId) {
        String url = String.format(
                "%s/%s/lookupevent.php?id=%s",
                baseUrl,
                apiKey,
                eventId
        );

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        ).getBody();
    }
}
