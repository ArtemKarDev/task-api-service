package ru.learning.taskapiservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.learning.taskapiservice.security.DemoProperties;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class StaticPageController {

    private final DemoProperties demo;

    @GetMapping("/")
    public String root(Principal principal) {
        return principal == null ? "redirect:/about" : "redirect:/tasks";
    }

    @GetMapping("/about")
    public String about(Model model) {
        if (demo.active()) {
            model.addAttribute("demo", demo);
        }
        return "about";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}