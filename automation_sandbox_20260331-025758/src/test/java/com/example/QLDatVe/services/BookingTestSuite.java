package com.example.QLDatVe.services;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BookingValidationTest.class,
        BookingSafetyTest.class,
        BookingLifecycleTest.class,
        BookingValidationGapsTest.class
})
public class BookingTestSuite {
}
