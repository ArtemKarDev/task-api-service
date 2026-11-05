package ru.learning.taskapiservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.learning.taskapiservice.dto.ChangePasswordRequest;
import ru.learning.taskapiservice.service.UserService;

import java.security.Principal;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class CurrentUserController {

    private final UserService userService;

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request, Principal principal) {
        userService.changePassword(principal.getName(), request.currentPassword(), request.newPassword());
    }
}