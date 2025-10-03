package com.team2.Residential_Maintenance_Management_System;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String loginSignup() {
        return "loginsignup";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}
