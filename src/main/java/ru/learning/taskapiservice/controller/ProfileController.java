package ru.learning.taskapiservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.learning.taskapiservice.dto.PasswordForm;
import ru.learning.taskapiservice.exceptions.InvalidRequestException;
import ru.learning.taskapiservice.exceptions.PasswordChangeException;
import ru.learning.taskapiservice.service.UserService;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping("/password")
    public String showForm(Model model) {
        model.addAttribute("passwordForm", new PasswordForm());
        return "profile-password";
    }

    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute("passwordForm") PasswordForm form,
                                 BindingResult bindingResult,
                                 Principal principal,
                                 RedirectAttributes ra) {
        if (!bindingResult.hasFieldErrors("newPassword") && !bindingResult.hasFieldErrors("confirmPassword")
                && !form.getNewPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Пароли не совпадают");
        }
        if (bindingResult.hasErrors()) {
            return "profile-password";
        }

        try {
            userService.changePassword(principal.getName(), form.getCurrentPassword(), form.getNewPassword());
        } catch (PasswordChangeException e) {
            switch (e.getReason()) {
                case WRONG_CURRENT -> bindingResult.rejectValue("currentPassword", "wrong",
                        "Текущий пароль указан неверно");
                case SAME_AS_CURRENT -> bindingResult.rejectValue("newPassword", "same",
                        "Новый пароль должен отличаться от текущего");
            }
            return "profile-password";
        } catch (InvalidRequestException e) {
            bindingResult.rejectValue("newPassword", "tooLong", "Пароль слишком длинный: не больше 72 байт");
            return "profile-password";
        }

        ra.addFlashAttribute("message", "Пароль изменён");
        return "redirect:/tasks";
    }
}