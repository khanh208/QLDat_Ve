# TLA+ cho `BookingService`

Thu muc nay bo sung dac ta TLA+ toi thieu cho de tai:

`TLA + DAC TA HE THONG DAT VE: CHUNG MINH "KHONG BAN QUA SO GHE" (SAFETY) + KHONG KET (LIVENESS)`

## File chinh

- `BookingServiceCurrentImpl.tla`
- `BookingServiceCurrentImpl_Safety.cfg`
- `BookingServiceCurrentImpl_Liveness.cfg`

## Y tuong mo hinh

Dac ta nay bam sat hanh vi hien tai cua `BookingService`:

- `StartCheck`: request vua qua buoc kiem tra trung ghe.
- `CommitBooking`: luu booking xuong he thong.
- `ConfirmBooking`: callback thanh toan MoMo.
- `CancelExpiredPending`: cron job huy booking `PENDING` qua han.
- `UserCancelBooking`, `AdminCancelBooking`: huy booking.
- `AdvanceTime`: dong ho logic de model hoa timeout.

Diem co y quan trong:

- `StartCheck` va `CommitBooking` duoc tach thanh 2 hanh dong rieng.
- O `CommitBooking`, dac ta co y khong kiem tra lai duplicate.
- Cach model nay dung de tai hien race condition hien dang duoc bo test Java phat hien.

Pham vi cua dac ta:

- Tap trung vao logic dat ghe, xung dot ghe, timeout `PENDING`, va cac chuyen trang thai chinh.
- Chua mo hinh hoa day du tat ca validation dau vao nhu `paymentMethod = null`, seat blank, request null...
- Vi vay, TLA+ trong thu muc nay nen duoc trinh bay la dac ta cho `safety/liveness` cua booking flow, khong phai dac ta day du cho toan bo backend.

## Thuoc tinh can kiem tra

### 1. Safety

- `NoSeatConflictHeld`
  - Cung 1 ghe khong duoc dong thoi nam trong hon 1 booking co trang thai `PENDING` hoac `CONFIRMED`.
- `NoSeatConflictConfirmed`
  - Cung 1 ghe khong duoc `CONFIRMED` cho hon 1 booking.

Chay voi file:

- `BookingServiceCurrentImpl_Safety.cfg`

Ky vong:

- Model co the sinh counterexample cho `safety`, phu hop voi hien trang du an: race condition van ton tai.

### 2. Liveness

- `PendingEventuallyResolved`
  - Moi booking dang `PENDING` cuoi cung phai duoc `CONFIRMED` hoac `CANCELLED`.

Chay voi file:

- `BookingServiceCurrentImpl_Liveness.cfg`

Ky vong:

- Model co the kiem tra rieng liveness duoi fairness cua `AdvanceTime` va `CancelExpiredPending`.

## Cach chay TLC

Workspace hien tai chua kem `tla2tools.jar`, nen file nay chi them dac ta va cau hinh.
Khi co `tla2tools.jar`, co the chay nhu sau:

```powershell
java -cp tla2tools.jar tlc2.TLC -config tla/BookingServiceCurrentImpl_Safety.cfg tla/BookingServiceCurrentImpl.tla
java -cp tla2tools.jar tlc2.TLC -config tla/BookingServiceCurrentImpl_Liveness.cfg tla/BookingServiceCurrentImpl.tla
```

## Cach viet vao bao cao

Nen viet theo huong:

- Dac ta TLA+ duoc xay dung de model hoa hanh vi hien tai cua module dat ve.
- Thuoc tinh `safety` duoc dat ra de kiem tra xem he thong co ban trung ghe hay khong.
- Neu TLC tim thay counterexample, day la bang chung hinh thuc cho thay implementation hien tai chua dat `safety`.
- Thuoc tinh `liveness` duoc dung de kiem tra booking `PENDING` co cuoi cung duoc giai quyet hay khong.

Khong nen viet:

- "Safety da duoc chung minh dung" neu model hoac implementation hien tai con sinh counterexample.
