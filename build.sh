#!/bin/bash
mvn package spring-boot:repackage
if [ ! -d "release" ];then
    mkdir release
fi
cp target/*.jar release
cp -R config config
cp startup.sh release
cp shutdown.sh release
chmod +x release/*.jar