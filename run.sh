#!/bin/bash
appName="autocard"
mvn clean package spring-boot:repackage
if [ ! -d "release" ];then
    mkdir release
fi
version=`ls target/autocard-*.jar|sed -r "s/target\/autocard-(.+?).jar/\1/g"`
targetDir="actions/$appName-$version"
if [ -d $targetDir ];then
    rm -rf $targetDir
fi
mkdir $targetDir
cp target/autocard-$version.jar $targetDir
cp -R config $targetDir/config