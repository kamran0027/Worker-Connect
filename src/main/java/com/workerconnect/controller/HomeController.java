package com.workerconnect.controller;

import com.workerconnect.service.CategoryService;
import com.workerconnect.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final WorkerService workerService;
    private final CategoryService categoryService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("topWorkers", workerService.findTopRatedWorkers());
        return "home";
    }

    // ── Public Worker Search ─────────────────────────────────────────────────

    @GetMapping("/workers/search")
    public String searchWorkers(@RequestParam(required = false) String city,
                                @RequestParam(required = false) String profession,
                                @RequestParam(required = false) BigDecimal minPrice,
                                @RequestParam(required = false) BigDecimal maxPrice,
                                @RequestParam(required = false) Double minRating,
                                Model model) {
        model.addAttribute("workers", workerService.searchWorkers(city, profession, minPrice, maxPrice, minRating));
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("city", city);
        model.addAttribute("profession", profession);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("minRating", minRating);
        return "workers/search";
    }

    @GetMapping("/workers/{id}")
    public String workerDetail(@PathVariable Long id, Model model) {
        model.addAttribute("worker", workerService.findById(id));
        return "workers/detail";
    }

    @GetMapping("/workers/category/{categoryId}")
    public String workersByCategory(@PathVariable Long categoryId, Model model) {
        model.addAttribute("workers", workerService.findByCategoryId(categoryId));
        model.addAttribute("category", categoryService.findById(categoryId));
        model.addAttribute("categories", categoryService.getActiveCategories());
        return "workers/search";
    }

    @GetMapping("/error/403")
    public String forbidden() {
        return "error/403";
    }
}
