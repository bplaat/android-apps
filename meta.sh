#!/bin/bash

set -e

platform=$ANDROID_HOME/platforms/android-36/android.jar

function clean() {
    find . -type d -name "target" -exec rm -rf {} +
}

function vscode() {
    mkdir -p .vscode
    echo "{\"editor.formatOnSave\":true,\"java.project.sourcePaths\":[" > .vscode/settings.json
    for dir in $(find . -name "src" ! -path "*/target/*") $(find . -name "src-gen"); do
        echo "\"$dir\"," >> .vscode/settings.json
    done
    echo "],\"java.project.referencedLibraries\":[\"$platform\"]," >> .vscode/settings.json
    echo "\"java.compile.nullAnalysis.mode\":\"automatic\"," >> .vscode/settings.json
    echo "\"java.compile.nullAnalysis.nullable\":[\"javax.annotation.Nullable\"]," >> .vscode/settings.json
    echo "\"java.compile.nullAnalysis.nonnull\":[\"javax.annotation.Nonnull\"]," >> .vscode/settings.json
    echo "\"java.compile.nullAnalysis.nonnullbydefault\":[\"javax.annotation.ParametersAreNonnullByDefault\"]," >> .vscode/settings.json
    echo "\"rust-analyzer.linkedProjects\":[" >> .vscode/settings.json
    for file in $(find . -name "Cargo.toml"); do
        echo "\"$file\"," >> .vscode/settings.json
    done
    echo "]}" >> .vscode/settings.json
}

function check_copyright() {
    exit=0
    for file in $(find . \( -name "*.java" -o -name "*.rs" \) ! -path "*/target/*"); do
        if ! grep -E -q "Copyright \(c\) 20[0-9]{2}(-20[0-9]{2})? Bastiaan van der Plaat" "$file"; then
            echo "Bad copyright header in: $file"
            exit=1
        fi
    done
    if [ "$exit" -ne 0 ]; then
        exit 1
    fi
}

function check() {
    check_copyright
    # FIXME: Check format and lint Java code
    bob build -C bin/bassiemusic
    bob build -C bin/bassietest
    bob build -C bin/bible
    bob build -C bin/coinlist
    bob build -C bin/hackernews
    bob build -C bin/redsquare
    bob build -C bin/rfidviewer
    bob build -C bin/tweakers
}

case "${1:-check}" in
    clean)
        clean
        ;;
    vscode)
        vscode
        ;;
    check)
        check
        ;;
    *)
        echo "Usage: $0 {clean|vscode|check}"
        exit 1
        ;;
esac
