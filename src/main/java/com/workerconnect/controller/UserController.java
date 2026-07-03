package com.workerconnect.controller;

import com.workerconnect.dto.BookingDto;
import com.workerconnect.model.*;
import com.workerconnect.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final WorkerService workerService;
    private final BookingService bookingService;
    private final AgreementService agreementService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;
    private final CategoryService categoryService;
    private final AuthService authService;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = userService.findByEmail(ud.getUsername());
        List<Booking> bookings = bookingService.getBookingsByUser(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("bookings", bookings);
        model.addAttribute("totalBookings", bookings.size());
        model.addAttribute("categories", categoryService.getActiveCategories());
        return "user/dashboard";
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails ud, Model model) {
        model.addAttribute("user", userService.findByEmail(ud.getUsername()));
        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails ud,
                                @RequestParam String fullName,
                                @RequestParam String phone,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) String city,
                                @RequestParam(required = false) String state,
                                RedirectAttributes ra) {
        User user = userService.findByEmail(ud.getUsername());
        userService.updateProfile(user.getId(), fullName, phone, address, city, state);
        ra.addFlashAttribute("success", "Profile updated successfully");
        return "redirect:/user/profile";
    }

    @PostMapping("/profile/image")
    public String updateProfileImage(@AuthenticationPrincipal UserDetails ud,
                                     @RequestParam("image") MultipartFile file,
                                     RedirectAttributes ra) {
        try {
            User user = userService.findByEmail(ud.getUsername());
            userService.updateProfileImage(user.getId(), file);
            ra.addFlashAttribute("success", "Profile image updated");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to upload image: " + e.getMessage());
        }
        return "redirect:/user/profile";
    }

    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "user/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal UserDetails ud,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes ra) {
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "New passwords do not match");
            return "redirect:/user/change-password";
        }
        try {
            authService.changePassword(ud.getUsername(), oldPassword, newPassword);
            ra.addFlashAttribute("success", "Password changed successfully");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/change-password";
    }

    // ── Booking ───────────────────────────────────────────────────────────────

    @GetMapping("/book/{workerId}")
    public String bookWorkerPage(@PathVariable Long workerId, Model model) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setWorkerId(workerId);

        model.addAttribute("worker", workerService.findById(workerId));
        model.addAttribute("bookingDto", bookingDto);
        return "user/booking/create";
    }

    @PostMapping("/book")
    public String createBooking(@AuthenticationPrincipal UserDetails ud,
                                @Valid @ModelAttribute BookingDto dto,
                                BindingResult result, RedirectAttributes ra, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("worker", workerService.findById(dto.getWorkerId()));
            return "user/booking/create";
        }
        try {
            User user = userService.findByEmail(ud.getUsername());
            Booking booking = bookingService.createBooking(user.getId(), dto);
            ra.addFlashAttribute("success", "Booking created! Booking #" + booking.getBookingNumber());
            return "redirect:/user/bookings/" + booking.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/book/" + dto.getWorkerId();
        }
    }

    @GetMapping("/bookings")
    public String myBookings(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = userService.findByEmail(ud.getUsername());
        model.addAttribute("bookings", bookingService.getBookingsByUser(user.getId()));
        model.addAttribute("user", user);
        return "user/booking/list";
    }

    @GetMapping("/bookings/{id}")
    public String bookingDetail(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails ud, Model model) {
        Booking booking = bookingService.findById(id);
        User user = userService.findByEmail(ud.getUsername());
        if (!booking.getUser().getId().equals(user.getId())) return "redirect:/user/bookings";

        model.addAttribute("booking", booking);
        model.addAttribute("user", user);
        Payment payment = paymentService.findByBookingId(id);
        model.addAttribute("payment", payment);

        Agreement agreement = null;
        try { agreement = agreementService.findByBookingId(id); } catch (Exception ignored) {}
        model.addAttribute("agreement", agreement);

        boolean reviewed = reviewService.hasReviewedBooking(id);
        model.addAttribute("reviewed", reviewed);
        

        return "user/booking/detail";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id,
                                @RequestParam(required = false) String reason,
                                @AuthenticationPrincipal UserDetails ud,
                                RedirectAttributes ra) {
        Booking booking = bookingService.findById(id);
        User user = userService.findByEmail(ud.getUsername());
        if (!booking.getUser().getId().equals(user.getId())) return "redirect:/user/bookings";
        bookingService.cancelBooking(id, reason);
        ra.addFlashAttribute("success", "Booking cancelled");
        return "redirect:/user/bookings";
    }

    // ── Agreement ─────────────────────────────────────────────────────────────

    @PostMapping("/agreement/{id}/sign")
    public String signAgreement(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Agreement a = agreementService.userSign(id);
            ra.addFlashAttribute("success", "Agreement signed successfully");
            return "redirect:/user/bookings/" + a.getBooking().getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/bookings";
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

    // ── Payment ───────────────────────────────────────────────────────────────

    @GetMapping("/payment/{bookingId}")
    public String paymentPage(@PathVariable Long bookingId, Model model) {
        Booking booking = bookingService.findById(bookingId);
        model.addAttribute("booking", booking);
        model.addAttribute("razorpayKeyId", paymentService.getRazorpayKeyId());
        //model.addAttribute("stripePublicKey", paymentService.getStripePublicKey());
        return "user/payment/pay";
    }

    @PostMapping("/payment/razorpay/create")
    @ResponseBody
    public ResponseEntity<?> createRazorpayOrder(@RequestParam Long bookingId) {
        try {
            var payment = paymentService.createRazorpayOrder(bookingId);
            return ResponseEntity.ok(java.util.Map.of(
                    "orderId", payment.getRazorpayOrderId(),
                    "amount", payment.getAmount(),
                    "currency", "INR"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/payment/razorpay/verify")
    public String verifyRazorpay(@RequestParam String razorpay_order_id,
                                 @RequestParam String razorpay_payment_id,
                                 @RequestParam String razorpay_signature,
                                 RedirectAttributes ra) {
        log.info("verifyRazorpay called: order_id={}, payment_id={}, signature={}", razorpay_order_id, razorpay_payment_id, razorpay_signature);
        try {
            Payment payment = paymentService.verifyRazorpayPayment(razorpay_order_id, razorpay_payment_id, razorpay_signature);
            log.info("Payment verified and saved: id={}, status={}", payment.getId(), payment.getStatus());
            ra.addFlashAttribute("success", "Payment successful!");
            return "redirect:/user/bookings/" + payment.getBooking().getId();
        } catch (Exception e) {
            log.error("Payment verification failed", e);
            ra.addFlashAttribute("error", "Payment verification failed: " + e.getMessage());
            return "redirect:/user/bookings";
        }
    }

    @GetMapping("/payment/COD/checkout")
    public String createCODPayment(@RequestParam Long bookingId, RedirectAttributes ra) {
        try {
            Payment payment = paymentService.createCODPayment(bookingId);
            ra.addFlashAttribute("success", "Cash on Delivery selected! Please pay the amount in cash to the worker upon service completion.");
            return "redirect:/user/bookings/" + payment.getBooking().getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to create COD payment: " + e.getMessage());
            return "redirect:/user/bookings";
        }
    }

    @GetMapping("/payments")
    public String paymentHistory(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = userService.findByEmail(ud.getUsername());
        model.addAttribute("payments", paymentService.getPaymentsByUser(user.getId()));
        model.addAttribute("user", user);
        return "user/payment/history";
    }

    @GetMapping("/payment/{bookingId}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long bookingId) throws Exception {
        byte[] pdf = paymentService.generateInvoicePdf(bookingId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + bookingId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ── Reviews ───────────────────────────────────────────────────────────────

    @PostMapping("/review/add")
    public String addReview(@AuthenticationPrincipal UserDetails ud,
                            @RequestParam Long workerId,
                            @RequestParam Long bookingId,
                            @RequestParam Integer rating,
                            @RequestParam String comment,
                            RedirectAttributes ra) {
        try {
            User user = userService.findByEmail(ud.getUsername());
            reviewService.addReview(user.getId(), workerId, bookingId, rating, comment);
            ra.addFlashAttribute("success", "Review submitted successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/bookings/" + bookingId;
    }
}
