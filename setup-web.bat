@echo off
echo Installing Edge Detection Web Interface...
echo.

REM Check if Node.js is installed
node --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Node.js is not installed or not in PATH
    echo Please install Node.js from https://nodejs.org/
    pause
    exit /b 1
)

echo Node.js detected: 
node --version

echo.
echo Navigating to web interface directory...
cd web-interface

echo.
echo Installing npm dependencies...
npm install

if errorlevel 1 (
    echo.
    echo ERROR: npm install failed
    pause
    exit /b 1
)

echo.
echo ✅ Installation completed successfully!
echo.
echo To start the development server, run:
echo   cd web-interface
echo   npm run dev
echo.
echo Then open http://localhost:3000 in your browser
echo.
pause