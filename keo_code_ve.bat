@echo off
cd /d "%~dp0"
rem Keo code moi nhat tu GitHub ve may (nhanh main)
git checkout main > keo_code_ve.log 2>&1
git pull origin main >> keo_code_ve.log 2>&1
echo PULL_EXIT=%errorlevel% >> keo_code_ve.log
