@echo off
chcp 65001 >nul
title Tra Sua DLU - Spring Boot
cd /d "%~dp0"
set LOG=%~dp0run.log
echo [%date% %time%] Bat dau > "%LOG%"

rem ---------- 0. Tat ban web cu dang chay (neu co) ----------
for /f "tokens=5" %%p in ('netstat -ano ^| findstr /r /c:":8080 .*LISTENING"') do taskkill /F /PID %%p >nul 2>&1
timeout /t 3 /nobreak >nul

rem ---------- 1. Doc mat khau PostgreSQL ----------
rem    (cau_hinh_rieng.properties - file rieng cua tung may, khong len GitHub - se ghi de)
if not exist cau_hinh_rieng.properties if exist cau_hinh_rieng.properties.example copy cau_hinh_rieng.properties.example cau_hinh_rieng.properties >nul
for /f "tokens=1,* delims==" %%a in ('findstr /b "spring.datasource.password=" src\main\resources\application.properties') do set PGPASSWORD=%%b
if exist cau_hinh_rieng.properties for /f "tokens=1,* delims==" %%a in ('findstr /b "spring.datasource.password=" cau_hinh_rieng.properties') do set PGPASSWORD=%%b
rem Tim psql.exe cua bat ky phien ban PostgreSQL nao (16, 17, 18...) hoac trong PATH
set PSQL=
for /d %%v in ("C:\Program Files\PostgreSQL\*") do if exist "%%v\bin\psql.exe" set PSQL="%%v\bin\psql.exe"
if not defined PSQL for %%x in (psql.exe) do if not "%%~$PATH:x"=="" set PSQL="%%~$PATH:x"
if not defined PSQL (
  echo.
  echo  Khong tim thay psql.exe - bo qua buoc tao database.
  echo  Hay tao database trasua_db trong pgAdmin truoc khi chay.
  echo.
  goto maven
)

echo === Kiem tra ket noi PostgreSQL ===
%PSQL% -h localhost -U postgres -d postgres -tAc "select 1" >> "%LOG%" 2>&1
if errorlevel 1 (
  echo DB_LOGIN_FAILED >> "%LOG%"
  echo.
  echo  KHONG DANG NHAP DUOC POSTGRESQL voi mat khau hien tai.
  echo  Mo file cau_hinh_rieng.properties ^(thu muc goc du an^), sua dong
  echo  spring.datasource.password=... thanh mat khau postgres cua may ban, roi chay lai file nay.
  pause
  exit /b 1
)
echo DB_LOGIN_OK >> "%LOG%"

rem ---------- 2. Tao database trasua_db neu chua co ----------
%PSQL% -h localhost -U postgres -d postgres -tAc "select 1 from pg_database where datname='trasua_db'" > "%TEMP%\ts_db.txt" 2>>"%LOG%"
findstr "1" "%TEMP%\ts_db.txt" >nul
if errorlevel 1 (
  echo Tao database trasua_db...
  %PSQL% -h localhost -U postgres -d postgres -c "CREATE DATABASE trasua_db ENCODING 'UTF8'" >> "%LOG%" 2>&1
  echo Nap du lieu mau database\trasua_db.sql...
  %PSQL% -h localhost -U postgres -d trasua_db -f database\trasua_db.sql >> "%LOG%" 2>&1
  echo DB_CREATED >> "%LOG%"
) else (
  echo DB_EXISTS >> "%LOG%"
)

rem ---------- 3. Maven (tu tai neu may chua co) ----------
:maven
set MVN_VER=3.9.11
set MVN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MVN_VER%
if not exist "%MVN_HOME%\bin\mvn.cmd" (
  echo Dang tai Maven %MVN_VER% tu Maven Central...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $z=Join-Path $env:TEMP 'maven.zip'; Invoke-WebRequest -UseBasicParsing -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MVN_VER%/apache-maven-%MVN_VER%-bin.zip' -OutFile $z; Expand-Archive -Force $z (Join-Path $env:USERPROFILE '.m2\wrapper\dists')" >> "%LOG%" 2>&1
)
if not exist "%MVN_HOME%\bin\mvn.cmd" (
  echo MAVEN_DOWNLOAD_FAILED >> "%LOG%"
  echo Tai Maven that bai, xem run.log
  pause
  exit /b 1
)
echo MAVEN_OK >> "%LOG%"

rem ---------- 4. Tat ban web cu dang chay (neu co) ----------
for /f "tokens=5" %%p in ('netstat -ano ^| findstr /r /c:":8080 .*LISTENING"') do taskkill /F /PID %%p >nul 2>&1

rem ---------- 5. Chay web ----------
echo.
echo === Dang khoi dong web (lan dau mat 1-3 phut de tai thu vien) ===
echo === Khi thay "Started TraSuaApplication" thi mo http://localhost:8080 ===
echo === Dong cua so nay = tat web ===
echo.
powershell -NoProfile -ExecutionPolicy Bypass -Command "& '%MVN_HOME%\bin\mvn.cmd' -DskipTests spring-boot:run 2>&1 | Tee-Object -FilePath '%~dp0app.log'"
pause
