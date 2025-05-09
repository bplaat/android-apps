#!/bin/bash
set -e
cargo build

for translation in nbv21 bgt nbv hsv niv kjv lu17 eue; do
    ./tools/target/debug/bible-dl $translation -o assets/bibles/$translation.bible
done

./tools/target/debug/convert-ops --name Hemelhoog --abbreviation hh \
    --language nl --copyright "Copyright © 2015 Uitgeverij Boekencentrum" \
    --songs tools/tmp/hh_songs.json --lyrics tools/tmp/hh_lyrics.json \
    -o assets/songbundles/hh.songbundle

./tools/target/debug/scrape-opwekking -o assets/songbundles/opw.songbundle
