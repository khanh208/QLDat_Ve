package com.example.QLDatVe.regression;

import com.example.QLDatVe.support.SuiteAssertions;
import org.junit.Test;

public class RegressionTestingSuite {

    @Test
    public void regressionTestingSuiteShouldPass() {
        SuiteAssertions.assertClassesPass(RegressionStableSuite.class);
    }
}
