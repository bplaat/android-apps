#!/bin/bash

set -e

platform=$ANDROID_HOME/platforms/android-36/android.jar

clean() {
    find . -type d -name "target" -exec rm -rf {} +
}

vscode() {
    mkdir -p .vscode
    echo "{\"[java]\": {\"editor.defaultFormatter\": \"xaver.clang-format\", \"editor.formatOnSave\": true}," > .vscode/settings.json
    echo "\"java.saveActions.organizeImports\":true," >> .vscode/settings.json
    echo "\"java.completion.importOrder\":[\"java\",\"android\",\"com\",\"nl\",\"org\",\"\"]," >> .vscode/settings.json
    echo "\"java.project.sourcePaths\":[" >> .vscode/settings.json
    for dir in $(find . -name "src" ! -path "*/target/*") $(find . -name "src-gen"); do
        echo "\"$dir\"," >> .vscode/settings.json
    done
    echo "],\"java.project.referencedLibraries\":[\"$platform\",\"./bin/bassiemusic/target/jar-cache/*.jar\"]," >> .vscode/settings.json
    echo "\"java.compile.nullAnalysis.mode\":\"automatic\"," >> .vscode/settings.json
    echo "\"java.compile.nullAnalysis.nullable\":[\"org.jspecify.annotations.Nullable\"]," >> .vscode/settings.json
    echo "\"java.compile.nullAnalysis.nonnull\":[\"org.jspecify.annotations.NonNull\"]," >> .vscode/settings.json
    echo "\"java.compile.nullAnalysis.nonnullbydefault\":[\"org.jspecify.annotations.NullMarked\"]," >> .vscode/settings.json
    echo "\"rust-analyzer.linkedProjects\":[" >> .vscode/settings.json
    for file in $(find . -name "Cargo.toml"); do
        echo "\"$file\"," >> .vscode/settings.json
    done
    echo "]}" >> .vscode/settings.json
}

check_copyright() {
    exit=0
    for file in $(find . -type f \( -name "*.java" -o -name "*.rs" \) ! -path "*/target/*"); do
        if ! grep -E -q "Copyright \(c\) 20[0-9]{2}(-20[0-9]{2})? Bastiaan van der Plaat" "$file"; then
            echo "Bad copyright header in: $file"
            exit=1
        fi
    done
    if [ "$exit" -ne 0 ]; then
        exit 1
    fi
}

check_format() {
    # Format
    echo "Checking Java formatting..."
    clang-format --dry-run --Werror $(find . -type f -name "*.java" ! -path "*/target/*")
}

build() {
    echo "Building all projects..."
    # FIXME: Do null analysis
    for dir in $(ls bin); do
        bob build --time -C "bin/$dir"
    done
}

check() {
    check_copyright
    check_format
    build
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
