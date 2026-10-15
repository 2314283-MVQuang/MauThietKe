@echo off
rem =====================================================================
rem  Maven Wrapper don gian: dung Maven %MVN_VER% trong %USERPROFILE%\.m2\wrapper\dists
rem  (tu tai neu chua co) -> may khong can cai Maven van chay duoc:
rem      mvnw.cmd spring-boot:run      |      mvnw.cmd test
rem  VS Code (Maven for Java) tu dung file nay.
rem =====================================================================
setlocal
set MVN_VER=3.9.11
set MVN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MVN_VER%
if not exist "%MVN_HOME%\bin\mvn.cmd" (
  echo Dang tai Maven %MVN_VER% ...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $z=Join-Path $env:TEMP 'maven.zip'; Invoke-WebRequest -UseBasicParsing -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MVN_VER%/apache-maven-%MVN_VER%-bin.zip' -OutFile $z; Expand-Archive -Force $z (Join-Path $env:USERPROFILE '.m2\wrapper\dists')"
)
call "%MVN_HOME%\bin\mvn.cmd" %*
endlocal & exit /b %ERRORLEVEL%
