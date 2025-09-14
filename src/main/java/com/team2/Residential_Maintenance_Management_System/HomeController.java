package com.team2.Residential_Maintenance_Management_System;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("msg", "Welcome to the Residential Maintenance Management System.");
        return "index"; // looks for views/index.html
    }
}
