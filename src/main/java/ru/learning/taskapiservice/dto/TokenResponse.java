package ru.learning.taskapiservice.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}