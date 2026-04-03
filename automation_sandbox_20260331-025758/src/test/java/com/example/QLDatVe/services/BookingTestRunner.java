package com.example.QLDatVe.services;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class BookingTestRunner {
    public static void main(String[] args) {
        System.out.println(">> DANG KHOI DONG HE THONG TEST TUDONG...");
        System.out.println(">> Ap dung 16 Rules tu Bang Quyet Dinh vao Kiem Thu.");
        System.out.println(">> Vui long cho...\n");
        
        // Chạy Test Suite
        Result result = JUnitCore.runClasses(BookingTestSuite.class);

        // In chi tiết các lỗi
        if (!result.wasSuccessful()) {
            System.out.println("============== DANH SACH LOI (FAILURES) ==============");
            for (Failure failure : result.getFailures()) {
                System.out.println("❌ Lỗi tại kịch bản: " + failure.getDescription().getMethodName());
                System.out.println("   Chi tiết: " + failure.getMessage() + "\n");
            }
        }

        int total = result.getRunCount();
        int failed = result.getFailureCount();
        int passed = total - failed; 

        System.out.println("=======================================================");
        System.out.println("       BAO CAO TONG HOP KET QUA KIEM THU (WHITE BOX)   ");
        System.out.println("=======================================================");
        System.out.printf("🔹 Tong so Test Case da chay : %d (Cover 16 Rules)%n", total);
        System.out.printf("✅ So Test Case PASSED       : %d%n", passed);
        System.out.printf("❌ So Test Case FAILED (BUG) : %d%n", failed);
        System.out.println("=======================================================");
        
        if (failed > 0) {
             System.out.println("👉 KET LUAN: HE THONG DANG TON TAI LUU HONG RACE CONDITION (MULTI-THREADING).");
             System.out.println("   (Chung minh qua cac Test case FAILED).");
        } else {
             System.out.println("👉 KET LUAN: HE THONG HOAN TOAN AN TOAN.");
        } 
    }
}