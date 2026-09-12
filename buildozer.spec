[app]

# ------------------------------------------------------------
# ThÃ´ng tin á»©ng dá»¥ng
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

# Náº¿u project cÃ³ icon.png thÃ¬ Buildozer sáº½ sá»­ dá»¥ng.
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
