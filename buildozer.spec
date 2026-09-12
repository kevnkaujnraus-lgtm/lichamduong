[app]

# Lich Am Duong - stable Android build configuration
title = Lich Am Duong
package.name = lichamduong
package.domain = com.kevnkaujnraus

source.dir = .
source.include_exts = py,png,jpg,jpeg,kv,json,txt,ini,atlas,ttf,ico
source.exclude_dirs = .git,.github,.buildozer,bin,__pycache__,tests

version = 1.0.0

# Keep the Android Python runtime explicit. p4a 2026.05.09 has stable
# support for Python 3.11; pinning avoids accidental Python 3.14 builds.
requirements = python3==3.11.9,kivy==2.3.1

orientation = portrait
fullscreen = 0

# Android toolchain
android.api = 35
android.minapi = 24
android.ndk = 28c
android.accept_sdk_license = True
android.archs = arm64-v8a

# Pin python-for-android to the latest stable release commit instead of HEAD.
p4a.fork = kivy
p4a.branch = master
p4a.commit = 58d2114

android.permissions = INTERNET
android.allow_backup = True
android.debug_artifact = apk
android.release_artifact = aab

log_level = 2

[buildozer]
log_level = 2
warn_on_root = 1
