package ru.learning.taskapiservice.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.learning.taskapiservice.exceptions.DirectoryUnavailableException;

@Slf4j
@Component
public class EmployeeDirectoryClient {

    private final RestClient restClient;
    private final DirectoryTokenProvider tokenProvider;
    private final DirectoryProperties props;

    public EmployeeDirectoryClient(@Qualifier("directoryRestClient") RestClient restClient,
                                   DirectoryTokenProvider tokenProvider,
                                   DirectoryProperties props) {
        this.restClient = restClient;
        this.tokenProvider = tokenProvider;
        this.props = props;
    }

    public boolean exists(String login) {
        try {
            return lookup(login);
        } catch (HttpClientErrorException.Unauthorized firstAttempt) {
            tokenProvider.invalidate();
            try {
                return lookup(login);
            } catch (RestClientException secondAttempt) {
                throw unavailable(secondAttempt);
            }
        } catch (RestClientException e) {
            throw unavailable(e);
        }
    }

    private boolean lookup(String login) {
        try {
            restClient.get()
                    .uri(props.baseUrl() + "/employees/{login}", login)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getToken())
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    private DirectoryUnavailableException unavailable(RestClientException cause) {
        log.warn("Справочник сотрудников недоступен: {}", cause.getMessage());
        return new DirectoryUnavailableException("Справочник сотрудников недоступен", cause);
    }
}