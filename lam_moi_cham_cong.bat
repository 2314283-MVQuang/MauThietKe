@echo off
chcp 65001 >nul
title Lam moi du lieu cham cong
cd /d "%~dp0"
rem =====================================================================
rem  XOA TOAN BO CA LAM + BANG LUONG DA CHOT (bang work_shifts, payroll_records)
rem  Lan chay web tiep theo se tu tao lai du lieu cham cong MAU (PayrollDemoSeeder).
rem  Dung truoc khi demo cho thay de du lieu sach dep.
rem =====================================================================
for /f "tokens=1,* delims==" %%a in ('findstr /b "spring.datasource.password=" src\main\resources\application.properties') do set PGPASSWORD=%%b
if exist cau_hinh_rieng.properties for /f "tokens=1,* delims==" %%a in ('findstr /b "spring.datasource.password=" cau_hinh_rieng.properties') do set PGPASSWORD=%%b
set PSQL=
for /d %%v in ("C:\Program Files\PostgreSQL\*") do if exist "%%v\bin\psql.exe" set PSQL="%%v\bin\psql.exe"
if not defined PSQL for %%x in (psql.exe) do if not "%%~$PATH:x"=="" set PSQL="%%~$PATH:x"
if not defined PSQL (
  echo Khong tim thay psql.exe
  pause
  exit /b 1
)
echo.
echo  SE XOA TOAN BO CA LAM VA BANG LUONG DA CHOT trong database trasua_db.
echo  Bam N de HUY. Sau 10 giay se tu dong tiep tuc.
choice /c YN /t 10 /d Y /m "Tiep tuc"
if errorlevel 2 exit /b 0
%PSQL% -h localhost -U postgres -d trasua_db -c "DELETE FROM payroll_records; DELETE FROM work_shifts;" > database\lam_moi_cham_cong.log 2>&1
if errorlevel 1 (
  echo LOI - xem database\lam_moi_cham_cong.log
  pause
  exit /b 1
)
echo RESET_OK >> database\lam_moi_cham_cong.log
echo Xong! Chay lai chay_web.bat de tao du lieu cham cong mau.
timeout /t 4 >nul
