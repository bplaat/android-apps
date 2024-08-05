#!/bin/bash
# --- Bassie Android Build Script v1.3 ---
# The default gradle Android build toolchain is so slow and produces bloated apks,
# so I use this nice build shell script to get the job done!
# - Install OpenJDK JDK 17 or 21
# - Install Android SDK with packages: platform-tools platforms;android-35 build-tools;35.0.0
# - Set $ANDROID_HOME, $name, $package, $password, $main_activity

PATH=$PATH:$ANDROID_HOME/build-tools/35.0.0:$ANDROID_HOME/platform-tools
PLATFORM=$ANDROID_HOME/platforms/android-35/android.jar

if [ -z "$name" ]; then
    echo "Can't run this script directly!"
    exit 1
fi

set -e

if [ "$1" = "clean" ]; then
    rm -rf target
    exit
fi
if [ "$1" = "key" ]; then
    keytool -genkey -validity 7120 -keystore keystore.jks -keyalg RSA -keysize 4096 -storepass "$password" -keypass "$password"
    exit
fi
if [ "$1" = "log" ]; then
    adb logcat -c
    adb logcat "*:E"
    exit
fi
if [ "$1" = "clear" ]; then
    echo "Clearing app data and opening application..."
    adb shell pm clear "$package"
    adb shell am start -n "$package/$main_activity"
    exit
fi

echo "Compiling resources..."
rm -rf target/res && mkdir -p target/res
aapt2 compile --dir res -o target/res
if [ -e assets ]; then
    aapt2 link target/res/*.flat --manifest AndroidManifest.xml -A assets --java src -I "$PLATFORM" -o "target/$name-unaligned.apk"
else
    aapt2 link target/res/*.flat --manifest AndroidManifest.xml --java src -I "$PLATFORM" -o "target/$name-unaligned.apk"
fi

echo "Compiling java code..."
rm -rf target/src && mkdir target/src
find src -name "*.java" > target/sources.txt
javac -Xlint -cp "$PLATFORM" -d target/src @target/sources.txt
rm -f -r src/${package//\./\/}/R.java

echo "Compiling dex, packing and signing application..."
find target/src -name "*.class" > target/classes.txt
if [ -e "$ANDROID_HOME/build-tools/d8.bat" ]; then
    d8.bat --release --lib "$PLATFORM" --min-api 21 --output target/ @target/classes.txt
else
    d8 --release --lib "$PLATFORM" --min-api 21 --output target/ @target/classes.txt
fi
zip -j "target/$name-unaligned.apk" target/classes.dex > /dev/null
zipalign -f -p 4 "target/$name-unaligned.apk" "target/$name.apk"
if [ -e "$ANDROID_HOME/build-tools/apksigner.bat" ]; then
    apksigner.bat sign --v4-signing-enabled false --ks keystore.jks --ks-pass "pass:$password" --ks-pass "pass:$password" "target/$name.apk"
else
    apksigner sign --v4-signing-enabled false --ks keystore.jks --ks-pass "pass:$password" --ks-pass "pass:$password" "target/$name.apk"
fi

echo "Installing and opening application..."
adb install -r "target/$name.apk"
adb shell am start -n "$package/$main_activity"
