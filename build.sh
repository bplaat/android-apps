# keytool -genkey -validity 10000 -keystore key.keystore -keyalg RSA -keysize 2048 -storepass redsquare -keypass redsquare

PATH=$PATH:~/android-sdk/build-tools/30.0.2:~/android-sdk/platform-tools
PLATFORM=~/android-sdk/platforms/android-30/android.jar

if [ "$1" == "log" ]; then
    adb logcat -c
    adb logcat *:E
else
    mkdir resources
    if
        aapt2 compile --dir res -o resources &&
        aapt2 link $(find resources -name *.flat) --manifest AndroidManifest.xml --java src -I $PLATFORM -o redsquare-unaligned.apk
    then
        mkdir classes
        if javac -Xlint -cp $PLATFORM -d classes $(find src -name *.java); then

            d8.bat --release --lib $PLATFORM $(find classes -name *.class)
            aapt add redsquare-unaligned.apk classes.dex

            zipalign -f -p 4 redsquare-unaligned.apk redsquare.apk

            apksigner.bat sign --ks key.keystore --ks-pass pass:redsquare --ks-pass pass:redsquare redsquare.apk

            adb install -r redsquare.apk
            adb shell am start -n nl.plaatsoft.redsquare.android/.MainActivity

            rm -r resources redsquare-unaligned.apk src/nl/plaatsoft/redsquare/android/R.java classes classes.dex
        else
            rm -r resources redsquare-unaligned.apk src/nl/plaatsoft/redsquare/android/R.java classes
        fi
    else
        rm -r resources redsquare-unaligned.apk
    fi
fi
