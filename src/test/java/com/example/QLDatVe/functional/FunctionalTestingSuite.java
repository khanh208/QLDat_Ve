package com.example.QLDatVe.functional;

import com.example.QLDatVe.services.BookingLifecycleTest;
import com.example.QLDatVe.services.BookingSafetyTest;
import com.example.QLDatVe.services.BookingValidationGapsTest;
import com.example.QLDatVe.services.BookingValidationTest;
import com.example.QLDatVe.support.SuiteAssertions;
import org.junit.Test;

public class FunctionalTestingSuite {

    @Test
    public void functionalTestingSuiteShouldPass() {
        SuiteAssertions.assertClassesPass(
                BookingValidationTest.class,
                BookingSafetyTest.class,
                BookingLifecycleTest.class,
                BookingValidationGapsTest.class
        );
    }
}
