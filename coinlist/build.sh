#!/bin/bash
export name="coinlist"
export package="ml.coinlist.android"
export version="0.1.0"
export password="android"
export main_activity=".activities.MainActivity"
../android-build.sh $@
