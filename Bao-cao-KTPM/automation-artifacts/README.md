# automation-artifacts

Thu muc nay luu ket qua moi lan chay automation.

Moi lan chay se tao 1 thu muc theo thoi gian, vi du:

- `20260327-170647`

## Trong moi lan chay co gi

- `summary.txt`: tom tat so test pass/fail
- `automation-console.log`: log tong hop
- `automation-stdout.log`: log output chuan
- `automation-stderr.log`: log loi
- `surefire-reports`: bao cao Maven test

## Cach doc nhanh

Neu `summary.txt` co:

- `tests=15`
- `passed=12`
- `failures=3`

thi bo automation da chay dung va da phat hien 3 bug race condition.

Luu y: `exit_code=1` trong truong hop nay la do test co case fail de chung minh bug, khong phai script bi hong.
