@echo off
chcp 65001 >nul
title Xuat database trasua_db
cd /d "%~dp0"
rem =====================================================================
rem  XUAT TOAN BO DATABASE trasua_db (cau truc + du lieu) ra database\trasua_db.sql
rem  De dua len GitHub: nguoi khac clone ve, chay_web.bat se tu nap file nay.
rem =====================================================================
for /f "tokens=1,* delims==" %%a in ('findstr /b "spring.datasource.password=" src\main\resources\application.properties') do set PGPASSWORD=%%b
if exist cau_hinh_rieng.properties for /f "tokens=1,* delims==" %%a in ('findstr /b "spring.datasource.password=" cau_hinh_rieng.properties') do set PGPASSWORD=%%b
set PGDUMP=
for /d %%v in ("C:\Program Files\PostgreSQL\*") do if exist "%%v\bin\pg_dump.exe" set PGDUMP="%%v\bin\pg_dump.exe"
if not defined PGDUMP for %%x in (pg_dump.exe) do if not "%%~$PATH:x"=="" set PGDUMP="%%~$PATH:x"
if not defined PGDUMP (
  echo Khong tim thay pg_dump.exe
  echo PGDUMP_NOT_FOUND > database\xuat_db.log
  pause
  exit /b 1
)
echo Dang xuat database trasua_db ...
%PGDUMP% -h localhost -U postgres -d trasua_db --clean --if-exists --no-owner --no-privileges --encoding=UTF8 -f database\trasua_db_dump.sql > database\xuat_db.log 2>&1
if errorlevel 1 (
  echo DUMP_FAILED >> database\xuat_db.log
  echo Xuat that bai, xem database\xuat_db.log
  pause
  exit /b 1
)
rem Bo cac dong chi PostgreSQL 17.6+ moi hieu (\restrict, transaction_timeout) de may ban cu van nap duoc
powershell -NoProfile -ExecutionPolicy Bypass -Command "$l = Get-Content -Encoding UTF8 'database\trasua_db_dump.sql' | Where-Object { $_ -notmatch '^\\(un)?restrict ' -and $_ -notmatch '^SET transaction_timeout' }; [IO.File]::WriteAllLines((Join-Path (Get-Location) 'database\trasua_db.sql'), $l, (New-Object Text.UTF8Encoding $false))" >> database\xuat_db.log 2>&1
echo DUMP_OK >> database\xuat_db.log
echo Xong: database\trasua_db.sql
timeout /t 3 >nul
