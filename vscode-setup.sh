#!/bin/bash
set -e
platform=$ANDROID_HOME/platforms/android-$target_sdk_version/android.jar
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
exit
