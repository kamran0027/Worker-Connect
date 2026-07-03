package com.workerconnect.controller;

import com.workerconnect.enums.BookingStatus;
import com.workerconnect.enums.Role;
import com.workerconnect.model.*;
import com.workerconnect.repository.BookingRepository;
import com.workerconnect.repository.UserRepository;
import com.workerconnect.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final WorkerService workerService;
    private final BookingService bookingService;
    private final AgreementService agreementService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;
    private final CategoryService categoryService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userRepository.countByRole(Role.ROLE_USER));
        model.addAttribute("totalWorkers", userRepository.countByRole(Role.ROLE_WORKER));
        model.addAttribute("pendingWorkers", workerService.findPendingWorkers().size());
        model.addAttribute("activeBookings", bookingRepository.countByStatus(BookingStatus.ACCEPTED));
        model.addAttribute("completedBookings", bookingRepository.countByStatus(BookingStatus.COMPLETED));
        model.addAttribute("totalRevenue", paymentService.getTotalRevenue());
        model.addAttribute("recentBookings", bookingService.getAllBookings().stream().limit(5).toList());
        model.addAttribute("topWorkers", workerService.findTopRatedWorkers().stream().limit(5).toList());
        return "admin/dashboard";
    }

    // ── User Management ───────────────────────────────────────────────────────

    @GetMapping("/users")
    public String users(@RequestParam(required = false) String query, Model model) {
        List<User> users = (query != null && !query.isBlank())
                ? userService.searchUsers(query)
                : userRepository.findByRole(Role.ROLE_USER);
        model.addAttribute("users", users);
        model.addAttribute("query", query);
        return "admin/users/list";
    }

    @GetMapping("/users/{id}")
    public String userDetail(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        model.addAttribute("bookings", bookingService.getBookingsByUser(id));
        return "admin/users/detail";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes ra) {
        userService.toggleUserStatus(id);
        ra.addFlashAttribute("success", "User status updated");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        userService.deleteUser(id);
        ra.addFlashAttribute("success", "User deleted");
        return "redirect:/admin/users";
    }

    // ── Worker Management ─────────────────────────────────────────────────────

    @GetMapping("/workers")
    public String workers(@RequestParam(required = false) String status, Model model) {
        List<Worker> workers = "pending".equals(status)
                ? workerService.findPendingWorkers()
                : workerService.findAllWorkers();
        model.addAttribute("workers", workers);
        model.addAttribute("status", status);
        return "admin/workers/list";
    }

    @GetMapping("/workers/{id}")
    public String workerDetail(@PathVariable Long id, Model model) {
        model.addAttribute("worker", workerService.findById(id));
        model.addAttribute("bookings", bookingService.getBookingsByWorker(id));
        model.addAttribute("reviews", reviewService.getWorkerReviews(id));
        return "admin/workers/detail";
    }

    @PostMapping("/workers/{id}/approve")
    public String approveWorker(@PathVariable Long id, RedirectAttributes ra) {
        workerService.approveWorker(id);
        ra.addFlashAttribute("success", "Worker approved and notified via email");
        return "redirect:/admin/workers?status=pending";
    }

    @PostMapping("/workers/{id}/reject")
    public String rejectWorker(@PathVariable Long id,
                               @RequestParam(required = false) String reason,
                               RedirectAttributes ra) {
        workerService.rejectWorker(id, reason != null ? reason : "Documents not satisfactory");
        ra.addFlashAttribute("success", "Worker rejected");
        return "redirect:/admin/workers?status=pending";
    }

    @PostMapping("/workers/{id}/suspend")
    public String suspendWorker(@PathVariable Long id, RedirectAttributes ra) {
        workerService.suspendWorker(id);
        ra.addFlashAttribute("success", "Worker suspended");
        return "redirect:/admin/workers";
    }

    @PostMapping("/workers/{id}/delete")
    public String deleteWorker(@PathVariable Long id, RedirectAttributes ra) {
        workerService.deleteWorker(id);
        ra.addFlashAttribute("success", "Worker deleted");
        return "redirect:/admin/workers";
    }

    // ── Booking Management ────────────────────────────────────────────────────

    @GetMapping("/bookings")
    public String bookings(@RequestParam(required = false) String status, Model model) {
        List<Booking> bookings = (status != null && !status.isBlank())
                ? bookingRepository.findByStatus(BookingStatus.valueOf(status.toUpperCase()))
                : bookingService.getAllBookings();
        model.addAttribute("bookings", bookings);
        model.addAttribute("status", status);
        model.addAttribute("statuses", BookingStatus.values());
        return "admin/bookings/list";
    }

    @GetMapping("/bookings/{id}")
    public String bookingDetail(@PathVariable Long id, Model model) {
        Booking booking = bookingService.findById(id);
        model.addAttribute("booking", booking);

        Payment payment = paymentService.findByBookingId(id);
        model.addAttribute("payment", payment);

        Agreement agreement = null;
        try { agreement = agreementService.findByBookingId(id); } catch (Exception ignored) {}
        model.addAttribute("agreement", agreement);

        return "admin/bookings/detail";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id,
                                @RequestParam(required = false) String reason,
                                RedirectAttributes ra) {
        bookingService.cancelBooking(id, reason);
        ra.addFlashAttribute("success", "Booking cancelled");
        return "redirect:/admin/bookings";
    }

    // ── Agreement Management ──────────────────────────────────────────────────

    @GetMapping("/agreements")
    public String agreements(Model model) {
        model.addAttribute("agreements", agreementService.getAllAgreements());
        return "admin/agreements/list";
    }

    @GetMapping("/agreements/{id}/download")
    public ResponseEntity<byte[]> downloadAgreement(@PathVariable Long id) throws Exception {
        byte[] pdf = agreementService.generateAgreementPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=agreement-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ── Payment Management ────────────────────────────────────────────────────

    @GetMapping("/payments")
    public String payments(Model model) {
        model.addAttribute("payments", paymentService.getAllPayments());
        model.addAttribute("totalRevenue", paymentService.getTotalRevenue());
        return "admin/payments/list";
    }

    @GetMapping("/payments/{bookingId}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long bookingId) throws Exception {
        byte[] pdf = paymentService.generateInvoicePdf(bookingId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + bookingId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ── Category Management ───────────────────────────────────────────────────

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories/list";
    }

    @PostMapping("/categories/add")
    public String addCategory(@RequestParam String name,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) String icon,
                              RedirectAttributes ra) {
        try {
            categoryService.createCategory(name, description, icon);
            ra.addFlashAttribute("success", "Category added: " + name);
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/update")
    public String updateCategory(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 @RequestParam(required = false) String icon,
                                 RedirectAttributes ra) {
        categoryService.updateCategory(id, name, description, icon);
        ra.addFlashAttribute("success", "Category updated");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/toggle")
    public String toggleCategory(@PathVariable Long id, RedirectAttributes ra) {
        categoryService.toggleStatus(id);
        ra.addFlashAttribute("success", "Category status changed");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        categoryService.deleteCategory(id);
        ra.addFlashAttribute("success", "Category deleted");
        return "redirect:/admin/categories";
    }

    // ── Review Management ─────────────────────────────────────────────────────

    @GetMapping("/reviews")
    public String reviews(Model model) {
        model.addAttribute("reviews", reviewService.getAllReviews());
        return "admin/reviews/list";
    }

    @PostMapping("/reviews/{id}/deactivate")
    public String deactivateReview(@PathVariable Long id, RedirectAttributes ra) {
        reviewService.deactivateReview(id);
        ra.addFlashAttribute("success", "Review hidden");
        return "redirect:/admin/reviews";
    }

    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id, RedirectAttributes ra) {
        reviewService.deleteReview(id);
        ra.addFlashAttribute("success", "Review deleted");
        return "redirect:/admin/reviews";
    }
}
