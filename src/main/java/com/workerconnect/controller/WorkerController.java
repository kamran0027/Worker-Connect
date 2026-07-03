package com.workerconnect.controller;

import com.workerconnect.model.*;
import com.workerconnect.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/worker")
@RequiredArgsConstructor
public class WorkerController {

    private final UserService userService;
    private final WorkerService workerService;
    private final BookingService bookingService;
    private final AgreementService agreementService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;
    private final CategoryService categoryService;
    private final AuthService authService;

    private Worker getCurrentWorker(UserDetails ud) {
        User user = userService.findByEmail(ud.getUsername());
        return workerService.findByUserId(user.getId());
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails ud, Model model) {
        Worker worker = getCurrentWorker(ud);
        List<Booking> bookings = bookingService.getBookingsByWorker(worker.getId());
        model.addAttribute("worker", worker);
        model.addAttribute("bookings", bookings);
        model.addAttribute("totalBookings", bookings.size());
        model.addAttribute("totalEarnings", paymentService.getWorkerEarnings(worker.getId()));
        model.addAttribute("totalReviews", worker.getTotalReviews());
        model.addAttribute("avgRating", worker.getAverageRating());
        model.addAttribute("totalJobsCompleted", worker.getTotalJobsCompleted());
        return "worker/dashboard";
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails ud, Model model) {
        model.addAttribute("worker", getCurrentWorker(ud));
        model.addAttribute("categories", categoryService.getActiveCategories());
        return "worker/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails ud,
                                @ModelAttribute Worker updated,
                                @RequestParam(required = false) Long categoryId,
                                RedirectAttributes ra) {
        try {
            Worker worker = getCurrentWorker(ud);
            workerService.updateProfile(worker.getId(), updated, categoryId);
            ra.addFlashAttribute("success", "Profile updated successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/worker/profile";
    }

    @PostMapping("/profile/image")
    public String updateProfileImage(@AuthenticationPrincipal UserDetails ud,
                                     @RequestParam("image") MultipartFile file,
                                     RedirectAttributes ra) {
        try {
            Worker worker = getCurrentWorker(ud);
            workerService.updateProfileImage(worker.getId(), file);
            ra.addFlashAttribute("success", "Profile image updated");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to upload: " + e.getMessage());
        }
        return "redirect:/worker/profile";
    }

    @PostMapping("/profile/documents")
    public String uploadDocuments(@AuthenticationPrincipal UserDetails ud,
                                  @RequestParam(value = "aadhaar", required = false) MultipartFile aadhaar,
                                  @RequestParam(value = "pan", required = false) MultipartFile pan,
                                  @RequestParam(value = "certificate", required = false) MultipartFile cert,
                                  RedirectAttributes ra) {
        try {
            Worker worker = getCurrentWorker(ud);
            workerService.uploadDocuments(worker.getId(), aadhaar, pan, cert);
            ra.addFlashAttribute("success", "Documents uploaded successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Upload failed: " + e.getMessage());
        }
        return "redirect:/worker/profile";
    }

    @PostMapping("/profile/toggle-availability")
    public String toggleAvailability(@AuthenticationPrincipal UserDetails ud, RedirectAttributes ra) {
        Worker worker = getCurrentWorker(ud);
        workerService.toggleAvailability(worker.getId());
        ra.addFlashAttribute("success", "Availability status updated");
        return "redirect:/worker/dashboard";
    }

    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "worker/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal UserDetails ud,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes ra) {
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Passwords do not match");
            return "redirect:/worker/change-password";
        }
        try {
            authService.changePassword(ud.getUsername(), oldPassword, newPassword);
            ra.addFlashAttribute("success", "Password changed successfully");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/worker/change-password";
    }

    // ── Bookings ──────────────────────────────────────────────────────────────

    @GetMapping("/bookings")
    public String bookingsList(@AuthenticationPrincipal UserDetails ud, Model model) {
        Worker worker = getCurrentWorker(ud);
        model.addAttribute("bookings", bookingService.getBookingsByWorker(worker.getId()));
        model.addAttribute("worker", worker);
        return "worker/booking/list";
    }

    @GetMapping("/bookings/{id}")
    public String bookingDetail(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails ud, Model model) {
        Worker worker = getCurrentWorker(ud);
        Booking booking = bookingService.findById(id);
        if (!booking.getWorker().getId().equals(worker.getId())) return "redirect:/worker/bookings";

        model.addAttribute("booking", booking);
        model.addAttribute("worker", worker);

        Agreement agreement = null;
        try { agreement = agreementService.findByBookingId(id); } catch (Exception ignored) {}
        model.addAttribute("agreement", agreement);

        return "worker/booking/detail";
    }

    @PostMapping("/bookings/{id}/accept")
    public String acceptBooking(@PathVariable Long id, RedirectAttributes ra) {
        bookingService.acceptBooking(id);
        ra.addFlashAttribute("success", "Booking accepted");
        return "redirect:/worker/bookings/" + id;
    }

    @PostMapping("/bookings/{id}/reject")
    public String rejectBooking(@PathVariable Long id,
                                @RequestParam(required = false) String reason,
                                RedirectAttributes ra) {
        bookingService.rejectBooking(id, reason);
        ra.addFlashAttribute("success", "Booking rejected");
        return "redirect:/worker/bookings";
    }

    @PostMapping("/bookings/{id}/start")
    public String startWork(@PathVariable Long id, RedirectAttributes ra) {
        bookingService.markWorkStarted(id);
        ra.addFlashAttribute("success", "Work marked as started");
        return "redirect:/worker/bookings/" + id;
    }

    @PostMapping("/bookings/{id}/complete")
    public String completeWork(@PathVariable Long id, RedirectAttributes ra) {
        bookingService.markWorkCompleted(id);
        ra.addFlashAttribute("success", "Work marked as completed");
        return "redirect:/worker/bookings/" + id;
    }

    // ── Agreement ─────────────────────────────────────────────────────────────

    @PostMapping("/agreement/{id}/sign")
    public String signAgreement(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Agreement a = agreementService.workerSign(id);
            ra.addFlashAttribute("success", "Agreement signed successfully");
            return "redirect:/worker/bookings/" + a.getBooking().getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/worker/bookings";
        }
    }

    @GetMapping("/agreement/{id}/download")
    public ResponseEntity<byte[]> downloadAgreement(@PathVariable Long id) throws Exception {
        byte[] pdf = agreementService.generateAgreementPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=agreement-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ── Earnings & Reviews ────────────────────────────────────────────────────

    @GetMapping("/earnings")
    public String earnings(@AuthenticationPrincipal UserDetails ud, Model model) {
        Worker worker = getCurrentWorker(ud);
        model.addAttribute("worker", worker);
        model.addAttribute("payments", paymentService.getPaymentsByWorker(worker.getId()));
        model.addAttribute("totalEarnings", paymentService.getWorkerEarnings(worker.getId()));
        return "worker/earnings";
    }

    @GetMapping("/reviews")
    public String reviews(@AuthenticationPrincipal UserDetails ud, Model model) {
        Worker worker = getCurrentWorker(ud);
        model.addAttribute("worker", worker);
        model.addAttribute("reviews", reviewService.getWorkerReviews(worker.getId()));
        return "worker/reviews";
    }
}
