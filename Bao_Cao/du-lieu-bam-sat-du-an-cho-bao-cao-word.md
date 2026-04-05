# Du lieu bam sat du an cho bao cao Word

## Ten de tai su dung cho bao cao

`TLA + DAC TA HE THONG DAT VE: CHUNG MINH "KHONG BAN QUA SO GHE" (SAFETY) + KHONG KET (LIVENESS)`

Ghi chu:
- Nguoi thuc hien: 01 sinh vien / 01 nguoi lam du an.
- Trong workspace hien tai chua thay file dac ta `*.tla` hoac `*.cfg`.
- Vi vay, khi viet bao cao khong duoc khang dinh da co model checking TLA+ neu chua bo sung hien vat dac ta.
- Co the viet theo huong: trinh bay safety/liveness duoi goc nhin dac ta nghiep vu va doi chieu bang chung tu bo test hien co.

## Cau truc bao cao can bam theo file `yeucaubaocao.docx`

Noi dung OCR tu file mau cho thay bao cao can di theo 8 phan:

1. Phan 1: Tong quan du an
2. Phan 2: Phan tich yeu cau va pham vi kiem thu
3. Phan 3: Ke hoach kiem thu
4. Phan 4: Thiet ke ca kiem thu
5. Phan 5: Thuc thi kiem thu
6. Phan 6: Kiem thu tu dong
7. Phan 7: Bao cao loi
8. Phan 8: Tong ket va danh gia

## Nguon bang chung nen dung khi viet tung phan

### Phan 1 - Tong quan du an

Nguon:
- `README.md`
- `src/APTTest.txt`
- `src/main/java/com/example/QLDatVe/services/BookingService.java`

Noi dung co the viet chac chan:
- He thong dat ve xe gom backend Spring Boot va frontend React.
- Backend mac dinh chay o port `8081`.
- Trong bai bao cao nen chot module trong tam la `BookingService`.
- Muc tieu thuc te cua bai test la kiem tra dat ve, tranh overbooking, va giai phong ghe pending qua han.

### Phan 2 - Phan tich yeu cau va pham vi kiem thu

Nguon:
- `Bao_Cao/MUC TIEU KIEM THU.docx`
- `Bao_Cao/KE HOACH KIEM THU PHAN MEM.docx`
- `src/main/java/com/example/QLDatVe/services/BookingService.java`
- `src/test/java/com/example/QLDatVe/services/*.java`

Pham vi co bang chung:
- `createBooking`
- `checkAndCancelExpiredBookings`
- `cancelBooking`
- `cancelBookingByAdmin`
- `confirmBookingAfterPayment`

Ngoai pham vi hoac chua co bang chung manh:
- Chua co bang chung ro rang cho kiem thu UI automation.
- Chua co bang chung ro rang cho integration test that voi PostgreSQL/MoMo that.
- Chua co bang chung ro rang cho performance test va security test dung nghia day du.

### Phan 3 - Ke hoach kiem thu

Nguon:
- `Bao_Cao/KE HOACH KIEM THU PHAN MEM.docx`
- `Bao_Cao/Tieu chi bat dau kiem thu.docx`
- `Bao_Cao/MUC TIEU KIEM THU.docx`

Noi dung da co san trong tai lieu:
- Muc tieu safety: khong ban trung / khong ban qua ghe khi co dong thoi.
- Muc tieu liveness: khong de ghe bi giu pending qua han.
- Moi truong: Windows 11, Java, Mockito, JUnit, PostgreSQL.
- Nhan su: 01 sinh vien.
- So luong test case tong hop: 30.

### Phan 4 - Thiet ke ca kiem thu

Nguon:
- `Bao_Cao/CAC TRUONG HOP KIEM THU.docx`
- `Bao_Cao/Kiem thu hop trang.docx`
- `Bao_Cao/Bang quyet dinh.xlsx`
- `Bao_Cao/TestCase.xlsx`

Noi dung co the viet chac:
- Co thiet ke test theo huong hop den va hop trang.
- White-box da bam sat ham `createBooking`, da mo ta CFG, node, path va mapping test path.
- Co bo test case lien quan race condition, validation, happy path, lifecycle.

### Phan 5 - Thuc thi kiem thu

Nguon:
- `Bao_Cao/BAO CAO TOM TAT KIEM THU.docx`
- `Bao_Cao/Bao Cao Loi.docx`
- `Bao-cao-KTPM/automation-artifacts/*`
- `Bao_Cao/CAC TRUONG HOP KIEM THU.docx`

Ket qua co the khang dinh:
- Tong so test case: 30
- Pass: 22
- Fail: 8
- 3 loi safety / da luong
- 5 loi validation du lieu

Khi viet muc 5 theo mau can rat can than:
- `Functional Testing`: co bang chung.
- `Regression Testing`: co the viet co thuc hien lap lai nhieu lan vi co nhieu lan chay artifact tu `20260327` den `20260331`.
- `Integration Testing`: chi nen viet muc do han che neu ban chua co bang chung test that voi DB, payment gateway, mail server.
- `System Testing`: chi nen viet muc do han che / khong nam pham vi neu chua co ho so thuc thi day du.
- `Performance Testing`: neu khong co script/so lieu do hieu nang thi ghi ro chua thuc hien.
- `Security Testing`: neu khong co test case/bao cao rieng thi ghi ro chua thuc hien day du.

### Phan 6 - Kiem thu tu dong

Nguon chinh:
- `Bao-cao-KTPM/PHAN-6-AUTOMATION-TESTING.md`
- `run-booking-automation.ps1`
- `src/test/java/com/example/QLDatVe/services/BookingTestSuite.java`
- `src/test/java/com/example/QLDatVe/services/BookingValidationTest.java`
- `src/test/java/com/example/QLDatVe/services/BookingSafetyTest.java`
- `src/test/java/com/example/QLDatVe/services/BookingLifecycleTest.java`
- `src/test/java/com/example/QLDatVe/services/BookingValidationGapsTest.java`
- `Bao-cao-KTPM/automation-artifacts/20260331-025758/summary.txt`

So lieu xac nhan:
- Bo test gom 4 nhom chay qua `BookingTestSuite`.
- Tong `30` test.
- `22` pass, `8` fail.
- `exit_code=1` la do co case fail de chung minh bug, khong phai script hu.

Phan nhom ket qua:
- Validation co san: 7/7 pass.
- Safety va concurrency: 5 pass, 3 fail.
- Lifecycle / cancel / cron / confirm payment: 10/10 pass.
- Validation gaps bo sung: 0/5 pass.

### Phan 7 - Bao cao loi

Nguon:
- `Bao_Cao/Bao Cao Loi.docx`
- `Bao_Cao/BAO CAO TOM TAT KIEM THU.docx`
- `Bao-cao-KTPM/automation-artifacts/20260331-025758/surefire-reports/TEST-com.example.QLDatVe.services.BookingTestSuite.xml`

Loi da co bang chung ro:
- BUG-001: Race Condition khi 2 nguoi dat cung 1 ghe bang CASH.
- BUG-002: Race Condition khi 2 nguoi dat cung 1 ghe bang MOMO.
- BUG-003: Overlap booking khi hai request co ghe giao nhau.
- 5 loi validation bo sung:
  - `paymentMethod = null`
  - `paymentMethod` khong hop le
  - `seatNumbers` chua `null`
  - `seatNumbers` chua chuoi rong
  - `seatNumbers` gom gia tri hop le va chuoi rong

### Phan 8 - Tong ket va danh gia

Co the tong ket theo huong:
- He thong dat ve hien da dung o nhieu luong nghiep vu chinh.
- Phan lifecycle va xu ly booking sau thanh toan/cron da on hon.
- Van ton tai loi safety quan trong: co the ban trung ghe khi co truy cap dong thoi.
- Van ton tai lo hong validation dau vao.
- Gia tri lon nhat cua bo test hien tai la phat hien va tai hien duoc bug mot cach lap lai.

## Bang doi chieu test that su da tim thay trong du an

### 1. Test validation co san

File:
- `src/test/java/com/example/QLDatVe/services/BookingValidationTest.java`

Case da co:
- GhE rong
- Trip da khoi hanh
- Trip khong ton tai
- `tripId` am
- `user = null`
- Trung ghe trong cung request
- `request = null`

### 2. Test safety va race condition

File:
- `src/test/java/com/example/QLDatVe/services/BookingSafetyTest.java`

Case da co:
- 2 nguoi cung dat 1 ghe bang CASH
- 2 nguoi cung dat 1 ghe bang MOMO
- Dat ghe da ban
- Dat ve thanh cong bang CASH
- Dat ve thanh cong bang MOMO
- 1 nguoi dat nhieu ghe
- 2 request dat ghe giao nhau
- Cron tu dong huy ve pending qua han

### 3. Test lifecycle va resilience

File:
- `src/test/java/com/example/QLDatVe/services/BookingLifecycleTest.java`

Case da co:
- Gui email loi nhung booking van tao
- Cron job khong co du lieu qua han
- Cron job gap loi repository nhung khong crash
- Hai nguoi dat dong thoi nhung khac ghe
- User huy booking dung quyen
- User huy booking sai quyen
- Huy booking khi chuyen da khoi hanh
- Admin huy booking
- Xac nhan thanh toan MoMo thanh cong
- Callback MoMo khong tim thay booking

### 4. Test validation gaps bo sung

File:
- `src/test/java/com/example/QLDatVe/services/BookingValidationGapsTest.java`

5 case nay hien dung de chung minh lo hong chua duoc chan o service hien tai.

## Su that can viet ro trong bao cao

1. Day la du an do 1 nguoi thuc hien.
2. Trong repo hien tai chua thay file TLA+ thuc te (`.tla`, `.cfg`), vi vay khong nen viet nhu da co model checking day du neu chua bo sung them.
3. Bang chung manh nhat hien co la bo unit/automation test backend cho `BookingService`.
4. Phan `Safety` co bang chung that bai ro rang qua 3 test fail va log surefire.
5. Phan `Liveness` hien duoc chung minh o muc nghiep vu van hanh qua cron job huy `PENDING` qua han, khong phai chung minh hinh thuc bang TLA+ trong repo hien tai.
6. Neu can viet cac muc `performance`, `security`, `integration`, `system` theo mau thi nen ghi ro:
   - chua thuc hien day du, hoac
   - chua nam trong pham vi bai test hien tai.

## So lieu nen dung nhat quan trong bao cao

- So test case: `30`
- So pass: `22`
- So fail: `8`
- Ti le pass: `73.3%`
- So loi safety / concurrency: `3`
- So loi validation du lieu: `5`
- Thoi diem artifact moi nhat trong repo: `2026-03-31 02:57:58` (thu muc `20260331-025758`)

## Hien vat TLA+ da bo sung

Da bo sung thu muc `tla/` gom:

- `tla/BookingServiceCurrentImpl.tla`
- `tla/BookingServiceCurrentImpl_Safety.cfg`
- `tla/BookingServiceCurrentImpl_Liveness.cfg`
- `tla/README.md`

Huong dung trong bao cao:
- Mo ta day la dac ta TLA+ cho hanh vi hien tai cua `BookingService`.
- `Safety` duoc kiem qua 2 property:
  - khong co 2 booking `PENDING/CONFIRMED` cung giu 1 ghe
  - khong co 2 booking `CONFIRMED` cung 1 ghe
- `Liveness` duoc kiem qua property: booking `PENDING` cuoi cung phai duoc giai quyet.
- Vi model bam sat implementation hien tai, safety co the sinh counterexample; dieu nay van hop le cho muc tieu phat hien loi.

## Huong viet bao cao an toan, khong bia

Nen viet:
- "Theo bo test hien co..."
- "Trong pham vi module BookingService..."
- "Theo artifact automation moi nhat..."
- "Du lieu trong repo cho thay..."

Khong nen viet:
- "He thong da duoc chung minh hinh thuc bang TLA+" neu chua co file dac ta.
- "Da thuc hien performance/security/integration/system test day du" neu chua co bang chung.
- "Nhom thuc hien" neu thuc te chi co 1 nguoi; nen doi thanh "nguoi thuc hien" hoac ghi ro "01 sinh vien".

## Buoc tiep theo khi viet Word

Neu viet bao cao Word o buoc sau, uu tien:
1. Giu dung 8 phan cua file mau.
2. Dien noi dung bam cac file bang chung o tren.
3. O cac muc chua co bang chung, ghi ro "chua thuc hien / ngoai pham vi".
4. Neu can lam ro phan TLA+, phai xin them file dac ta hoac thong tin ngoai repo truoc khi viet nhu mot bang chung chinh thuc.

## Hiện vật Chương 5 JUnit + Mock
- `Bảng thống kê 40TC.xlsx`
- `Bảng thống kê Chương 5 JUnit Mock.xlsx`
- `BÁO CÁO TÓM TẮT KIỂM THỬ - Chương 5.docx`
- `CÁC TRƯỜNG HỢP KIỂM THỬ - Chương 5.docx`
- `Test Case ID - Chương 5.docx`


## Hiện vật Chương 6 Automation
- `Bảng thống kê Chương 6 Automation.xlsx`
- `TestCase_Chuong6_Automation.xlsx`
- `TestReport.xlsx`
- `Chuong6_KiemThuTuDong_Automation.docx`
- `Chuong6_KiemThuTuDong_Automation.md`


## Hiện vật Chương 7 và Chương 8
- `Bảng thống kê Chương 7 Bug Report.xlsx`
- `TestReport.xlsx`
- `Chuong7_BaoCaoLoi.docx`
- `Chuong8_TongKetVaDanhGia.docx`
