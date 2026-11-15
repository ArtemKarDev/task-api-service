package ru.learning.taskapiservice.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;

@Component
public class DirectoryTokenProvider {

    private static final Duration RESERVE = Duration.ofSeconds(30);
    private static final long DEFAULT_TTL_SECONDS = 60;

    private final RestClient restClient;
    private final DirectoryProperties props;

    private String token;
    private Instant expiresAt = Instant.EPOCH;

    public DirectoryTokenProvider(@Qualifier("directoryRestClient") RestClient restClient,
                                  DirectoryProperties props) {
        this.restClient = restClient;
        this.props = props;
    }

    public synchronized String getToken() {
        if (token == null || Instant.now().isAfter(expiresAt.minus(RESERVE))) {
            refresh();
        }
        return token;
    }

    /** Забыть токен: например, справочник ответил 401, и нужен новый. */
    public synchronized void invalidate() {
        token = null;
    }

    private void refresh() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", props.clientId());
        form.add("client_secret", props.clientSecret());

        TokenResponse response = restClient.post()
                .uri(props.tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(TokenResponse.class);

        if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
            throw new RestClientException("Сервер авторизации не вернул токен");
        }
        long ttl = response.expiresIn() > 0 ? response.expiresIn() : DEFAULT_TTL_SECONDS;
        this.token = response.accessToken();
        this.expiresAt = Instant.now().plusSeconds(ttl);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TokenResponse(@JsonProperty("access_token") String accessToken,
                         @JsonProperty("expires_in") long expiresIn) {
    }
}
