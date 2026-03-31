package com.prediction.platform.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.List;
import java.util.Map;

@Component
public class ApiSportsClient {

    @Value("${api-sports.key}")
    private String apiKey;

    @Value("${api-sports.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-apisports-key", apiKey);
        return headers;
    }

    public Map<String, Object> getGames(String league, String season) {
        String url = baseUrl + "/games?league=" + league + "&season=" + season;
        HttpEntity<Void> entity = new HttpEntity<>(getHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class);
        return response.getBody();
    }

    public Map<String, Object> getTeams(String league, String season) {
        String url = baseUrl + "/teams?league=" + league + "&season=" + season;
        HttpEntity<Void> entity = new HttpEntity<>(getHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class);
        return response.getBody();
    }
}