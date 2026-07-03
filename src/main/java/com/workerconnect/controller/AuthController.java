package com.workerconnect.controller;

import com.workerconnect.dto.UserRegistrationDto;
import com.workerconnect.dto.WorkerRegistrationDto;
import com.workerconnect.service.AuthService;
import com.workerconnect.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CategoryService categoryService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout, Model model) {
        if (error != null) model.addAttribute("error", "Invalid email or password");
        if (logout != null) model.addAttribute("message", "You have been logged out successfully");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userDto") UserRegistrationDto dto,
                               BindingResult result, RedirectAttributes ra, Model model) {
        if (result.hasErrors()) return "auth/register";
        try {
            authService.registerUser(dto);
            ra.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/register/worker")
    public String registerWorkerPage(Model model) {
        model.addAttribute("workerDto", new WorkerRegistrationDto());
        model.addAttribute("categories", categoryService.getActiveCategories());
        return "auth/register-worker";
    }

    @PostMapping("/register/worker")
    public String registerWorker(@Valid @ModelAttribute("workerDto") WorkerRegistrationDto dto,
                                 BindingResult result, RedirectAttributes ra, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getActiveCategories());
            return "auth/register-worker";
        }
        try {
            
            authService.registerWorker(dto);
            ra.addFlashAttribute("success", "Registration submitted! Await admin approval.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", categoryService.getActiveCategories());
            return "auth/register-worker";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, RedirectAttributes ra) {
        try {
            authService.initiatePasswordReset(email);
            ra.addFlashAttribute("success", "Password reset link sent to your email.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                RedirectAttributes ra) {
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Passwords do not match");
            return "redirect:/auth/reset-password?token=" + token;
        }
        try {
            authService.resetPassword(token, newPassword);
            ra.addFlashAttribute("success", "Password reset successfully. Please login.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/reset-password?token=" + token;
        }
    }
}
