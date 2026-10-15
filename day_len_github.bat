@echo off
chcp 65001 >nul
title Dua du an len GitHub
cd /d "%~dp0"
rem =====================================================================
rem  DUA DU AN LEN GITHUB: https://github.com/2314283-MVQuang/MauThietKe
rem  - Lan dau: Git se mo trinh duyet de ban dang nhap GitHub.
rem  - Cac file trong .gitignore (cau_hinh_rieng.properties chua mat khau,
rem    target, log, zip...) KHONG duoc dua len.
rem =====================================================================
where git >nul 2>&1
if errorlevel 1 (
  echo.
  echo  May chua cai Git. Tai va cai tai: https://git-scm.com/download/win
  echo  ^(cu bam Next den het^), sau do bam dup lai file nay.
  echo.
  pause
  exit /b 1
)
if not exist .git git init
git config user.name >nul 2>&1 || git config user.name "Mai Van Quang"
git config user.email >nul 2>&1 || git config user.email "2314283@dlu.edu.vn"

rem An toan: chac chan file mat khau khong bi dua len
git check-ignore -q cau_hinh_rieng.properties
if errorlevel 1 (
  echo LOI: cau_hinh_rieng.properties chua nam trong .gitignore - dung lai de tranh lo mat khau.
  pause
  exit /b 1
)

git add -A
git commit -m "Cap nhat: cham cong & luong, quan ly KH/NV, xu ly don theo ngay & chot ket, QR thanh toan, con/het mon, topping x2, gop y, size tuy chinh, top ban chay theo ngay/tuan/thang"
git branch -M main
git remote get-url origin >nul 2>&1 || git remote add origin https://github.com/2314283-MVQuang/MauThietKe.git
echo.
echo === Dang day len GitHub (lan dau se hien cua so dang nhap GitHub) ===
git push -u origin main > push.log 2>&1
set PUSH_ERR=%errorlevel%
type push.log
if not "%PUSH_ERR%"=="0" (
  echo.
  echo  Day len THAT BAI - xem thong bao loi o tren.
) else (
  echo.
  echo  XONG! Mo https://github.com/2314283-MVQuang/MauThietKe de kiem tra.
)
pause
