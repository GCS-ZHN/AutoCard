#!/usr/bin/bash
ROOT=`dirname $0`
VERSION=1.2
cd $ROOT
nohup java -jar autocard-$VERSION.jar $@ 2>&1 1>/dev/null &