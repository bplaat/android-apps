#!/bin/bash
# --- Bassie Android Build Script v1.3 ---
# The default gradle Android build toolchain is so slow and produces bloated apks,
# so I use this nice build script to get the job done!
# - Install OpenJDK JDK 17 or 21
# - Install Android SDK with packages: platform-tools platforms;android-35 build-tools;35.0.0
# - Set $ANDROID_HOME, $name, $package, $version, $password, $main_activity

min_sdk_version=21
target_sdk_version=35

################# BUILD SCRIPT #################

if [ -z "$name" ]; then
    echo "You haven't set all the required variables!"
    exit 1
fi

set -e
PATH=$PATH:$ANDROID_HOME/platform-tools:$(find "$ANDROID_HOME/build-tools" -name "$target_sdk_version*")

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

# VSCode Java settings
platform=$ANDROID_HOME/platforms/android-$target_sdk_version/android.jar
if [ ! -e ../.vscode/settings.json ]; then
    mkdir -p ../.vscode
    echo "{\"editor.formatOnSave\":true,\"java.project.sourcePaths\":["> ../.vscode/settings.json
    for dir in $(find .. -name "src" ! -path "*/target/*") $(find .. -name "src-gen"); do
        echo "\"${dir:1}\"," >> ../.vscode/settings.json
    done
    echo "],\"java.project.referencedLibraries\":[\"$platform\"],\"rust-analyzer.linkedProjects\":[" >> ../.vscode/settings.json
    for file in $(find .. -name "Cargo.toml"); do
        echo "\"${file:1}\"," >> ../.vscode/settings.json
    done
    echo "]}" >> ../.vscode/settings.json
fi

echo "Compiling resources..."
mkdir -p target/res && rm -f target/res/*
aapt2 compile --dir res --no-crunch -o target/res
IFS="." read -r version_major version_minor version_patch <<< "$version"
version_code=$((version_major * 10000 + version_minor * 100 + version_patch))
if [ -e assets ]; then
    aapt2 link target/res/*.flat --manifest AndroidManifest.xml -A assets --java target/src-gen \
        --version-name "$version" --version-code "$version_code" --min-sdk-version "$min_sdk_version" \
        --target-sdk-version "$target_sdk_version" -I "$platform" -o "target/$name-unaligned.apk"
else
    aapt2 link target/res/*.flat --manifest AndroidManifest.xml --java target/src-gen \
        --version-name "$version" --version-code "$version_code" --min-sdk-version "$min_sdk_version" \
        --target-sdk-version "$target_sdk_version" -I "$platform" -o "target/$name-unaligned.apk"
fi

echo "Compiling java..."
rm -rf target/src
find src target/src-gen -name "*.java" > target/sources.txt
javac -Xlint -cp "$platform" -d target/src @target/sources.txt

echo "Compiling dex..."
find target/src -name "*.class" > target/classes.txt
if [ -e "$ANDROID_HOME/build-tools/d8.bat" ]; then
    d8.bat --release --lib "$platform" --min-api "$min_sdk_version" --output target/ @target/classes.txt
else
    d8 --release --lib "$platform" --min-api "$min_sdk_version" --output target/ @target/classes.txt
fi

echo "Packing and signing application..."
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
