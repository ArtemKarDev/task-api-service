package ru.learning.taskapiservice.dto;

import ru.learning.taskapiservice.entity.Role;
import ru.learning.taskapiservice.entity.UserAccount;

public record UserResponse(Long id, String username, Role role, boolean enabled) {

    public static UserResponse from(UserAccount user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.isEnabled());
    }
}