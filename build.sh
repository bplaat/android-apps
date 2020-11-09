# keytool -genkey -validity 10000 -keystore key.keystore -keyalg RSA -keysize 2048 -storepass nos-android -keypass nos-android

PATH=$PATH:~/android-sdk/build-tools/30.0.2:~/android-sdk/platform-tools
PLATFORM=C:\\Users\\bplaat\\android-sdk\\platforms\\android-30\\android.jar

if [ "$1" == "log" ]; then
    adb logcat -c
    adb logcat *:E
else
    mkdir resources
    if
        aapt2 compile --dir res -o resources &&
        aapt2 link $(find resources -name *.flat) --manifest AndroidManifest.xml --java src -I $PLATFORM -o nos-unaligned.apk
    then
        if javac -Xlint -cp "$PLATFORM;classes" -d classes $(find src -name *.java); then

            find classes -name *.class > classes.txt
            d8.bat --release --lib $PLATFORM @classes.txt
            aapt add nos-unaligned.apk classes.dex

            zipalign -f -p 4 nos-unaligned.apk nos.apk

            apksigner.bat sign --ks key.keystore --ks-pass pass:nos-android --ks-pass pass:nos-android nos.apk

            adb install -r nos.apk
            adb shell am start -n nl.plaatsoft.nos.android/.MainActivity

            rm -r resources nos-unaligned.apk src/nl/plaatsoft/nos/android/R.java classes/nl classes.dex classes.txt
        else
            rm -r resources nos-unaligned.apk src/nl/plaatsoft/nos/android/R.java classes/nl
        fi
    else
        rm -r resources nos-unaligned.apk
    fi
fi
