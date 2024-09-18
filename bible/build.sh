#!/bin/bash
set -e
if [ "$1" = "tools" ]; then
    cd tools/bible-dl; cargo build --release; cd ../..
    # Dutch bible translations
    ./tools/bible-dl/target/release/bible-dl nbv21 -o assets/bibles/nbv21.bible
    ./tools/bible-dl/target/release/bible-dl bgt -o assets/bibles/bgt.bible
    ./tools/bible-dl/target/release/bible-dl nbv -o assets/bibles/nbv.bible
    ./tools/bible-dl/target/release/bible-dl hsv -o assets/bibles/hsv.bible
    # English bible translations
    ./tools/bible-dl/target/release/bible-dl niv -o assets/bibles/niv.bible
    ./tools/bible-dl/target/release/bible-dl kjv -o assets/bibles/kjv.bible
    ./downloader/target/release/bible-dl niv -o assets/bibles/niv.bible
    ./downloader/target/release/bible-dl kjv -o assets/bibles/kjv.bible
    exit
fi

export name="bible"
export package="nl.plaatsoft.bible"
export version="1.1.0"
export password="android"
export main_activity=".activities.MainActivity"
../android-build.sh $@
