package ru.learning.taskapiservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaticPageController {
    @GetMapping("/about") public String about() { return "about"; }
    @GetMapping("/login") public String login() { return "login"; }
}
