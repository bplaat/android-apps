#!/bin/bash
set -e
if [ "$1" = "tools" ]; then
    cd tools; cargo build; cd ..
    for translation in nbv21 bgt nbv hsv niv kjv lu17 eue; do
        ./tools/target/debug/bible-dl $translation -o assets/bibles/$translation.bible
    done
    ./tools/target/debug/convert-ops --name Hemelhoog --abbreviation hh \
        --language nl --copyright "Copyright © 2015 Uitgeverij Boekencentrum" \
        --songs tools/tmp/hh_songs.json --lyrics tools/tmp/hh_lyrics.json \
        -o assets/songbundles/hh.songbundle
    ./tools/target/debug/scrape-opwekking -o assets/songbundles/opw.songbundle
    exit
fi

export name="bible"
export package="nl.plaatsoft.bible"
export version="1.3.0"
export password="android"
export main_activity=".activities.MainActivity"
../android-build.sh $@
