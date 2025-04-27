package com.choi.springmall2.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController {
    @RequestMapping("/error/custom")
    public String handleCustomError(HttpServletRequest request, Model model) {
        model.addAttribute("message", request.getAttribute("message"));
        model.addAttribute("exception", request.getAttribute("exception"));
        model.addAttribute("status", request.getAttribute("status"));
        model.addAttribute("path", request.getAttribute("path"));
        return "error/error"; // templates/error/error.html
    }
}
