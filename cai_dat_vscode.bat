@echo off
chcp 65001 >nul
title Cai dat cong cu cho VS Code
cd /d "%~dp0"
set LOG=%~dp0cai_dat_vscode.log
echo [%date% %time%] Bat dau > "%LOG%"
rem =====================================================================
rem  CAI CAC EXTENSION VS CODE CAN DE CHAY DU AN SPRING BOOT + MAVEN
rem  (chi can chay 1 lan tren moi may)
rem =====================================================================
set CODE=
for %%x in (code.cmd) do if not "%%~$PATH:x"=="" set CODE="%%~$PATH:x"
if not defined CODE if exist "%LOCALAPPDATA%\Programs\Microsoft VS Code\bin\code.cmd" set CODE="%LOCALAPPDATA%\Programs\Microsoft VS Code\bin\code.cmd"
if not defined CODE if exist "C:\Program Files\Microsoft VS Code\bin\code.cmd" set CODE="C:\Program Files\Microsoft VS Code\bin\code.cmd"
if not defined CODE (
  echo CODE_NOT_FOUND >> "%LOG%"
  echo Khong tim thay VS Code. Cai tai https://code.visualstudio.com roi chay lai.
  pause
  exit /b 1
)
echo === Cai Extension Pack for Java + Spring Boot Extension Pack ===
for %%e in (vscjava.vscode-java-pack vmware.vscode-boot-dev-pack) do (
  echo Cai %%e ...
  call %CODE% --install-extension %%e --force >> "%LOG%" 2>&1
)
rem Tai Maven neu may chua co (mvnw.cmd trong du an se dung ban nay)
set MVN_VER=3.9.11
set MVN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MVN_VER%
if not exist "%MVN_HOME%\bin\mvn.cmd" (
  echo Dang tai Maven %MVN_VER% ...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $z=Join-Path $env:TEMP 'maven.zip'; Invoke-WebRequest -UseBasicParsing -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MVN_VER%/apache-maven-%MVN_VER%-bin.zip' -OutFile $z; Expand-Archive -Force $z (Join-Path $env:USERPROFILE '.m2\wrapper\dists')" >> "%LOG%" 2>&1
)
if exist "%MVN_HOME%\bin\mvn.cmd" (echo MAVEN_OK >> "%LOG%") else (echo MAVEN_FAILED >> "%LOG%")
echo ===== EXTENSIONS ===== >> "%LOG%"
call %CODE% --list-extensions --show-versions >> "%LOG%" 2>&1
echo DONE >> "%LOG%"
echo.
echo Xong! Mo lai VS Code, bam "Trust" thu muc du an, roi chay TraSuaApplication.java.
timeout /t 5 >nul
