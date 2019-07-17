# keytool -genkey -validity 10000 -keystore key.keystore -keyalg RSA -keysize 2048 -storepass nos-android -keypass nos-android
PATH=$PATH:../android-sdk/build-tools/28.0.3:../android-sdk/platform-tools
PLATFORM=../android-sdk/platforms/android-28/android.jar
if aapt package -m -J src -M AndroidManifest.xml -S res -I $PLATFORM; then
    mkdir classes
    if javac -cp $PLATFORM -d classes $(find -name *.java); then
        dx.bat --dex --output=classes.dex classes
        aapt package -F nos-unaligned.apk -M AndroidManifest.xml -S res -I $PLATFORM
        aapt add nos-unaligned.apk classes.dex
        zipalign -f -p 4 nos-unaligned.apk nos.apk
        rm -r classes src/nl/nos/android/R.java classes.dex nos-unaligned.apk
        apksigner.bat sign --ks key.keystore --ks-pass pass:nos-android --ks-pass pass:nos-android nos.apk
        adb install -r nos.apk
        adb shell am start -n nl.nos.android/.MainActivity
    else
        rm -r classes src/nl/nos/android/R.java
    fi
fi
