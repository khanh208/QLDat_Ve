// src/main/java/com/example/QLDatVe/services/FeedbackService.java
package com.example.QLDatVe.services;

import com.example.QLDatVe.dtos.FeedbackRequest;
import com.example.QLDatVe.entities.Feedback;
import com.example.QLDatVe.entities.Trip;
import com.example.QLDatVe.entities.User;
import com.example.QLDatVe.repositories.FeedbackRepository;
import com.example.QLDatVe.repositories.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final TripRepository tripRepository;
    // No need for UserRepository directly if we pass the User object

    /**
     * Creates and saves new feedback.
     * @param request The feedback details.
     * @param user    The user submitting the feedback.
     * @return The saved Feedback object.
     */
    @Transactional
    public Feedback createFeedback(FeedbackRequest request, User user) {
        // Find the trip the feedback is about
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new RuntimeException("Trip not found with ID: " + request.getTripId()));

        // TODO: Optional validation: Check if this user actually took this trip
        // (e.g., check if they have a CONFIRMED booking for this trip)

        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setTrip(trip);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());
        // feedbackTime is set by @PrePersist

        return feedbackRepository.save(feedback);
    }

    /**
     * Gets all feedback for a specific trip, ordered by newest first.
     * @param tripId The ID of the trip.
     * @return List of Feedback objects.
     */
     public List<Feedback> getPublicPositiveFeedback() {
        List<Feedback> allFeedback = feedbackRepository.findAll();
        
        return allFeedback.stream()
            .filter(fb -> fb.getRating() != null && fb.getRating() >= 4)
            .sorted((a, b) -> {
                if (a.getFeedbackTime() == null) return 1;
                if (b.getFeedbackTime() == null) return -1;
                return b.getFeedbackTime().compareTo(a.getFeedbackTime());
            })
            .limit(50)
            .collect(Collectors.toList());
    }
     public List<Feedback> getFeedbackForTrip(int tripId) {
        return feedbackRepository.findByTrip_TripIdOrderByFeedbackTimeDesc(tripId);
    }

     /**
     * (Optional) Gets all feedback submitted by a specific user.
     * @param userId The ID of the user.
     * @return List of Feedback objects.
     */
    // public List<Feedback> getFeedbackByUser(int userId) {
    //     return feedbackRepository.findByUser_UserIdOrderByFeedbackTimeDesc(userId);
    // }

    /**
     * (Optional - For Admin) Gets all feedback in the system.
     * @return List of all Feedback objects.
     */
     public List<Feedback> getAllFeedback() {
         // Add sorting if desired, e.g., by date
         return feedbackRepository.findAll();
     }
}