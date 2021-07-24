#!/usr/bin/bash
ROOT=`dirname $0`
if [ -f $ROOT/app.pid ];then
    cat $ROOT/app.pid|xargs kill
else
    echo "No app.pid found" >&2
    exit 1
fi