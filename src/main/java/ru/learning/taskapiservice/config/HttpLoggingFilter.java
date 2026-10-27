package ru.learning.taskapiservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Пишет в лог метод, адрес, тела запроса и ответа, статус и время для REST-запросов (/api/**).
 * Включается параметром app.http-log.enabled=true (на стендах через переменную HTTP_LOG_ENABLED).
 */
@Component
@ConditionalOnProperty(name = "app.http-log.enabled", havingValue = "true")
public class HttpLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(HttpLoggingFilter.class);
    private static final int MAX_BODY_LENGTH = 2000;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, MAX_BODY_LENGTH);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            log.info("HTTP {} {} -> {} ({} ms)\n  request body:  {}\n  response body: {}",
                    request.getMethod(),
                    uriWithQuery(request),
                    wrappedResponse.getStatus(),
                    System.currentTimeMillis() - start,
                    body(wrappedRequest.getContentAsByteArray()),
                    body(wrappedResponse.getContentAsByteArray()));
            // Без этого вызова клиент получит пустой ответ
            wrappedResponse.copyBodyToResponse();
        }
    }

    private String uriWithQuery(HttpServletRequest request) {
        String query = request.getQueryString();
        return query == null ? request.getRequestURI() : request.getRequestURI() + "?" + query;
    }

    private String body(byte[] content) {
        if (content.length == 0) {
            return "-";
        }
        String text = new String(content, StandardCharsets.UTF_8);
        return text.length() > MAX_BODY_LENGTH ? text.substring(0, MAX_BODY_LENGTH) + "…(обрезано)" : text;
    }
}