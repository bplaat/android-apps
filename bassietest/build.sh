#!/bin/bash
export name="bassietest"
export package="nl.plaatsoft.bassietest"
export version="0.1.0"
export password="android"
export main_activity=".activities.MainActivity"
../android-build.sh $@
