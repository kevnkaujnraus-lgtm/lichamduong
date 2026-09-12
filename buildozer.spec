[app]

# (str) Title of your application

title = Lịch Âm Dương

# (str) Package name

package.name = lichamduong

# (str) Package domain

package.domain = org.lichamduong

# (str) Source code directory

source.dir = .

# (str) Application version

version = 1.0.0

# (str) Application requirements

requirements = python3,kivy==2.3.1

# (str) Supported source file extensions

source.include_exts = py,png,jpg,jpeg,kv,json,txt

# (str) Application orientation

orientation = portrait

# (bool) Fullscreen mode

fullscreen = 0

# (str) Presplash

# presplash.filename = %(source.dir)s/data/presplash.png

# (str) Icon

# icon.filename = %(source.dir)s/data/icon.png

# (str) Supported Android architectures

android.archs = arm64-v8a, armeabi-v7a

# (int) Android API level

android.api = 35

# (int) Minimum Android API level

android.minapi = 23

# (str) Android permissions

android.permissions =

# (str) Android entry point

# android.entrypoint = org.kivy.android.PythonActivity

# (str) Android application theme

# android.apptheme = "@android:style/Theme.Material.Light.NoActionBar"

# (bool) Enable AndroidX

android.enable_androidx = True

# (bool) Enable Android activity restart on configuration changes

android.allow_backup = True

# (str) Python-for-Android branch

# p4a.branch = master

# (str) Python-for-Android URL

# p4a.url =

# (str) Extra source files

# source.include_patterns =

# (str) Extra exclude files

source.exclude_exts = pyc,pyo

# (str) Extra exclude directories

source.exclude_dirs = **pycache**,.git,.github,.buildozer,bin

# (str) Application log level

log_level = 2

[buildozer]

# (int) Log level

log_level = 2

# (bool) Warn when running Buildozer as root

warn_on_root = 1

[buildozer:android]

# Android specific options

android.api = 35
android.minapi = 23
android.archs = arm64-v8a,armeabi-v7a
