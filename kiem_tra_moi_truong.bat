@echo off
chcp 65001 >nul
cd /d "%~dp0"
set OUT=%~dp0env.txt
echo ===== JAVA ===== > "%OUT%"
where java >> "%OUT%" 2>&1
java -version >> "%OUT%" 2>&1
echo JAVA_HOME=%JAVA_HOME% >> "%OUT%"
dir /b "C:\Program Files\Java" >> "%OUT%" 2>&1
dir /b "C:\Program Files\Eclipse Adoptium" >> "%OUT%" 2>&1
dir /b "C:\Program Files\Microsoft" 2>nul | findstr /i jdk >> "%OUT%"
dir /b "%USERPROFILE%\.jdks" >> "%OUT%" 2>&1
echo ===== MAVEN ===== >> "%OUT%"
where mvn >> "%OUT%" 2>&1
dir /b "%USERPROFILE%\.m2" >> "%OUT%" 2>&1
dir /b "%USERPROFILE%\.m2\repository\org\springframework\boot\spring-boot" >> "%OUT%" 2>&1
dir /b "%USERPROFILE%\.m2\wrapper\dists" >> "%OUT%" 2>&1
echo ===== IDE ===== >> "%OUT%"
dir /b "C:\Program Files\JetBrains" >> "%OUT%" 2>&1
echo ===== POSTGRES ===== >> "%OUT%"
where psql >> "%OUT%" 2>&1
dir /b "C:\Program Files\PostgreSQL" >> "%OUT%" 2>&1
sc query state= all | findstr /i "postgres" >> "%OUT%" 2>&1
netstat -ano | findstr ":5432 :8080" >> "%OUT%" 2>&1
dir /b "C:\Program Files\pgAdmin 4" >> "%OUT%" 2>&1
echo ===== NET ===== >> "%OUT%"
powershell -NoProfile -Command "try{(Invoke-WebRequest -UseBasicParsing -Uri https://repo.maven.apache.org/maven2/ -TimeoutSec 10).StatusCode}catch{$_.Exception.Message}" >> "%OUT%" 2>&1
echo DONE >> "%OUT%"
