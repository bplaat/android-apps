#!/bin/bash
export name="hackernews"
export package="com.ycombinator.news"
export version="1.0.0"
export password="android"
export main_activity=".MainActivity"
../android-build.sh $@
