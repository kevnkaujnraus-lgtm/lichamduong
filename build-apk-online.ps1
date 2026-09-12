```powershell
# ============================================================
# LichAmDuong - Build APK Online bằng GitHub Actions
# Repository:
# https://github.com/kevnkaujnraus-lgtm/lichamduong
#
# Chạy file này tại:
# C:\Users\User\Desktop\api\LichAmDuong
#
# PowerShell:
# Set-ExecutionPolicy -Scope Process Bypass
# .\build-apk-online.ps1
# ============================================================

$ErrorActionPreference = "Stop"

# ------------------------------------------------------------
# CẤU HÌNH
# ------------------------------------------------------------

$ProjectDir = "C:\Users\User\Desktop\api\LichAmDuong"

$GitHubUser  = "kevnkaujnraus-lgtm"
$GitHubEmail = "kevnkaujnraus@gmail.com"
$RepoName   = "lichamduong"
$RepoUrl    = "https://github.com/$GitHubUser/$RepoName.git"

$WorkflowDir  = Join-Path $ProjectDir ".github\workflows"
$WorkflowFile = Join-Path $WorkflowDir "build-apk.yml"
$SpecFile     = Join-Path $ProjectDir "buildozer.spec"

# ------------------------------------------------------------
# HÀM HIỂN THỊ
# ------------------------------------------------------------

function Write-Step {
    param([string]$Text)

    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host $Text -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor Cyan
}

function Write-OK {
    param([string]$Text)
    Write-Host "[OK] $Text" -ForegroundColor Green
}

function Write-Warn {
    param([string]$Text)
    Write-Host "[WARN] $Text" -ForegroundColor Yellow
}

function Write-Err {
    param([string]$Text)
    Write-Host "[ERROR] $Text" -ForegroundColor Red
}

function Test-Command {
    param([string]$CommandName)

    return $null -ne (Get-Command $CommandName -ErrorAction SilentlyContinue)
}

# ------------------------------------------------------------
# BẮT ĐẦU
# ------------------------------------------------------------

Clear-Host

Write-Host ""
Write-Host "============================================================" -ForegroundColor Magenta
Write-Host "       LICHAMDUONG - ONLINE APK BUILDER" -ForegroundColor Magenta
Write-Host "============================================================" -ForegroundColor Magenta
Write-Host ""
Write-Host "GitHub : https://github.com/$GitHubUser/$RepoName" -ForegroundColor White
Write-Host "Email  : $GitHubEmail" -ForegroundColor White
Write-Host ""

# ------------------------------------------------------------
# KIỂM TRA THƯ MỤC
# ------------------------------------------------------------

Write-Step "1. Kiểm tra thư mục dự án"

if (-not (Test-Path $ProjectDir)) {
    Write-Err "Không tìm thấy thư mục:"
    Write-Host $ProjectDir

    Write-Host ""
    Write-Host "Hãy đặt file PowerShell này vào thư mục LichAmDuong." -ForegroundColor Yellow
    exit 1
}

Set-Location $ProjectDir

Write-OK "Project: $ProjectDir"

# ------------------------------------------------------------
# KIỂM TRA MAIN.PY
# ------------------------------------------------------------

Write-Step "2. Kiểm tra mã nguồn"

$MainPy = Join-Path $ProjectDir "main.py"

if (-not (Test-Path $MainPy)) {
    Write-Err "Không tìm thấy main.py."
    Write-Host "Thư mục hiện tại không giống project LichAmDuong."
    exit 1
}

Write-OK "Tìm thấy main.py"

# ------------------------------------------------------------
# KIỂM TRA GIT
# ------------------------------------------------------------

Write-Step "3. Kiểm tra Git"

if (-not (Test-Command "git")) {
    Write-Err "Chưa cài Git."

    Write-Host ""
    Write-Host "Cài Git for Windows tại:" -ForegroundColor Yellow
    Write-Host "https://git-scm.com/download/win"
    Write-Host ""

    exit 1
}

$GitVersion = git --version
Write-OK $GitVersion

# ------------------------------------------------------------
# KIỂM TRA GH CLI
# ------------------------------------------------------------

Write-Step "4. Kiểm tra GitHub CLI"

$HasGh = Test-Command "gh"

if ($HasGh) {

    Write-OK "Đã tìm thấy GitHub CLI."

    try {
        $GhStatus = gh auth status 2>&1

        if ($LASTEXITCODE -ne 0) {
            Write-Warn "GitHub CLI chưa đăng nhập."

            Write-Host ""
            Write-Host "Mở cửa sổ đăng nhập GitHub..." -ForegroundColor Yellow
            Write-Host ""

            gh auth login

            if ($LASTEXITCODE -ne 0) {
                Write-Err "Đăng nhập GitHub thất bại."
                exit 1
            }
        }
        else {
            Write-OK "GitHub CLI đã đăng nhập."
        }
    }
    catch {
        Write-Warn "Không thể kiểm tra trạng thái GitHub CLI."
    }

}
else {

    Write-Warn "Không tìm thấy GitHub CLI (gh)."

    Write-Host ""
    Write-Host "Script vẫn có thể sử dụng Git Credential Manager." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Nếu push thất bại, hãy cài GitHub CLI:"
    Write-Host "https://cli.github.com/"
    Write-Host ""
}

# ------------------------------------------------------------
# GIT CONFIG
# ------------------------------------------------------------

Write-Step "5. Cấu hình Git"

git config user.name "kevnkaujnraus-lgtm"
git config user.email $GitHubEmail

Write-OK "Git user.name  = kevnkaujnraus-lgtm"
Write-OK "Git user.email = $GitHubEmail"

# ------------------------------------------------------------
# KHỞI TẠO GIT
# ------------------------------------------------------------

Write-Step "6. Chuẩn bị Git repository"

if (-not (Test-Path (Join-Path $ProjectDir ".git"))) {

    Write-Host "Chưa có Git repository. Đang khởi tạo..." -ForegroundColor Yellow

    git init

    if ($LASTEXITCODE -ne 0) {
        Write-Err "git init thất bại."
        exit 1
    }

    Write-OK "Đã git init."
}
else {
    Write-OK "Project đã là Git repository."
}

# ------------------------------------------------------------
# REMOTE
# ------------------------------------------------------------

Write-Step "7. Cấu hình GitHub remote"

$Remotes = @(git remote)

if ($Remotes -contains "origin") {

    $CurrentOrigin = git remote get-url origin

    if ($CurrentOrigin -ne $RepoUrl) {

        Write-Warn "origin hiện tại:"
        Write-Host $CurrentOrigin

        git remote set-url origin $RepoUrl

        Write-OK "Đã đổi origin thành:"
        Write-Host $RepoUrl
    }
    else {
        Write-OK "origin đã đúng."
    }

}
else {

    git remote add origin $RepoUrl

    if ($LASTEXITCODE -ne 0) {
        Write-Err "Không thể tạo GitHub remote."
        exit 1
    }

    Write-OK "Đã thêm origin:"
    Write-Host $RepoUrl
}

# ------------------------------------------------------------
# TẠO WORKFLOW DIRECTORY
# ------------------------------------------------------------

Write-Step "8. Tạo GitHub Actions workflow"

New-Item -ItemType Directory -Force -Path $WorkflowDir | Out-Null

# ------------------------------------------------------------
# BUILD.OZER SPEC
# ------------------------------------------------------------

Write-Step "9. Tạo buildozer.spec"

$BuildozerSpec = @'
[app]

# ------------------------------------------------------------
# Thông tin ứng dụng
# ------------------------------------------------------------

title = Lich Am Duong
package.name = lichamduong
package.domain = com.kevnkaujnraus

source.dir = .
source.include_exts = py,png,jpg,jpeg,kv,json,txt,ini,atlas,ttf,ico

version = 1.0.0

# ------------------------------------------------------------
# Python / Kivy
# ------------------------------------------------------------

requirements = python3,kivy

orientation = portrait

fullscreen = 0

# ------------------------------------------------------------
# Android
# ------------------------------------------------------------

android.api = 35
android.minapi = 23
android.ndk = 27c

android.accept_sdk_license = True

android.archs = arm64-v8a, armeabi-v7a

# ------------------------------------------------------------
# Permissions
# ------------------------------------------------------------

android.permissions = INTERNET

# ------------------------------------------------------------
# Icon / Presplash
# ------------------------------------------------------------

# Nếu project có icon.png thì Buildozer sẽ sử dụng.
# icon.filename = %(source.dir)s/icon.png

# presplash.filename = %(source.dir)s/presplash.png

# ------------------------------------------------------------
# Log
# ------------------------------------------------------------

log_level = 2

# ------------------------------------------------------------
# Buildozer
# ------------------------------------------------------------

[buildozer]

log_level = 2
warn_on_root = 1
'@

Set-Content -Path $SpecFile -Value $BuildozerSpec -Encoding UTF8

Write-OK "Đã tạo buildozer.spec"

# ------------------------------------------------------------
# GITHUB ACTIONS
# ------------------------------------------------------------

$Workflow = @'
name: Build LichAmDuong APK

on:
  push:
    branches:
      - main
      - master

  workflow_dispatch:

permissions:
  contents: read

jobs:

  build:

    name: Build Android APK

    runs-on: ubuntu-22.04

    timeout-minutes: 60

    steps:

      # ------------------------------------------------------
      # Checkout
      # ------------------------------------------------------

      - name: Checkout source
        uses: actions/checkout@v4

      # ------------------------------------------------------
      # Python
      # ------------------------------------------------------

      - name: Setup Python 3.12
        uses: actions/setup-python@v5
        with:
          python-version: "3.12"

      # ------------------------------------------------------
      # Java
      # ------------------------------------------------------

      - name: Setup Java 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "17"

      # ------------------------------------------------------
      # Android / Build dependencies
      # ------------------------------------------------------

      - name: Install system dependencies
        run: |
          sudo apt-get update

          sudo apt-get install -y \
            git \
            zip \
            unzip \
            openjdk-17-jdk \
            autoconf \
            automake \
            libtool \
            pkg-config \
            zlib1g-dev \
            libncurses5-dev \
            libncursesw5-dev \
            libtinfo5 \
            cmake \
            libffi-dev \
            libssl-dev

      # ------------------------------------------------------
      # Python dependencies
      # ------------------------------------------------------

      - name: Upgrade pip
        run: |
          python -m pip install --upgrade pip setuptools wheel

      - name: Install Buildozer
        run: |
          pip install "cython<3"
          pip install buildozer

      # ------------------------------------------------------
      # Cache Buildozer / Android SDK
      # ------------------------------------------------------

      - name: Cache Buildozer
        uses: actions/cache@v4
        with:
          path: |
            ~/.buildozer
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: buildozer-${{ runner.os }}-${{ hashFiles('buildozer.spec', 'requirements.txt') }}
          restore-keys: |
            buildozer-${{ runner.os }}-

      # ------------------------------------------------------
      # Build APK
      # ------------------------------------------------------

      - name: Build APK
        run: |
          yes | buildozer android debug

      # ------------------------------------------------------
      # Show APK
      # ------------------------------------------------------

      - name: List generated APK
        run: |
          echo "===== APK FILES ====="
          find bin -type f -name "*.apk" -print

      # ------------------------------------------------------
      # Upload APK
      # ------------------------------------------------------

      - name: Upload APK artifact
        uses: actions/upload-artifact@v4
        with:
          name: LichAmDuong-APK
          path: bin/*.apk
          if-no-files-found: error
          retention-days: 30
'@

Set-Content -Path $WorkflowFile -Value $Workflow -Encoding UTF8

Write-OK "Đã tạo:"
Write-Host $WorkflowFile

# ------------------------------------------------------------
# GITIGNORE
# ------------------------------------------------------------

Write-Step "10. Tạo .gitignore"

$GitIgnoreFile = Join-Path $ProjectDir ".gitignore"

$GitIgnore = @'
# Python
__pycache__/
*.py[cod]
*.pyo

# Virtual environment
.venv/
venv/
env/

# Buildozer
.buildozer/
bin/

# Android
*.apk
*.aab

# IDE
.vscode/
.idea/

# OS
Thumbs.db
.DS_Store

# Logs
*.log

# Temporary
*.tmp
*.temp
'@

Set-Content -Path $GitIgnoreFile -Value $GitIgnore -Encoding UTF8

Write-OK ".gitignore đã được tạo."

# ------------------------------------------------------------
# KIỂM TRA CÁC FILE
# ------------------------------------------------------------

Write-Step "11. Kiểm tra project trước khi push"

$RequiredFiles = @(
    "main.py",
    "buildozer.spec",
    ".github\workflows\build-apk.yml"
)

$AllGood = $true

foreach ($File in $RequiredFiles) {

    $FullPath = Join-Path $ProjectDir $File

    if (Test-Path $FullPath) {
        Write-OK $File
    }
    else {
        Write-Err "Thiếu $File"
        $AllGood = $false
    }
}

if (-not $AllGood) {
    Write-Err "Project chưa sẵn sàng."
    exit 1
}

# ------------------------------------------------------------
# KIỂM TRA GIT STATUS
# ------------------------------------------------------------

Write-Step "12. Kiểm tra thay đổi"

git status --short

# ------------------------------------------------------------
# ADD
# ------------------------------------------------------------

Write-Step "13. Add toàn bộ source"

git add .

if ($LASTEXITCODE -ne 0) {
    Write-Err "git add thất bại."
    exit 1
}

Write-OK "git add hoàn tất."

# ------------------------------------------------------------
# COMMIT
# ------------------------------------------------------------

Write-Step "14. Commit"

$StatusAfterAdd = git status --porcelain

if ([string]::IsNullOrWhiteSpace(($StatusAfterAdd -join "`n"))) {

    Write-Warn "Không có thay đổi mới để commit."

}
else {

    $CommitMessage = "Build LichAmDuong APK online"

    git commit -m $CommitMessage

    if ($LASTEXITCODE -ne 0) {
        Write-Err "git commit thất bại."
        exit 1
    }

    Write-OK "Commit thành công."
}

# ------------------------------------------------------------
# XÁC ĐỊNH BRANCH
# ------------------------------------------------------------

Write-Step "15. Chuẩn bị branch main"

$CurrentBranch = git branch --show-current

if ([string]::IsNullOrWhiteSpace($CurrentBranch)) {

    git checkout -b main

}
elseif ($CurrentBranch -ne "main") {

    Write-Host "Branch hiện tại: $CurrentBranch"

    git branch -M main
}

Write-OK "Branch: main"

# ------------------------------------------------------------
# PUSH
# ------------------------------------------------------------

Write-Step "16. Push source lên GitHub"

Write-Host ""
Write-Host "Repository:" -ForegroundColor Yellow
Write-Host $RepoUrl -ForegroundColor White
Write-Host ""

Write-Host "Đang push..." -ForegroundColor Yellow

git push -u origin main

if ($LASTEXITCODE -ne 0) {

    Write-Host ""
    Write-Err "Push lên GitHub thất bại."

    Write-Host ""
    Write-Host "Nếu Git yêu cầu đăng nhập, hãy thực hiện:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  gh auth login" -ForegroundColor White
    Write-Host ""
    Write-Host "hoặc đăng nhập GitHub thông qua Git Credential Manager."
    Write-Host ""

    exit 1
}

Write-OK "Đã push source lên GitHub."

# ------------------------------------------------------------
# KIỂM TRA WORKFLOW
# ------------------------------------------------------------

Write-Step "17. GitHub Actions đang build APK"

Write-Host ""
Write-Host "GitHub Repository:" -ForegroundColor Yellow
Write-Host "https://github.com/$GitHubUser/$RepoName"
Write-Host ""

Write-Host "Actions:" -ForegroundColor Yellow
Write-Host "https://github.com/$GitHubUser/$RepoName/actions"
Write-Host ""

# ------------------------------------------------------------
# GH CLI THEO DÕI WORKFLOW
# ------------------------------------------------------------

if ($HasGh) {

    Write-Host "Kiểm tra GitHub Actions..." -ForegroundColor Yellow

    Start-Sleep -Seconds 5

    try {

        $Runs = gh run list `
            --repo "$GitHubUser/$RepoName" `
            --limit 3 `
            --json status,conclusion,name,url,headBranch `
            --template '{{range .}}{{.name}} | {{.status}} | {{.conclusion}} | {{.url}}{{"\n"}}{{end}}'

        if ($Runs) {
            Write-Host ""
            Write-Host $Runs
        }

    }
    catch {
        Write-Warn "Chưa lấy được trạng thái Actions."
    }
}

# ------------------------------------------------------------
# KẾT THÚC
# ------------------------------------------------------------

Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "                 HOÀN TẤT GỬI BUILD" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host ""

Write-Host "GitHub:" -ForegroundColor Cyan
Write-Host "https://github.com/$GitHubUser/$RepoName"

Write-Host ""
Write-Host "GitHub Actions:" -ForegroundColor Cyan
Write-Host "https://github.com/$GitHubUser/$RepoName/actions"

Write-Host ""
Write-Host "Sau khi build thành công:" -ForegroundColor Yellow
Write-Host "1. Mở GitHub Actions"
Write-Host "2. Chọn workflow 'Build LichAmDuong APK'"
Write-Host "3. Chọn lần chạy mới nhất"
Write-Host "4. Kéo xuống phần Artifacts"
Write-Host "5. Tải 'LichAmDuong-APK'"

Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host ""

# Mở GitHub Actions trên trình duyệt
try {
    Start-Process "https://github.com/$GitHubUser/$RepoName/actions"
}
catch {
    # Không làm gì nếu không mở được trình duyệt
}

Write-Host "Script đã hoàn tất." -ForegroundColor Green
Write-Host ""
```
