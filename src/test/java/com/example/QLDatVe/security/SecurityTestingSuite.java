package com.example.QLDatVe.security;

import com.example.QLDatVe.support.SuiteAssertions;
import org.junit.Test;

public class SecurityTestingSuite {

    @Test
    public void securityTestingSuiteShouldPass() {
        SuiteAssertions.assertClassesPass(SecurityAccessIntegrationTest.class);
    }
}
