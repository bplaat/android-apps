# Bassie's Android Apps

A collection of various Android apps that I created for myself and others

## Android Apps

-   [BassieMusic](bassiemusic/) A simple Android app to listen to local music!
-   [BassieTest](bassietest/) A example test app for the bob build tool and sandbox for some ideas
-   [Bible](bible/) An offline Android Bible app containing multiple bible translations
-   [CoinList](coinlist/) A cryptocurrency information app similar to the [coinlist](https://github.com/bplaat/coinlist) website
-   [HackerNews](hackernews/) A simple [HackerNews](https://news.ycombinator.com/) webview app because installed PWA's suck sadly
-   [Redsquare](redsquare/) A port of the [RedSquare](https://github.com/plaatsoft/redsquare) game to Android
-   [RFID Viewer](rfidviewer/) A Mifare Classic / RFID card viewer / writer app
-   [Tweakers](tweakers/) A simple [Tweakers](https://tweakers.net/) webview app because installed PWA's suck sadly

## Android Libraries

-   [NullSafe](lib/nullsafe/) The unreleased null-safe annotations from `javax.annotation`
-   [RatingAlert](lib/ratingalert/) A simple rating alert dialog

## Getting Started

-   Install Java JDK 21
-   Install Android SDK with `platform-tools`, `build-tools:36` and `platforms:android-36` and set `$ANDROID_HOME` env var
-   Install a [Rust toolchain](https://rustup.rs/)
-   Install [bob](https://github.com/bplaat/crates/tree/master/bin/bob)
    ```sh
    cargo install --git https://github.com/bplaat/crates bob
    ```
-   Run `./meta.sh check`

## License

Copyright © 2018-2025 [Bastiaan van der Plaat](https://github.com/bplaat)

Licensed under the [MIT](LICENSE) license.
