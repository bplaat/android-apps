#!/bin/bash
set -e
if [ "$1" = "tools" ]; then
    cd tools/bible-dl; cargo build --release; cd ../..
    for translation in nbv21 bgt nbv hsv niv kjv; do
        ./tools/bible-dl/target/release/bible-dl $translation -o assets/bibles/$translation.bible
    done
    ./tools/convert_ops.py -n Hemelhoog -a HH -g nl -c "Copyright © 2015 Uitgeverij Boekencentrum" \
        -s tools/tmp/hh_songs.json -l tools/tmp/hh_lyrics.json -o assets/songbundles/hh.songbundle
    ./tools/scrape_opwekking.py -n 870 -o assets/songbundles/opw.songbundle
    exit
fi

export name="bible"
export package="nl.plaatsoft.bible"
export version="1.1.0"
export password="android"
export main_activity=".activities.MainActivity"
../android-build.sh $@
