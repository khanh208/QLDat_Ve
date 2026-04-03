package com.example.QLDatVe.performance;

import com.example.QLDatVe.dtos.BookingRequest;
import com.example.QLDatVe.entities.Booking;
import com.example.QLDatVe.entities.Route;
import com.example.QLDatVe.entities.Trip;
import com.example.QLDatVe.entities.User;
import com.example.QLDatVe.repositories.BookingDetailRepository;
import com.example.QLDatVe.repositories.BookingRepository;
import com.example.QLDatVe.repositories.TripRepository;
import com.example.QLDatVe.services.BookingService;
import com.example.QLDatVe.services.EmailService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BookingPerformanceSmokeTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingDetailRepository bookingDetailRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private EmailService emailService;

    private Trip validTrip;
    private User validUser;

    @Before
    public void setUp() {
        Route route = new Route();
        route.setStartLocation("Ha Noi");
        route.setEndLocation("Da Nang");

        validTrip = new Trip();
        validTrip.setTripId(1);
        validTrip.setBasePrice(BigDecimal.valueOf(120000));
        validTrip.setDepartureTime(LocalDateTime.now().plusDays(1));
        validTrip.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(5));
        validTrip.setRoute(route);

        validUser = new User();
        validUser.setUserId(1);
        validUser.setUsername("performance-user");
        validUser.setEmail("performance@example.com");
        validUser.setRole("ROLE_USER");
        validUser.setEnabled(true);

        when(tripRepository.findById(1)).thenReturn(Optional.of(validTrip));
        when(bookingDetailRepository.findDuplicateSeats(anyInt(), anySet())).thenReturn(new HashSet<>());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    public void createBookingShouldStayWithinPerformanceBudget() throws IOException {
        int iterations = 150;
        List<Long> durationsNanos = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            BookingRequest request = buildRequest("CASH", "P" + i);
            long startedAt = System.nanoTime();
            bookingService.createBooking(request, validUser);
            durationsNanos.add(System.nanoTime() - startedAt);
        }

        PerformanceMetrics metrics = PerformanceMetrics.from("createBooking", durationsNanos);
        writeReport("create-booking-smoke.txt", metrics.toMultilineReport());

        Assert.assertTrue("Average latency is unexpectedly high: " + metrics.averageMillis,
                metrics.averageMillis < 75.0d);
        Assert.assertTrue("P95 latency is unexpectedly high: " + metrics.p95Millis,
                metrics.p95Millis < 150.0d);
    }

    @Test
    public void concurrentBookingsShouldCompleteWithinBudget() throws Exception {
        int rounds = 12;
        List<Long> roundDurationsNanos = new ArrayList<>();

        for (int round = 0; round < rounds; round++) {
            ExecutorService executor = Executors.newFixedThreadPool(4);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch finishLatch = new CountDownLatch(4);

            for (int i = 0; i < 4; i++) {
                final int seatIndex = round * 10 + i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        bookingService.createBooking(buildRequest("CASH", "Q" + seatIndex), validUser);
                    } catch (Exception ignored) {
                    } finally {
                        finishLatch.countDown();
                    }
                });
            }

            long startedAt = System.nanoTime();
            startLatch.countDown();
            boolean completed = finishLatch.await(2, TimeUnit.SECONDS);
            long elapsed = System.nanoTime() - startedAt;
            roundDurationsNanos.add(elapsed);
            executor.shutdownNow();

            Assert.assertTrue("A concurrent performance round timed out.", completed);
        }

        PerformanceMetrics metrics = PerformanceMetrics.from("concurrentBookingRound", roundDurationsNanos);
        writeReport("concurrent-booking-smoke.txt", metrics.toMultilineReport());

        Assert.assertTrue("Concurrent average latency is unexpectedly high: " + metrics.averageMillis,
                metrics.averageMillis < 300.0d);
        Assert.assertTrue("Concurrent max latency is unexpectedly high: " + metrics.maxMillis,
                metrics.maxMillis < 1000.0d);
    }

    private BookingRequest buildRequest(String paymentMethod, String... seats) {
        BookingRequest request = new BookingRequest();
        request.setTripId(1);
        request.setPaymentMethod(paymentMethod);
        request.setSeatNumbers(Arrays.asList(seats));
        return request;
    }

    private void writeReport(String fileName, String content) throws IOException {
        Path reportDirectory = Path.of("target", "performance-reports");
        Files.createDirectories(reportDirectory);
        Files.writeString(
                reportDirectory.resolve(fileName),
                content + System.lineSeparator(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
    }

    private static class PerformanceMetrics {
        private final String scenario;
        private final int sampleCount;
        private final double averageMillis;
        private final double p95Millis;
        private final double maxMillis;

        private PerformanceMetrics(String scenario, int sampleCount, double averageMillis, double p95Millis, double maxMillis) {
            this.scenario = scenario;
            this.sampleCount = sampleCount;
            this.averageMillis = averageMillis;
            this.p95Millis = p95Millis;
            this.maxMillis = maxMillis;
        }

        private static PerformanceMetrics from(String scenario, List<Long> durationsNanos) {
            List<Long> sorted = new ArrayList<>(durationsNanos);
            Collections.sort(sorted);

            double averageMillis = sorted.stream()
                    .mapToDouble(value -> value / 1_000_000.0d)
                    .average()
                    .orElse(0.0d);

            int p95Index = Math.max(0, (int) Math.ceil(sorted.size() * 0.95d) - 1);
            double p95Millis = sorted.get(p95Index) / 1_000_000.0d;
            double maxMillis = sorted.get(sorted.size() - 1) / 1_000_000.0d;

            return new PerformanceMetrics(scenario, sorted.size(), averageMillis, p95Millis, maxMillis);
        }

        private String toMultilineReport() {
            return String.join(System.lineSeparator(),
                    "Scenario: " + scenario,
                    "Samples: " + sampleCount,
                    String.format("Average (ms): %.3f", averageMillis),
                    String.format("P95 (ms): %.3f", p95Millis),
                    String.format("Max (ms): %.3f", maxMillis)
            );
        }
    }
}
