#!/bin/bash

# The default gradle Android build toolchain is slow on my old laptop and produces bloated apks
# So I use this bullshit build script to get the job done!

PATH=$PATH:~/android-sdk/build-tools/30.0.3:~/android-sdk/platform-tools
PLATFORM=~/android-sdk/platforms/android-30/android.jar

name="reactdroid"
package="ml.bastiaan.reactdroid"
password="android"

if [ "$1" == "key" ]; then
    keytool -genkey -validity 7120 -keystore keystore.jks -keyalg RSA -keysize 4096 -storepass $password -keypass $password

elif [ "$1" == "log" ]; then
    adb logcat -c
    adb logcat *:E

elif [ "$1" == "clear" ]; then
    adb shell pm clear $package
    adb shell am start -n $package/.MainActivity

else
    mkdir res-compiled
    if aapt2 compile --dir res -o res-compiled; then
        if aapt2 link res-compiled/*.flat --manifest AndroidManifest.xml --java src -I $PLATFORM -o $name-unaligned.apk; then

            mkdir src-compiled
            find src -name *.java > sources.txt
            if javac -Xlint -cp $PLATFORM -d src-compiled @sources.txt; then

                find src-compiled -name *.class > classes.txt
                if [ "$(uname -s)" == "Linux" ]; then
                    d8 --release --lib $PLATFORM --min-api 21 @classes.txt
                else
                    d8.bat --release --lib $PLATFORM --min-api 21 @classes.txt
                fi
                aapt add $name-unaligned.apk classes.dex > /dev/null

                zipalign -f -p 4 $name-unaligned.apk $name.apk

                if [ "$(uname -s)" == "Linux" ]; then
                    apksigner sign --v4-signing-enabled false --ks keystore.jks --ks-pass pass:$password --ks-pass pass:$password $name.apk
                else
                    apksigner.bat sign --v4-signing-enabled false --ks keystore.jks --ks-pass pass:$password --ks-pass pass:$password $name.apk
                fi

                adb install -r $name.apk
                adb shell am start -n $package/.MainActivity

                rm -f classes.txt classes.dex
            fi
            rm -f -r src-compiled sources.txt
        fi
        rm -f -r $name-unaligned.apk src/${package//\./\/}/R.java
    fi
    rm -f -r res-compiled
fi
