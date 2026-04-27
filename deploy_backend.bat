@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ========================================
REM   智语·云枢 (SapientiaCloud-EduPivot) 
REM   交互式单微服务一键部署脚本
REM ========================================

REM --- 服务器全局配置 ---
set SERVER_PASSWORD=Ez54088zz
set SERVER_USER=root
set SERVER_IP=117.72.194.197
set SERVER_PATH=/root/SapientiaCloud-EduPivot
set VERSION=1.0.0

:MENU
cls
echo ========================================================
echo       智语·云枢 (SapientiaCloud-EduPivot) 部署工具
echo ========================================================
echo 请选择要打包并部署的微服务模块：
echo.
echo   [1] celestial-hub (注册中心/配置中心)
echo   [2] system        (系统服务)
echo   [3] auth          (认证服务)
echo   [4] minIO         (文件服务)
echo   [5] student       (学生服务)
echo   [6] teacher       (教师服务)
echo   [7] course        (课程服务)
echo   [8] classroom     (课堂服务)
echo   [9] live          (直播服务)
echo   [10] gateway      (网关服务)
echo.
echo   [0] 退出脚本
echo ========================================================
set /p choice="请输入对应的数字 (0-10): "

REM --- 匹配用户输入 ---
set MODULE_KEY=
if "%choice%"=="1" set MODULE_KEY=celestial-hub
if "%choice%"=="2" set MODULE_KEY=system
if "%choice%"=="3" set MODULE_KEY=auth
if "%choice%"=="4" set MODULE_KEY=minIO
if "%choice%"=="5" set MODULE_KEY=student
if "%choice%"=="6" set MODULE_KEY=teacher
if "%choice%"=="7" set MODULE_KEY=course
if "%choice%"=="8" set MODULE_KEY=classroom
if "%choice%"=="9" set MODULE_KEY=live
if "%choice%"=="10" set MODULE_KEY=gateway
if "%choice%"=="0" exit /b 0

if not defined MODULE_KEY (
    echo.
    echo ❌ 输入无效，请重新输入 0-10 之间的数字！
    timeout /t 2 >nul
    goto MENU
)

REM --- 自动推断本地 Maven 模块对应的文件夹名称 ---
set MAVEN_MODULE_DIR=SapientiaCloud-EduPivot-!MODULE_KEY!
if not exist "!MAVEN_MODULE_DIR!\" (
    set MAVEN_MODULE_DIR=SapientiaCloud-EduPivot--!MODULE_KEY!
)
if not exist "!MAVEN_MODULE_DIR!\" (
    set MAVEN_MODULE_DIR=!MODULE_KEY!
)

REM --- 拼接 JAR 包名称和本地路径 ---
set JAR_NAME=SapientiaCloud-EduPivot--!MODULE_KEY!-%VERSION%.jar
set LOCAL_JAR_PATH=.\!MAVEN_MODULE_DIR!\target\!JAR_NAME!

cls
echo ========================================
echo   即将执行部署任务...
echo   目标微服务: !MODULE_KEY!
echo   本地模块目录: !MAVEN_MODULE_DIR!
echo   目标 JAR 包: !JAR_NAME!
echo ========================================
echo.

REM 检查 plink 是否可用
where plink >nul 2>&1
if errorlevel 1 (
    echo ❌ 未找到 plink，请确认 PuTTY 已安装并加入环境变量。
    pause
    exit /b 1
)

REM === 步骤 1: 本地打包 ===
echo [步骤 1/3] 正在使用 Maven 打包 !MODULE_KEY! 模块...
echo.
call mvn clean package -pl !MAVEN_MODULE_DIR! -am -DskipTests
if errorlevel 1 (
    echo.
    echo ❌ Maven 打包失败！请检查代码或依赖。
    pause
    exit /b 1
)

if not exist "!LOCAL_JAR_PATH!" (
    echo.
    echo ❌ 找不到生成的 JAR 包: !LOCAL_JAR_PATH!
    echo 请检查 pom.xml 中的 finalName 配置。
    pause
    exit /b 1
)
echo.
echo ✓ !MODULE_KEY! 打包成功！
echo.

REM === 步骤 2: 传输文件 ===
echo [步骤 2/3] 正在上传 !JAR_NAME! 到服务器...
echo.
echo %SERVER_PASSWORD%| plink -ssh -pw %SERVER_PASSWORD% %SERVER_USER%@%SERVER_IP% "mkdir -p %SERVER_PATH%"

echo y | pscp -pw %SERVER_PASSWORD% !LOCAL_JAR_PATH! %SERVER_USER%@%SERVER_IP%:%SERVER_PATH%/!JAR_NAME!
if errorlevel 1 (
    echo.
    echo ❌ 文件传输失败！
    pause
    exit /b 1
)
echo.
echo ✓ 文件传输完毕！
echo.

REM === 步骤 3: 远程启动 ===
echo [步骤 3/3] 正在服务器上重启 !MODULE_KEY! 服务...
echo.

REM 修复说明：去除了 $ 前的转义符，并将远端 Linux 的输出改为英文，彻底杜绝乱码和截断错误
set REMOTE_CMD="cd %SERVER_PATH%; PID=$(ps -ef | grep !JAR_NAME! | grep -v grep | awk '{print $2}'); if [ -n \"$PID\" ]; then kill -9 $PID; echo '[-] Old process (PID: '$PID') killed.'; else echo '[-] No old process found.'; fi; mkdir -p logs; echo '[+] Starting new service...'; nohup java -Xms128m -Xmx256m -jar !JAR_NAME! > logs/!MODULE_KEY!.log 2>&1 & echo '[Success] Done! Check log at: logs/!MODULE_KEY!.log'"

echo %SERVER_PASSWORD%| plink -ssh -pw %SERVER_PASSWORD% %SERVER_USER%@%SERVER_IP% !REMOTE_CMD!

echo.
echo ========================================
echo 🎉 !MODULE_KEY! 微服务自动化部署完成！
echo ========================================
pause
goto MENU