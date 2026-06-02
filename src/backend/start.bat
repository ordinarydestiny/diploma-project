@echo off
chcp 65001 >nul
echo ============================================
echo   毕设管理系统 - 后端服务启动脚本
echo ============================================
echo.

set JAVA_HOME=D:\java17
set PATH=%JAVA_HOME%\bin;%PATH%

echo [INFO] 切换终端编码为 UTF-8...
echo [INFO] 启动 Spring Boot 服务...
echo.

"%JAVA_HOME%\bin\java" -Dfile.encoding=UTF-8 -jar target\internship-system-1.0.0.jar

pause