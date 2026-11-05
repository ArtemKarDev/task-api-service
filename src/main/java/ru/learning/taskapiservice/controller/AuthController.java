package ru.learning.taskapiservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.learning.taskapiservice.dto.LoginRequest;
import ru.learning.taskapiservice.dto.TokenResponse;
import ru.learning.taskapiservice.entity.UserAccount;
import ru.learning.taskapiservice.exceptions.InvalidCredentialsException;
import ru.learning.taskapiservice.security.TokenService;
import ru.learning.taskapiservice.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        UserAccount user = authService.authenticate(request.username(), request.password())
                .orElseThrow(InvalidCredentialsException::new);
        return tokenService.issue(user);
    }
}