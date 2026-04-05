package com.example.QLDatVe.chapter5;

import com.example.QLDatVe.integration.Chapter5ReducedIntegrationVisibleTest;
import com.example.QLDatVe.performance.BookingPerformanceSmokeTest;
import com.example.QLDatVe.regression.RegressionStableSuite;
import com.example.QLDatVe.security.Chapter5ReducedSecurityVisibleTest;
import com.example.QLDatVe.services.BookingLifecycleTest;
import com.example.QLDatVe.services.BookingSafetyTest;
import com.example.QLDatVe.services.BookingValidationGapsTest;
import com.example.QLDatVe.services.BookingValidationTest;
import com.example.QLDatVe.system.BookingSystemFlowMockMvcTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BookingValidationTest.class,
        BookingSafetyTest.class,
        BookingLifecycleTest.class,
        BookingValidationGapsTest.class,
        Chapter5ReducedIntegrationVisibleTest.class,
        BookingSystemFlowMockMvcTest.class,
        BookingPerformanceSmokeTest.class,
        Chapter5ReducedSecurityVisibleTest.class,
        RegressionStableSuite.class
})
public class Chapter5VisibleResultsSuite {
}
