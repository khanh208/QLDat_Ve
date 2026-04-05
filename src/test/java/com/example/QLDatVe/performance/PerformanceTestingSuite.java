package com.example.QLDatVe.performance;

import com.example.QLDatVe.support.SuiteAssertions;
import org.junit.Test;

public class PerformanceTestingSuite {

    @Test
    public void performanceTestingSuiteShouldPass() {
        SuiteAssertions.assertClassesPass(BookingPerformanceSmokeTest.class);
    }
}
