#!/bin/bash
export name="rfidviewer"
export package="nl.plaatsoft.rfidviewer"
export version="0.1.0"
export password="android"
export main_activity=".activities.MainActivity"
../android-build.sh $@
