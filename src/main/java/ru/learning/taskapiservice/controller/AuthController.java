package ru.learning.taskapiservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.learning.taskapiservice.dto.ApiErrorResponse;
import ru.learning.taskapiservice.dto.LoginRequest;
import ru.learning.taskapiservice.dto.TokenResponse;
import ru.learning.taskapiservice.entity.UserAccount;
import ru.learning.taskapiservice.exceptions.InvalidCredentialsException;
import ru.learning.taskapiservice.security.TokenService;
import ru.learning.taskapiservice.service.AuthService;

@Tag(name = "Auth", description = "Log in to get an access token")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    @Operation(summary = "Log in",
            description = "Exchanges username and password for a short-lived access token (JWT). "
                    + "Pass it in the header \"Authorization: Bearer <token>\"")
    @SecurityRequirements
    @ApiResponse(responseCode = "200", description = "Token issued")
    @ApiResponse(responseCode = "400", description = "Request validation failed",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Invalid username or password",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        UserAccount user = authService.authenticate(request.username(), request.password())
                .orElseThrow(InvalidCredentialsException::new);
        return tokenService.issue(user);
    }
}