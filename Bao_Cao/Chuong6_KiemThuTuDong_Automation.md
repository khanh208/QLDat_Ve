# CHƯƠNG 6: KIỂM THỬ TỰ ĐỘNG (AUTOMATION TESTING)

## 6.1. Mục tiêu kiểm thử tự động

Kiểm thử tự động trong dự án QLDatVe được bổ sung nhằm giảm khối lượng chạy test thủ công lặp lại, giúp nhóm có thể kiểm tra nhanh các nhánh nghiệp vụ quan trọng sau mỗi lần cập nhật mã nguồn. Phần automation cũng đóng vai trò chuẩn hóa cách thực thi test, tạo ra log, báo cáo và ảnh chụp màn hình có cấu trúc để phục vụ phân tích kết quả trong báo cáo.

Phạm vi tự động hóa hiện tại tập trung vào ba lớp chính. Thứ nhất là automation backend bằng JUnit, Mockito và MockMvc cho các luồng ổn định của BookingService. Thứ hai là automation hiệu năng ở mức smoke test để đo nhanh thời gian xử lý của createBooking. Thứ ba là automation giao diện bằng Selenium kết hợp GitHub Actions, dùng để kiểm tra các luồng người dùng quan trọng trên frontend như xem chuyến xe, đặt vé CASH, đặt vé MOMO, kiểm tra ghế đã bán và trang lịch sử vé.

## 6.2. Công cụ và kiến trúc automation

Hệ thống automation của dự án được xây dựng trên nền JUnit 4 và Maven Surefire ở phía backend, kết hợp với Mockito và MockMvc để tách logic nghiệp vụ khỏi dependency thật. Đối với frontend, dự án sử dụng Selenium WebDriver để điều khiển trình duyệt, Node.js để chạy mock API server, React dev server để dựng giao diện và GitHub Actions để điều phối toàn bộ pipeline kiểm thử tự động trên môi trường CI.

Điểm đáng chú ý của phần automation là workflow Selenium được cấu hình theo matrix strategy để chạy song song trên hai trình duyệt Chrome và Firefox. Cách tổ chức này giúp tăng độ tin cậy của kiểm thử giao diện, giảm nguy cơ testcase chỉ pass ở một browser duy nhất và tạo ra bộ minh chứng có giá trị hơn cho báo cáo. Ngoài workflow smoke test ổn định, dự án còn có workflow riêng để thu thập bằng chứng cho các lỗi known issue như đặt trùng ghế trong tình huống hai người dùng thao tác đồng thời.

## 6.3. Automation backend bằng JUnit + Mock

Ở phía backend, dự án hiện có workflow backend-regression.yml để chạy RegressionStableSuite mỗi khi push code, mở pull request hoặc kích hoạt thủ công. Workflow này dùng để phát hiện nhanh việc thay đổi mã nguồn có làm ảnh hưởng đến các nhánh nghiệp vụ ổn định hay không. Song song với đó, workflow performance-smoke.yml cho phép chạy BookingPerformanceSmokeTest theo yêu cầu để ghi nhận số liệu hiệu năng smoke test và upload báo cáo ra artifact.

Ngoài GitHub Actions, dự án còn duy trì khả năng chạy automation backend ngay trên máy local thông qua Maven và script PowerShell. Điều này giúp người thực hiện có thể chủ động chạy lại các suite functional, integration, system, performance, security và regression trước khi commit code, từ đó giảm xác suất đưa lỗi lên môi trường CI.

## 6.4. Automation UI bằng Selenium + GitHub Actions

Workflow selenium-e2e.yml được cấu hình để chạy khi push code và khi kích hoạt thủ công. Trong mỗi lần chạy, workflow lần lượt checkout mã nguồn, cài Java và Node, cài dependency frontend, khởi động mock API server, khởi động React frontend, chờ các dịch vụ sẵn sàng rồi mới chạy bộ FrontendSeleniumSmokeTest. Bộ smoke test hiện tại bao phủ sáu luồng chính gồm hiển thị danh sách chuyến, đặt vé CASH, kiểm tra ghế đã bán, chuyển hướng login, hiển thị My Bookings và luồng thanh toán MOMO.

Để phục vụ phân tích lỗi và minh chứng báo cáo, workflow selenium-known-issues.yml được bổ sung riêng cho ba testcase quan trọng là TC02, TC03 và TC24. Đây là các ca kiểm thử mô phỏng hai người dùng thao tác đồng thời trên cùng ghế hoặc trên tập ghế giao nhau. Workflow này sử dụng continue-on-error để trong trường hợp testcase fail theo đúng bug hiện hữu, hệ thống vẫn tiếp tục upload ảnh chụp màn hình, summary và log để người kiểm thử có thể sử dụng làm bằng chứng trong báo cáo lỗi.

## 6.5. Artifact và đầu ra của automation

Mỗi workflow automation đều tạo ra artifact riêng để phục vụ kiểm tra kết quả. Với backend regression, artifact chính là thư mục target/surefire-reports. Với performance smoke, dự án lưu thêm target/performance-reports chứa số liệu Average, P95 và Max. Với Selenium, artifact bao gồm frontend.log, mock-api.log, target/surefire-reports và target/selenium-screenshots.

Thư mục target/selenium-screenshots được tổ chức theo pass và fail, sau đó tách tiếp theo từng thành phần như home, booking, auth, my-bookings và payment. Cách tổ chức này giúp người thực hiện có thể nhanh chóng tìm lại ảnh minh họa cho từng testcase. Riêng workflow known issue còn tạo evidence bundle cho từng testcase TC02, TC03 và TC24 để làm rõ expected result, observed result và trạng thái của từng user trước và sau khi submit booking.

## 6.6. Đánh giá chung

Nhìn chung, phần kiểm thử tự động của dự án đã được tổ chức theo hướng thực tế và có thể sử dụng lại nhiều lần. Backend có regression workflow để kiểm tra nhanh các luồng ổn định, performance smoke để quan sát hiệu năng cơ bản, còn frontend có Selenium smoke test chạy trên nhiều trình duyệt ngay trong GitHub Actions. Đây là bước tiến quan trọng so với việc chỉ chạy test thủ công hoặc chỉ chạy unit test rời rạc trên máy cá nhân.

Tuy vậy, phần automation hiện vẫn chủ yếu dựa trên mock đối với frontend flow và chưa thay thế hoàn toàn cho end-to-end test với môi trường thật, cơ sở dữ liệu thật và cổng thanh toán thật. Do đó, hướng phát triển tiếp theo là tiếp tục mở rộng phạm vi regression UI, bổ sung thêm kịch bản booking đồng thời ở mức hệ thống thật và tăng cường việc lưu trữ artifact để phục vụ phân tích lỗi sâu hơn.
