package com.example.QLDatVe.services;

import com.example.QLDatVe.support.SuiteAssertions;
import org.junit.Test;

public class BookingTestSuite {

    @Test
    public void bookingTestSuiteShouldRunUnderlyingServiceTests() {
        SuiteAssertions.assertClassesPass(
                BookingValidationTest.class,
                BookingSafetyTest.class,
                BookingLifecycleTest.class,
                BookingValidationGapsTest.class
        );
    }
}
