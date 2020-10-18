# keytool -genkey -validity 10000 -keystore key.keystore -keyalg RSA -keysize 2048 -storepass nos-android -keypass nos-android
PATH=$PATH:~/android-sdk/build-tools/30.0.2:~/android-sdk/platform-tools
PLATFORM=C:\\Users\\bplaat\\android-sdk\\platforms\\android-30\\android.jar
if aapt package -m -J src -M AndroidManifest.xml -S res -I $PLATFORM; then
    if javac -Xlint -cp "$PLATFORM;classes" -d classes $(find -name *.java); then
        dx.bat --dex --output=classes.dex classes
        aapt package -F nos-unaligned.apk -M AndroidManifest.xml -S res -I $PLATFORM
        aapt add nos-unaligned.apk classes.dex
        zipalign -f -p 4 nos-unaligned.apk nos.apk
        rm -r classes/nl src/nl/plaatsoft/nos/android/R.java classes.dex nos-unaligned.apk
        apksigner.bat sign --ks key.keystore --ks-pass pass:nos-android --ks-pass pass:nos-android nos.apk
        adb install -r nos.apk
        adb shell am start -n nl.plaatsoft.nos.android/.MainActivity
    else
        rm -r classes/nl src/nl/plaatsoft/nos/android/R.java
    fi
fi
