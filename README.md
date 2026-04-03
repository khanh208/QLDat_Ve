# QLDatVe

He thong dat ve xe gom:

- Backend: Spring Boot
- Frontend: React
- Bao cao bai tap: `Bao-cao-KTPM`

## Cau truc nhanh

- `src`: ma nguon backend
- `qldatve-frontend`: ma nguon frontend
- `Bao-cao-KTPM`: tai lieu bao cao, test case, test report, bug report
- `run-booking-automation.ps1`: script chay automation test

## Chay backend

1. Kiem tra file `src/main/resources/application.properties`
2. Dam bao PostgreSQL da chay
3. Chay lenh:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend mac dinh chay o port `8081`.

## Chay frontend

```powershell
cd qldatve-frontend
npm install
npm start
```

Frontend mac dinh chay o `http://localhost:3000`.

## Chay automation

```powershell
powershell -ExecutionPolicy Bypass -File .\run-booking-automation.ps1
```

Ket qua se duoc luu trong `Bao-cao-KTPM/automation-artifacts`.

## Selenium E2E tren GitHub Actions

- Workflow: `.github/workflows/selenium-e2e.yml`
- Trigger: chay tu dong khi `push`
- Browser matrix hien tai: `chrome`, `firefox`
- Cach chay:
  - dung mock API tai `tests/e2e/mock-api-server.js`
  - bat frontend React tren `http://127.0.0.1:3000`
  - chay Selenium smoke test `FrontendSeleniumSmokeTest`

Test Selenium hien tai kiem tra nhanh:

- Trang chu tai duoc danh sach chuyen di
- Nguoi dung mo trang chi tiet chuyen
- Chon ghe va dat ve tien mat tren UI mock
- Hien thong bao thanh cong sau khi dat ve

## Tai lieu bao cao

Mo file `Bao-cao-KTPM/README.md` de xem nhanh cac tai lieu chinh cua bai.
