# NAVI MEAL (Android, Kotlin + Jetpack Compose)

## Requirements
- Android Studio (Koala or newer) with JDK 17 (bundled), and Android SDK 34
- A phone with USB debugging on, or an emulator

## Run it (easiest: Android Studio)
1. Unzip, then File > Open > select the `navi-meal-android` folder
2. Wait for Gradle sync to finish
3. Pick your phone/emulator in the toolbar and press Run

## Run it (command line)
```
cd navi-meal-android
gradle wrapper --gradle-version 8.7     # one-time (needs Gradle installed)
./gradlew assembleDebug                 # APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug                  # installs on connected device/emulator
adb shell am start -n com.navimeal/.MainActivity
```
Windows: use `gradlew.bat` instead of `./gradlew`.

## Get an APK link without Android Studio
Push this folder to a GitHub repo; the Actions workflow in `.github/workflows` builds the APK,
downloadable from the run's "Artifacts".
