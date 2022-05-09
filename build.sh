#!/bin/bash
appName="autocard"
mvn clean package spring-boot:repackage
if [ ! -d "release" ];then
    mkdir release
fi
version=`ls target/autocard-*.jar|sed -r "s/target\/autocard-(.+?).jar/\1/g"`
targetDir="release/$appName-$version"
if [ -d $targetDir ];then
    rm -rf $targetDir
fi
if [ -f "$targetDir.zip" ];then
    rm -f "$targetDir.zip"
fi
mkdir $targetDir
cp target/autocard-$version.jar $targetDir
cp templete/shutdown.sh.temp $targetDir/shutdown.sh
cp templete/startup.sh.temp $targetDir/startup.sh
cp -R config $targetDir/config
cat templete/startup.sh.temp|sed -r "s/VERSION/$version/g" > $targetDir/startup.sh
zip -r $targetDir.zip $targetDir