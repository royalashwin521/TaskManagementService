package com.TMP.tms.service;

import com.TMP.tms.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
public class AuthServiceClient {

    private final RestClient restClient;

    public AuthServiceClient(RestClient.Builder restClientBuilder, 
                             @Value("${auth.service.url}") String authServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(authServiceUrl).build();
    }

    /**
     * Calls the Auth Service to verify if a user exists.
     */
    public void validateUserExists(UUID userId, String authHeader) {
        restClient.get()
                .uri("/api/v1/users/{id}", userId)
                // Forward the exact Authorization header (Bearer + token)
                .header("Authorization", authHeader)
                .retrieve()
                // If Auth Service returns 404 (User Not Found) or 401/403
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new BusinessException("Validation failed: Assignee User ID is invalid or does not exist.");
                })
                // If Auth Service crashes or is offline
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new BusinessException("Auth Service is currently unavailable. Cannot assign task.");
                })
                .toBodilessEntity(); // We just want a successful 200 OK status, we ignore the JSON body
    }
}