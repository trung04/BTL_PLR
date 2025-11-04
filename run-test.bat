@echo off
echo ============================================================
echo  COMPILE VA CHAY TEST HE THONG MOI CHOI
echo ============================================================
echo.

REM Di chuyen den thu muc goc
cd /d "%~dp0"

echo [1/4] Tao thu muc build...
if not exist "build\classes" mkdir "build\classes"

echo [2/4] Compile tat ca file Java...
javac -d build\classes -encoding UTF-8 -sourcepath src src\btl\*.java src\controller\*.java src\model\*.java src\database\*.java src\test\*.java
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compile that bai!
    pause
    exit /b 1
)

echo [3/4] Compile thanh cong!
echo.
echo ============================================================
echo  LUA CHON CACH TEST
echo ============================================================
echo  1. Chay Server
echo  2. Chay Test Client Thu Cong (Manual Test)
echo  3. Chay Test Tu Dong (Auto Test)
echo ============================================================
echo.

set /p choice="Nhap lua chon (1/2/3): "

if "%choice%"=="1" (
    echo.
    echo [4/4] Khoi dong Server...
    echo ============================================================
    java -cp build\classes btl.ServerMain
) else if "%choice%"=="2" (
    echo.
    echo [4/4] Khoi dong Test Client Thu Cong...
    echo ============================================================
    echo Luu y: Can chay server truoc khi chay client!
    echo Mo nhieu terminal va chay script nay voi lua chon 2
    echo de test voi nhieu client!
    echo ============================================================
    echo.
    java -cp build\classes test.ManualInvitationTest
) else if "%choice%"=="3" (
    echo.
    echo [4/4] Chay Test Tu Dong...
    echo ============================================================
    echo Luu y: Can chay server truoc!
    echo ============================================================
    echo.
    java -cp build\classes test.InvitationSystemTest
) else (
    echo.
    echo [ERROR] Lua chon khong hop le!
    pause
    exit /b 1
)

echo.
echo ============================================================
echo HOAN THANH!
echo ============================================================
pause
