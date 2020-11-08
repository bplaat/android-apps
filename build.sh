# keytool -genkey -validity 10000 -keystore key.keystore -keyalg RSA -keysize 2048 -storepass bassietest -keypass bassietest
PATH=$PATH:~/android-sdk/build-tools/30.0.2:~/android-sdk/platform-tools
PLATFORM=~/android-sdk/platforms/android-30/android.jar
if [ "$1" == "log" ]; then
    adb logcat -c
    adb logcat *:E
else
    if aapt package -m -J src -M AndroidManifest.xml -S res -I $PLATFORM; then
        mkdir classes
        if javac -Xlint -cp $PLATFORM -d classes $(find src -name *.java); then
            dx.bat --dex --output=classes.dex classes
            aapt package -F bassietest-unaligned.apk -M AndroidManifest.xml -S res -I $PLATFORM
            aapt add bassietest-unaligned.apk classes.dex
            zipalign -f -p 4 bassietest-unaligned.apk bassietest.apk
            rm -r classes src/nl/plaatsoft/bassietest/R.java classes.dex bassietest-unaligned.apk
            apksigner.bat sign --ks key.keystore --ks-pass pass:bassietest --ks-pass pass:bassietest bassietest.apk
            adb install -r bassietest.apk
            adb shell am start -n nl.plaatsoft.bassietest/.MainActivity
        else
            rm -r classes src/nl/plaatsoft/bassietest/R.java
        fi
    fi
fi
