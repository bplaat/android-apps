#!/bin/bash
set -e

for translation in nbv21 bgt nbv hsv niv kjv lu17 eue; do
    cargo run --bin bible-dl -- $translation -o ../assets/bibles/$translation.bible
done

cargo run --bin convert-ops -- --name Hemelhoog --abbreviation hh \
    --language nl --copyright "Copyright © 2015 Uitgeverij Boekencentrum" \
    --songs tmp/hh_songs.json --lyrics tmp/hh_lyrics.json \
    -o ../assets/songbundles/hh.songbundle

cargo run --bin scrape-opwekking -- -o ../assets/songbundles/opw.songbundle
cargo run --bin scrape-lvdk -- -o ../assets/songbundles/lvdk.songbundle
