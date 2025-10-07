package com.team2.Residential_Maintenance_Management_System;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /*
     * Spring discovers this controller via the @Controller stereotype and use
     * the @GetMapping annotations to map incoming HTTP GET requests to the
     * methods below. Each method returns the logical view name; the configured
     * view resolver turns that into the corresponding HTML template under
     * src/main/resources/views.
     */
    @GetMapping("/")
    public String loginSignup() {
        // Returning "loginsignup" loads loginsignup.html as the landing page.
        return "loginsignup";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        // Returning "dashboard" loads dashboard.html when the route is hit.
        return "dashboard";
    }

//    EXAMPLE CODE
//    @GetMapping("/")
//    public String index(Model model) {
//        model.addAttribute("msg", "Welcome to the Residential Maintenance Management System.");
//        return "index"; // looks for views/index.html
//    }
}
