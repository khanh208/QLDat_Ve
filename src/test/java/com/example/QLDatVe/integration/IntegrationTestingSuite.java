package com.example.QLDatVe.integration;

import com.example.QLDatVe.support.SuiteAssertions;
import org.junit.Test;

public class IntegrationTestingSuite {

    @Test
    public void integrationTestingSuiteShouldPass() {
        SuiteAssertions.assertClassesPass(BookingApiIntegrationTest.class);
    }
}
