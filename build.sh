#!/bin/bash
mvn package spring-boot:repackage
cp target/*.jar release
chmod +x release/*.jar