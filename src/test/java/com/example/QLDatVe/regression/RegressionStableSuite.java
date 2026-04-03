package com.example.QLDatVe.regression;

import com.example.QLDatVe.security.SecurityAccessIntegrationTest;
import com.example.QLDatVe.services.BookingLifecycleTest;
import com.example.QLDatVe.services.BookingValidationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BookingValidationTest.class,
        BookingLifecycleTest.class,
        SecurityAccessIntegrationTest.class
})
public class RegressionStableSuite {
}
