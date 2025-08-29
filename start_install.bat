@echo off
setlocal

REM === CONFIGURE THIS ===
set AVD_NAME=Medium_Phone_API_36.0

REM === START EMULATOR ===
echo Starting emulator: %AVD_NAME%
start "" emulator -avd %AVD_NAME%

REM === WAIT UNTIL DEVICE IS ONLINE ===
echo Waiting for device to boot...
adb wait-for-device

:check_boot
adb shell getprop sys.boot_completed | findstr "1" >nul
if errorlevel 1 (
    timeout /t 5 >nul
    goto check_boot
)

echo Emulator booted!

REM === RUN GRADLE BUILD & INSTALL ===
echo Building and installing app with Gradle...
cd /d %~dp0
gradlew installDebug

echo Done.