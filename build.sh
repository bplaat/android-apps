# keytool -genkey -validity 10000 -keystore key.keystore -keyalg RSA -keysize 2048 -storepass redsquare -keypass redsquare
PATH=$PATH:~/android-sdk/build-tools/29.0.2:~/android-sdk/platform-tools
PLATFORM=~/android-sdk/platforms/android-29/android.jar
if aapt package -m -J src -M AndroidManifest.xml -S res -I $PLATFORM; then
    mkdir classes
    if javac -Xlint -cp $PLATFORM -d classes $(find src -name *.java); then
        dx.bat --dex --output=classes.dex classes
        aapt package -F redsquare-unaligned.apk -M AndroidManifest.xml -S res -I $PLATFORM
        aapt add redsquare-unaligned.apk classes.dex
        zipalign -f -p 4 redsquare-unaligned.apk redsquare.apk
        rm -r classes src/nl/plaatsoft/redsquare/android/R.java classes.dex redsquare-unaligned.apk
        apksigner.bat sign --ks key.keystore --ks-pass pass:redsquare --ks-pass pass:redsquare redsquare.apk
        adb install -r redsquare.apk
        adb shell am start -n nl.plaatsoft.redsquare.android/.MainActivity
    else
        rm -r classes src/nl/plaatsoft/redsquare/android/R.java
    fi
fi
