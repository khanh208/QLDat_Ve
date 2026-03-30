# PHAN 6: KIEM THU TU DONG (AUTOMATION TESTING)

## Huong thuc hien

Phan 6 duoc bo sung theo huong bam sat dung hien trang du an, khong mo rong them cong cu khong ton tai trong ma nguon. Noi dung automation se dua tren bo test backend da co san trong thu muc `src/test/java/com/example/QLDatVe/services`, sau do tong hop lai thanh mot phan bao cao hoan chinh gom 5 muc tu `6.1` den `6.5`.

Huong thuc hien cu the:

1. Xac dinh cong cu va framework test dang duoc du an su dung thuc te.
2. Liet ke cac test script da viet va chuc nang cua tung script.
3. Bo sung huong dan cai dat, lenh chay, va vi tri ket qua dau ra.
4. Tong hop ket qua automation tu cac artefact da co trong thu muc bao cao.
5. Danh gia diem manh, han che, va huong cai tien cho hoat dong automation.

## 6.1. Cong cu su dung va ly do lua chon

He thong kiem thu tu dong cua du an hien tai tap trung vao lop backend `BookingService` va su dung cac cong cu sau:

- `JUnit 4`: la framework chinh de viet va thuc thi test case tu dong cho Java. JUnit phu hop voi bai toan kiem thu logic nghiep vu, assert ket qua, va tach tung kich ban thanh cac case doc lap.
- `Mockito`: dung de gia lap (`mock`) cac thanh phan phu thuoc nhu `BookingRepository`, `BookingDetailRepository`, `TripRepository`, `EmailService`. Cach tiep can nay giup kiem thu logic thuan cua `BookingService` ma khong phu thuoc vao database that.
- `Maven Wrapper (mvnw.cmd)`: dung de chay test dong nhat tren moi may ma khong can cai dat Maven toan cuc.
- `JUnitCore / BookingTestRunner`: ho tro gom nhom test va in bao cao tong hop tren console, giup viec trinh bay ket qua automation ro rang hon.

Ly do lua chon bo cong cu tren:

- Phu hop voi cong nghe nen cua du an la `Spring Boot + Java`.
- Phu hop voi muc tieu bai tap: kiem tra `validation`, `functional`, `liveness`, va dac biet la `race condition`.
- Cho phep chay nhanh, lap lai duoc nhieu lan, de phat hien hoi quy khi code thay doi.
- De chung minh loi nghiep vu nghiem trong ma khong can phu thuoc vao giao dien frontend.

## 6.2. Cac test script da viet

Bo test tu dong hien co gom 5 artefact chinh:

### 1. `BookingValidationTest.java`

Duong dan: `src/test/java/com/example/QLDatVe/services/BookingValidationTest.java`

Chuc nang:

- Kiem thu nhom `Validation`.
- Bao gom 7 kich ban du lieu dau vao khong hop le:
  - Danh sach ghe rong.
  - Chuyen xe da khoi hanh.
  - Trip khong ton tai.
  - `tripId` am.
  - `user` bi `null`.
  - Trung ghe trong cung request.
  - `request` bi `null`.

Muc dich:

- Xac minh he thong chan duoc du lieu rac truoc khi di vao xu ly nghiep vu dat ve.

### 2. `BookingSafetyTest.java`

Duong dan: `src/test/java/com/example/QLDatVe/services/BookingSafetyTest.java`

Chuc nang:

- Kiem thu nhom `Safety & Concurrency`.
- Kiem thu nhom `Functional & Liveness`.
- Bao gom 8 kich ban chinh:
  - 2 nguoi cung dat 1 ghe bang `CASH`.
  - 2 nguoi cung dat 1 ghe bang `MOMO`.
  - Dat ghe da co nguoi mua.
  - Dat ve thanh cong bang `CASH`.
  - Dat ve thanh cong bang `MOMO`.
  - 1 nguoi dat nhieu ghe trong mot lan.
  - `Overlap Booking`: 2 nguoi dat danh sach ghe giao nhau.
  - Cron job tu dong huy ve `PENDING` qua han.

Muc dich:

- Chung minh he thong xu ly dung o cac luong happy path.
- Dong thoi phat hien loi `double booking` va `race condition` khi co tranh chap tai nguyen.

### 3. `BookingTestSuite.java`

Duong dan: `src/test/java/com/example/QLDatVe/services/BookingTestSuite.java`

Chuc nang:

- Gom `BookingValidationTest` va `BookingSafetyTest` thanh mot bo test tong.
- Giup chay mot lan de thu duoc toan bo 15 test case thay vi chay tung file rieng le.

### 4. `BookingTestRunner.java`

Duong dan: `src/test/java/com/example/QLDatVe/services/BookingTestRunner.java`

Chuc nang:

- Dung `JUnitCore` de kich hoat `BookingTestSuite`.
- In bao cao tong hop tren console:
  - Tong so test case da chay.
  - So case pass.
  - So case fail.
  - Danh sach loi va ket luan ve `race condition`.

Muc dich:

- Ho tro demo va trinh bay ket qua kiem thu tu dong mot cach truc quan.

### 5. `run-booking-automation.ps1`

Duong dan: `run-booking-automation.ps1`

Chuc nang:

- Tu dong tao mot sandbox build rieng de tranh xung dot voi thu muc `target` cua du an goc.
- Chay bo `BookingTestSuite` bang `Maven Wrapper`.
- Thu thap log console va `surefire-reports`.
- Xuat artifact vao thu muc `Bao-cao-KTPM/automation-artifacts/<timestamp>`.

Muc dich:

- Bien bo test thanh mot quy trinh automation co the chay lai bang mot lenh duy nhat.
- Tao bang chung thuc te cho phan bao cao va demo.

## 6.3. Huong dan cai dat va chay script

### Yeu cau moi truong

- JDK 21
- Maven Wrapper co san trong du an (`mvnw.cmd`)
- Cac dependency duoc khai bao trong `pom.xml`

### Cach chay tu thu muc goc du an

Mo `PowerShell` tai thu muc goc cua project:

```powershell
cd E:\NguyenQuocKhanh\tester\QLDat_Ve\QLDat_Ve
```

### Cach chay khuyen nghi

```powershell
.\run-booking-automation.ps1
```

Script tren se:

- Tao sandbox rieng de build/test.
- Chay bo `BookingTestSuite`.
- Luu log vao `Bao-cao-KTPM/automation-artifacts/`.

### Cach chay truc tiep bang Maven

```powershell
.\mvnw.cmd -Dtest=com.example.QLDatVe.services.BookingTestSuite test
```

### Chay rieng tung nhom test

```powershell
.\mvnw.cmd -Dtest=com.example.QLDatVe.services.BookingValidationTest test
.\mvnw.cmd -Dtest=com.example.QLDatVe.services.BookingSafetyTest test
```

### Chay bang runner de xem bao cao tong hop tren console

Co the chay `BookingTestRunner` truc tiep trong IDE de hien thi bao cao tong hop va danh sach case loi.

### Dau ra mong doi

Sau khi chay test, ket qua co the duoc xem tai:

- Console output cua Maven / IDE
- Thu muc `target/surefire-reports`
- Thu muc `Bao-cao-KTPM/automation-artifacts/` neu chay bang script
- Bao cao tong hop do `BookingTestRunner` in ra

## 6.4. Ket qua chay tu dong

Trong bo ho so hien tai, ket qua automation da duoc tong hop san trong:

- `Bao-cao-KTPM/TestReport-GK.xlsx`
- `Bao-cao-KTPM/Bang thong ke 15TC.xlsx`
- `Bao-cao-KTPM/TestCase-GK.xlsx`
- `Bao-cao-KTPM/automation-artifacts/20260327-165811/summary.txt`
- `Bao-cao-KTPM/automation-artifacts/20260327-165811/automation-console.log`

Tom tat ket qua:

- Tong so test case: `15`
- So test case `PASS`: `12`
- So test case `FAIL`: `3`
- Ti le pass: `80%`

Ket qua theo nhom:

- `Validation / Exception`: `7/7 PASS`
- `Functional & Liveness`: `4/4 PASS`
- `Safety & Concurrency`: `1 PASS / 3 FAIL`

Ba test case that bai:

1. `TC13 - Race Condition (Cash)`
2. `TC14 - Race Condition (MoMo)`
3. `TC15 - Overlap Booking`

Y nghia ket qua:

- He thong xu ly dung cac tinh huong `validation`, `happy path`, va `cron auto-cancel`.
- He thong chua dam bao an toan khi xay ra truy cap dong thoi vao cung mot tai nguyen ghe.
- Automation da phat hien ro loi nghiem trong ve `double booking`, qua do chung minh duoc rui ro nghiep vu cua module dat ve.

## 6.5. Nhan xet ve automation

### Uu diem

- Bo test tu dong bao phu duoc cac nhom logic quan trong nhat cua `BookingService`.
- Co kha nang chay lai nhieu lan de kiem tra hoi quy sau moi lan sua code.
- Dung `Mockito` de tach logic nghiep vu khoi database that, giup test nhanh va on dinh.
- Co cac kich ban da luong mo phong sat voi loi thuc te cua he thong.

### Han che

- Automation hien tai moi tap trung o muc `service/unit test`, chua mo rong thanh `integration test`, `system test`, hay `UI automation`.
- Chua co quy trinh `CI/CD` tu dong chay test sau moi lan commit.
- Chua co co che xuat bao cao HTML / dashboard tu dong; ket qua hien dang duoc tong hop thu cong vao file Excel va Word.

### Huong cai tien

- Them pipeline CI de tu dong chay `BookingTestSuite` moi khi co thay doi ma nguon.
- Xuat ket qua tu `Surefire Report` hoac `Allure Report` de de luu tru va trinh bay hon.
- Bo sung `integration test` cho luong ket noi that voi database va thanh toan.
- Bo sung `API test` cho cac endpoint dat ve, thanh toan, va huy ve.

## Ket luan

Phan kiem thu tu dong cua du an da co nen tang thuc te va phu hop voi bai toan dat ra. Bo test JUnit + Mockito hien tai da giup phat hien 3 loi nghiem trong lien quan den `race condition`, dong thoi xac nhan cac luong nghiep vu co ban van hoat dong dung. Day la mot bang chung ro rang cho gia tri cua automation testing trong viec bao ve chat luong backend cua he thong dat ve.
