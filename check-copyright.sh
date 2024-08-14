#!/bin/bash
exit_code=0
for file in $(find . -name "*.java" ! -path "*/target/*"); do
    if ! grep -E -q "Copyright \(c\) 20[0-9][0-9](-20[0-9][0-9])? Bastiaan van der Plaat" "$file"; then
        echo "No valid copyright notice in $file"
        exit_code=1
    fi
done
exit $exit_code
