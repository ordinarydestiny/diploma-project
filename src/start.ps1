$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$backendDir = Join-Path $projectRoot "backend"
$frontendDir = Join-Path $projectRoot "frontend"

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  实习与毕设管理系统 - 一键启动脚本" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
if (-not $mvnCmd) {
    Write-Host "[错误] 未找到 Maven (mvn)，请确保已安装并添加到 PATH" -ForegroundColor Red
    exit 1
}

$nodeCmd = Get-Command node -ErrorAction SilentlyContinue
if (-not $nodeCmd) {
    Write-Host "[错误] 未找到 Node.js，请确保已安装并添加到 PATH" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path (Join-Path $frontendDir "node_modules"))) {
    Write-Host "[信息] 前端依赖未安装，正在执行 npm install..." -ForegroundColor Yellow
    Push-Location $frontendDir
    npm install
    Pop-Location
    Write-Host "[完成] 前端依赖安装完毕" -ForegroundColor Green
}

function Stop-ProcessTree {
    param([int]$ProcessId)
    $children = Get-CimInstance Win32_Process | Where-Object {
        $_.ParentProcessId -eq $ProcessId
    }
    foreach ($child in $children) {
        Stop-ProcessTree -ProcessId $child.ProcessId
    }
    Stop-Process -Id $ProcessId -Force -ErrorAction SilentlyContinue
}

function Release-Port {
    param([int]$Port)
    $listeners = netstat -ano | Select-String ":$Port\s.*LISTENING"
    if ($listeners) {
        $pids = $listeners | ForEach-Object { ($_ -split '\s+')[-1] } | Select-Object -Unique
        foreach ($procId in $pids) {
            try {
                $proc = Get-Process -Id $procId -ErrorAction Stop
                Write-Host "[释放] 正在终止占用端口 $Port 的进程: $($proc.ProcessName) (PID: $procId)" -ForegroundColor Yellow
                Stop-ProcessTree -ProcessId $procId
            } catch {
                Write-Host "[释放] 正在终止占用端口 $Port 的进程 (PID: $procId)" -ForegroundColor Yellow
                Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
            }
        }
    }
}

Write-Host ""
Write-Host "[检查] 正在释放被占用的端口..." -ForegroundColor Yellow
Release-Port -Port 8080
Release-Port -Port 5173
Start-Sleep -Seconds 1
Write-Host "[完成] 端口释放完毕" -ForegroundColor Green

Write-Host ""
Write-Host "[启动] 正在启动后端服务 (Spring Boot :8080)..." -ForegroundColor Green
$backendProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "mvn spring-boot:run" -WorkingDirectory $backendDir -PassThru -NoNewWindow:$false

Write-Host "[启动] 正在启动前端服务 (Vite :5173)..." -ForegroundColor Green
$frontendProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "npm run dev" -WorkingDirectory $frontendDir -PassThru -NoNewWindow:$false

Write-Host ""
Write-Host "[等待] 正在等待后端服务启动..." -ForegroundColor Yellow
$backendReady = $false
$waitSeconds = 0
$maxWait = 120
while (-not $backendReady -and $waitSeconds -lt $maxWait) {
    Start-Sleep -Seconds 2
    $waitSeconds += 2
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/doc.html" -Method Head -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
        $backendReady = $true
        Write-Host "[完成] 后端服务已启动 (耗时 ${waitSeconds}s)" -ForegroundColor Green
    } catch {
        Write-Host "." -NoNewline -ForegroundColor Gray
    }
}
if (-not $backendReady) {
    Write-Host ""
    Write-Host "[警告] 后端服务启动超时 (${maxWait}s)，请检查后端窗口日志" -ForegroundColor Red
}

Write-Host "[等待] 正在等待前端服务启动..." -ForegroundColor Yellow
$frontendReady = $false
$waitSeconds = 0
$maxWait = 30
while (-not $frontendReady -and $waitSeconds -lt $maxWait) {
    Start-Sleep -Seconds 1
    $waitSeconds += 1
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:5173" -Method Head -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
        $frontendReady = $true
        Write-Host "[完成] 前端服务已启动 (耗时 ${waitSeconds}s)" -ForegroundColor Green
    } catch {
        Write-Host "." -NoNewline -ForegroundColor Gray
    }
}
if (-not $frontendReady) {
    Write-Host ""
    Write-Host "[警告] 前端服务启动超时 (${maxWait}s)，请检查前端窗口日志" -ForegroundColor Red
}

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  服务启动完成" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  地址: http://localhost:5173" -ForegroundColor White
Write-Host ""
Write-Host "  测试账号 (初始密码均为 123456):" -ForegroundColor Yellow
Write-Host ""
Write-Host "  [纯角色]" -ForegroundColor DarkGray
Write-Host "  admin            校级管理员测试账号" -ForegroundColor White
Write-Host "  student1         学生测试账号" -ForegroundColor White
Write-Host "  teacher1         实习指导教师测试账号" -ForegroundColor White
Write-Host "  dept_admin1      院系管理员测试账号" -ForegroundColor White
Write-Host "  major_dir1       专业负责人测试账号" -ForegroundColor White
Write-Host ""
Write-Host "  [兼任角色，可切换测试]" -ForegroundColor DarkGray
Write-Host "  dept_teacher1    院系管理员兼指导教师测试账号" -ForegroundColor White
Write-Host "  major_teacher1   专业负责人兼指导教师测试账号" -ForegroundColor White
Write-Host ""
Write-Host "  [全角色测试账号]" -ForegroundColor DarkGray
Write-Host "  super_test       全角色测试账号" -ForegroundColor White
Write-Host ""
Write-Host "  按 q 键停止所有服务..." -ForegroundColor Yellow
Write-Host "======================================" -ForegroundColor Cyan

# 等待用户按下 q 键（不区分大小写）
do {
    $key = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
} while ($key.Character -ne 'q' -and $key.Character -ne 'Q')

Write-Host ""
Write-Host "[停止] 正在停止所有服务..." -ForegroundColor Yellow

Stop-ProcessTree -ProcessId $backendProcess.Id
Stop-ProcessTree -ProcessId $frontendProcess.Id

Start-Sleep -Seconds 2

$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {
    $cmdLine = (Get-CimInstance Win32_Process -Filter "ProcessId = $($_.Id)" -ErrorAction SilentlyContinue).CommandLine
    $cmdLine -match "internship-system"
}
$javaProcesses | Stop-Process -Force -ErrorAction SilentlyContinue

$nodeProcesses = Get-Process -Name "node" -ErrorAction SilentlyContinue | Where-Object {
    $cmdLine = (Get-CimInstance Win32_Process -Filter "ProcessId = $($_.Id)" -ErrorAction SilentlyContinue).CommandLine
    $cmdLine -match "vite"
}
$nodeProcesses | Stop-Process -Force -ErrorAction SilentlyContinue

Write-Host "[完成] 所有服务已停止" -ForegroundColor Green
