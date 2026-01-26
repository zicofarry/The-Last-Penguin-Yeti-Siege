@echo off
setlocal enabledelayedexpansion

echo ============================================
echo   The Last Penguin: Yeti Siege - Builder
echo ============================================
echo.

:: Configuration
set APP_NAME=TheLastPenguin
set MAIN_CLASS=com.lastpenguin.Main
set VERSION=1.0.0

:: Check Java version
echo [1/5] Checking Java installation...
java -version 2>&1 | findstr /i "version" >nul
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH!
    echo Please install JDK 17+ from: https://adoptium.net/
    pause
    exit /b 1
)

:: Clean previous builds
echo [2/5] Cleaning previous builds...
if exist build rmdir /s /q build
if exist dist rmdir /s /q dist
mkdir build
mkdir dist
mkdir dist\lib

:: Compile Java sources
echo [3/5] Compiling Java sources...
dir /s /b src\*.java > sources.txt
javac -d build -cp "lib/*" @sources.txt
if errorlevel 1 (
    echo ERROR: Compilation failed!
    del sources.txt
    pause
    exit /b 1
)
del sources.txt
echo     Compilation successful!

:: Copy resources to build folder
echo [4/5] Copying resources...
xcopy /s /e /q /y res\* build\ >nul

:: Create JAR file
echo [5/5] Creating JAR file...
copy lib\*.jar dist\lib\ >nul
jar cfm dist\%APP_NAME%.jar MANIFEST.MF -C build .
if errorlevel 1 (
    echo ERROR: JAR creation failed!
    pause
    exit /b 1
)

echo.
echo ============================================
echo   BUILD SUCCESSFUL!
echo ============================================
echo.
echo JAR file created: dist\%APP_NAME%.jar
echo.
echo To run the game:
echo   cd dist
echo   java -jar %APP_NAME%.jar
echo.
echo ============================================
echo   Creating Windows Installer (EXE)...
echo ============================================
echo.

:: Check if jpackage exists
where jpackage >nul 2>&1
if errorlevel 1 (
    echo WARNING: jpackage not found!
    echo You need JDK 14+ to create EXE installer.
    echo.
    echo You can still run the game using:
    echo   java -jar dist\%APP_NAME%.jar
    echo.
    pause
    exit /b 0
)

:: Create installer directory
if exist installer rmdir /s /q installer
mkdir installer

:: Copy config file to dist for packaging
copy config.properties dist\ >nul 2>&1

:: Run jpackage to create EXE installer
echo Creating EXE installer with jpackage...
jpackage ^
    --type exe ^
    --name "%APP_NAME%" ^
    --app-version %VERSION% ^
    --vendor "Last Penguin Studio" ^
    --description "The Last Penguin: Yeti Siege - A Java Game" ^
    --input dist ^
    --main-jar %APP_NAME%.jar ^
    --main-class %MAIN_CLASS% ^
    --dest installer ^
    --win-dir-chooser ^
    --win-menu ^
    --win-shortcut

if errorlevel 1 (
    echo.
    echo WARNING: jpackage failed!
    echo.
    echo Possible causes:
    echo   1. WiX Toolset not installed (required for EXE)
    echo      Download: https://wixtoolset.org/releases/
    echo   2. Insufficient permissions
    echo.
    echo Alternative: Create MSI installer instead:
    echo   jpackage --type msi ...
    echo.
    pause
    exit /b 1
)

echo.
echo ============================================
echo   INSTALLER CREATED SUCCESSFULLY!
echo ============================================
echo.
echo Installer location: installer\%APP_NAME%-%VERSION%.exe
echo.
echo Share this installer with others!
echo They don't need to install Java separately.
echo.
pause
