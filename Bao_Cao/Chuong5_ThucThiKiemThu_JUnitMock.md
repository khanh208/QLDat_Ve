# CHƯƠNG 5 - THỰC THI KIỂM THỬ (JUnit + Mock)

## 5.1. Kiểm thử chức năng (Functional Testing)

Mục này tiếp tục bám vào `BookingService` và sử dụng `FunctionalTestingSuite` để gọi lại `BookingValidationTest`, `BookingSafetyTest`, `BookingLifecycleTest` và `BookingValidationGapsTest`.

Kết quả hiện tại: `30` test case chức năng đã chạy, `22` pass và `8` fail. Trong `8` fail có `3` lỗi `Safety / Concurrency` và `5` lỗi `Validation gaps`. Đây là các bug nghiệp vụ thật của implementation hiện tại, không phải lỗi môi trường kiểm thử.

Minh chứng: `E:\NguyenQuocKhanh\tester\QLDat_Ve\QLDat_Ve\target\chapter5-test-execution\20260404-152308\5.1_Functional_Testing.log`

## 5.2. Kiểm thử tích hợp (Integration Testing)

Phần kiểm thử tích hợp được bổ sung bằng `BookingApiIntegrationTest` với `WebMvcTest`, `MockMvc` và `@MockBean`. Ở bản rút gọn cho báo cáo, phần này giữ `2` test case tiêu biểu: tạo booking CASH thành công và trả lỗi khi service phát hiện seat conflict.

Kết quả thực thi: `2/2` test pass.

Minh chứng: `E:\NguyenQuocKhanh\tester\QLDat_Ve\QLDat_Ve\target\chapter5-test-execution\20260404-152308\5.2_Integration_Testing.log`

## 5.3. Kiểm thử hệ thống (System Testing)

Kiểm thử hệ thống ở mức mock được triển khai bằng `BookingSystemFlowMockMvcTest`. Hai hành trình được mô phỏng là luồng `CASH` từ tra cứu chuyến đến `My Bookings`, và luồng `MOMO` từ tạo booking `PENDING` đến checkout, confirm callback và lịch sử booking.

Kết quả thực thi: `2/2` test pass.

Minh chứng: `E:\NguyenQuocKhanh\tester\QLDat_Ve\QLDat_Ve\target\chapter5-test-execution\20260404-152308\5.3_System_Testing.log`

## 5.4. Kiểm thử hiệu năng (Performance Testing)

Hiệu năng được kiểm tra ở mức smoke test bằng `BookingPerformanceSmokeTest`. Kết quả đo được:

- `createBooking` lặp `150` lần: Average = `2.621`, P95 = `4.290`, Max = `5.081`;
- `12` vòng concurrent booking: Average = `32.751`, P95 = `129.778`, Max = `129.778`.

Cả hai test đều pass theo ngưỡng đã đặt trong mã nguồn.

Minh chứng: `E:\NguyenQuocKhanh\tester\QLDat_Ve\QLDat_Ve\target\chapter5-test-execution\20260404-152308\5.4_Performance_Testing.log`

## 5.5. Kiểm thử bảo mật cơ bản (Security Testing)

Kiểm thử bảo mật cơ bản được bổ sung bằng `SecurityAccessIntegrationTest`, tập trung vào xác thực và phân quyền. Trong bản rút gọn cho báo cáo, phần này giữ `3` test case tiêu biểu: cho phép anonymous truy cập `/api/trips`, chặn anonymous ở `/api/bookings/my-bookings`, và cho phép admin truy cập `/api/admin/bookings`.

Kết quả thực thi: `3/3` test pass.

Minh chứng: `E:\NguyenQuocKhanh\tester\QLDat_Ve\QLDat_Ve\target\chapter5-test-execution\20260404-152308\5.5_Security_Testing.log`

## 5.6. Kiểm thử hồi quy (Regression Testing)

Kiểm thử hồi quy được triển khai bằng `RegressionStableSuite`. Suite này chạy lại `BookingValidationTest`, `BookingLifecycleTest` và `SecurityAccessIntegrationTest` thông qua `SuiteAssertions` để xác nhận các nhóm test ổn định vẫn không bị ảnh hưởng sau khi bổ sung test mới.

Kết quả thực thi: `1/1` suite pass.

Minh chứng: `E:\NguyenQuocKhanh\tester\QLDat_Ve\QLDat_Ve\target\chapter5-test-execution\20260404-152308\5.6_Regression_Testing.log`

## 5.7. Thống kê kết quả

Tổng hợp Chương 5 hiện có:

- Functional Testing: `30` tested, `22` passed, `8` failed;
- Integration Testing: `2` tested, `2` passed;
- System Testing: `2` tested, `2` passed;
- Performance Testing: `2` tested, `2` passed;
- Security Testing: `3` tested, `3` passed;
- Regression Testing: `1` suite, `1` passed.

Tổng cộng có `40` lượt thực thi được ghi nhận, trong đó `32` pass, `8` fail, tỷ lệ pass khoảng `80.00%`.

Lưu ý: các mục `5.2` đến `5.6` đều chạy trong môi trường `JUnit + Mock / MockMvc`, không dùng cơ sở dữ liệu thật hay dịch vụ ngoài thật.
