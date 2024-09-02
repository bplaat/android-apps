#!/bin/bash
if [ "$1" = "downloader" ]; then
    cd downloader; cargo build --release; cd ..
    # Dutch bible translations
    ./downloader/target/release/bible-dl nbv21 -o assets/bibles/nbv21.bible
    ./downloader/target/release/bible-dl bgt -o assets/bibles/bgt.bible
    ./downloader/target/release/bible-dl nbv -o assets/bibles/nbv.bible
    ./downloader/target/release/bible-dl hsv -o assets/bibles/hsv.bible
    # English bible translations
    ./downloader/target/release/bible-dl niv -o assets/bibles/niv.bible
    ./downloader/target/release/bible-dl kjv -o assets/bibles/kjv.bible
    exit
fi

export name="bible"
export package="nl.plaatsoft.bible"
export version="1.1.0-dev"
export password="android"
export main_activity=".activities.MainActivity"
../android-build.sh $@
