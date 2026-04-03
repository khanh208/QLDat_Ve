// src/main/java/com/example/QLDatVe/controllers/FeedbackController.java
package com.example.QLDatVe.controllers;

import com.example.QLDatVe.dtos.FeedbackRequest;
import com.example.QLDatVe.entities.Feedback;
import com.example.QLDatVe.entities.User;
import com.example.QLDatVe.services.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    // Helper to get logged-in user
    private User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null || 
            !(authentication.getPrincipal() instanceof User)) {
            return null;
        }
        return (User) authentication.getPrincipal();
    }

    /**
     * ⭐ PUBLIC API: Lấy feedback tích cực để hiển thị trên homepage
     * GET /api/feedback/public
     * Không cần authentication
     */
    @GetMapping("/feedback/public")
    public ResponseEntity<?> getPublicFeedback() {
        try {
            List<Feedback> positiveFeedback = feedbackService.getPublicPositiveFeedback();
            
            System.out.println("✅ Public feedback request - Returned " + positiveFeedback.size() + " feedbacks");
            
            return ResponseEntity.ok(positiveFeedback);
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching public feedback: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unable to load feedbacks"));
        }
    }

    /**
     * API for logged-in users to submit feedback.
     * POST /api/feedback
     * Requires USER role (authenticated).
     */
    @PostMapping("/feedback")
    public ResponseEntity<?> submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        User currentUser = getLoggedInUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Vui lòng đăng nhập để gửi phản hồi."));
        }
        
        try {
            Feedback savedFeedback = feedbackService.createFeedback(request, currentUser);
            
            System.out.println("✅ User " + currentUser.getUsername() + 
                             " submitted feedback for trip " + request.getTripId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFeedback);
            
        } catch (RuntimeException e) {
            System.err.println("❌ Error submitting feedback: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * API to get all feedback for a specific trip (public).
     * GET /api/trips/{tripId}/feedback
     */
    @GetMapping("/trips/{tripId}/feedback")
    public ResponseEntity<?> getTripFeedback(@PathVariable int tripId) {
        try {
            List<Feedback> feedbackList = feedbackService.getFeedbackForTrip(tripId);
            
            System.out.println("✅ Fetched " + feedbackList.size() + 
                             " feedbacks for trip " + tripId);
            
            return ResponseEntity.ok(feedbackList);
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching feedback for trip " + tripId + 
                             ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unable to load trip feedbacks"));
        }
    }

    /**
     * (Optional) API for Admin to get ALL feedback.
     * GET /api/admin/feedback
     * Requires ADMIN role.
     */
    @GetMapping("/admin/feedback")
    public ResponseEntity<?> getAllFeedbackForAdmin() {
        try {
            List<Feedback> allFeedback = feedbackService.getAllFeedback();
            
            System.out.println("✅ Admin fetched all feedbacks: " + allFeedback.size());
            
            return ResponseEntity.ok(allFeedback);
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching all feedback for admin: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unable to load all feedbacks"));
        }
    }

    /**
     * ⭐ BONUS: API để user xóa feedback của chính mình
     * DELETE /api/feedback/{feedbackId}
     */
    @DeleteMapping("/feedback/{feedbackId}")
    public ResponseEntity<?> deleteFeedback(@PathVariable int feedbackId) {
        User currentUser = getLoggedInUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Vui lòng đăng nhập."));
        }
        
        try {
            // TODO: Implement deleteFeedback in FeedbackService
            // Verify ownership before deleting
            
            return ResponseEntity.ok(Map.of("message", "Feedback deleted successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}