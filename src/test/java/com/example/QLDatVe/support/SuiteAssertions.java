package com.example.QLDatVe.support;

import org.junit.Assert;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.util.stream.Collectors;

public final class SuiteAssertions {

    private SuiteAssertions() {
    }

    public static void assertClassesPass(Class<?>... testClasses) {
        Result result = JUnitCore.runClasses(testClasses);
        Assert.assertTrue(
                "Expected suite to pass but got failures: " + result.getFailures().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(" | ")),
                result.wasSuccessful()
        );
        Assert.assertTrue("Expected at least one test to run.", result.getRunCount() > 0);
    }
}
