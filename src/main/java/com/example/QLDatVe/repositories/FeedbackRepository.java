// src/main/java/com/example/QLDatVe/repositories/FeedbackRepository.java
package com.example.QLDatVe.repositories;

import com.example.QLDatVe.entities.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {

    // Find all feedback for a specific trip, ordered by newest first
    List<Feedback> findByTrip_TripIdOrderByFeedbackTimeDesc(int tripId);

    // (Optional) Find all feedback submitted by a specific user
    // List<Feedback> findByUser_UserIdOrderByFeedbackTimeDesc(int userId);
}