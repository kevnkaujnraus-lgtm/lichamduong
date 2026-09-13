[app]
title = Lịch Âm Dương
package.name = lichamduong
package.domain = org.lichamduong
source.dir = .
source.include_exts = py
version = 1.0.0
requirements = python3,kivy
orientation = portrait
fullscreen = 0

android.api = 34
android.minapi = 23
android.ndk = 25b
android.ndk_api = 23
android.archs = arm64-v8a, armeabi-v7a
android.permissions =
android.accept_sdk_license = True

# Ghim python-for-android về bản ổn định (tránh bản master mới nhất
# đang lỗi khi build với Python 3.14 cho Android)
p4a.branch = v2024.01.21

[buildozer]
log_level = 2
warn_on_root = 1
