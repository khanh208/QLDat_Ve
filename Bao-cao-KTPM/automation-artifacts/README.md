# automation-artifacts

Thu muc nay luu ket qua moi lan chay automation.

Moi lan chay se tao 1 thu muc theo thoi gian, vi du:

- `20260330-210928`

## Trong moi lan chay co gi

- `summary.txt`: tom tat so test pass/fail
- `automation-console.log`: log tong hop
- `automation-stdout.log`: log output chuan
- `automation-stderr.log`: log loi
- `surefire-reports`: bao cao Maven test

## Cach doc nhanh

Neu `summary.txt` co:

- `tests=30`
- `passed=22`
- `failures=8`

thi bo automation da chay dung va da phat hien tong cong 8 bug/lo hong, gom 3 loi race condition va 5 loi validation dau vao.

Luu y: `exit_code=1` trong truong hop nay la do test co case fail de chung minh bug, khong phai script bi hong.
