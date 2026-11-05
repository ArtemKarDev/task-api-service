package ru.learning.taskapiservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.learning.taskapiservice.dto.CreateUserRequest;
import ru.learning.taskapiservice.dto.ResetPasswordRequest;
import ru.learning.taskapiservice.dto.UpdateUserRequest;
import ru.learning.taskapiservice.dto.UserResponse;
import ru.learning.taskapiservice.service.UserService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return UserResponse.from(userService.createUser(request.username(), request.password(), request.role()));
    }

    @GetMapping
    public List<UserResponse> list() {
        return userService.listUsers().stream().map(UserResponse::from).toList();
    }

    @PatchMapping("/{id}")
    public UserResponse update(@PathVariable Long id,
                               @RequestBody UpdateUserRequest request,
                               Principal principal) {
        return UserResponse.from(userService.updateUser(id, request.enabled(), request.role(), principal.getName()));
    }

    @PutMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request.newPassword());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Principal principal) {
        userService.deleteUser(id, principal.getName());
    }
}