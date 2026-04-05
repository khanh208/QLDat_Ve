package com.example.QLDatVe.system;

import com.example.QLDatVe.support.SuiteAssertions;
import org.junit.Test;

public class SystemTestingSuite {

    @Test
    public void systemTestingSuiteShouldPass() {
        SuiteAssertions.assertClassesPass(BookingSystemFlowMockMvcTest.class);
    }
}
