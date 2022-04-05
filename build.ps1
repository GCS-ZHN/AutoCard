$appName="autocard"
mvn clean package spring-boot:repackage
if (!(Test-Path release)) {
    mkdir release
}
if ((Get-ChildItem target/*.jar) -match "$appName-(.+?).jar") {
    $version = $Matches[1]
    $targetDir = "release/$appName-$version"
    if ((Test-Path $targetDir)) {
        Remove-Item -Recurse -Force $targetDir 
    }
    if ((Test-Path "$targetDir.zip")) {
        Remove-Item -Recurse -Force "$targetDir.zip" 
    }
    mkdir $targetDir
    Copy-Item -Path target/autocard-$version.jar -Destination $targetDir
    Copy-Item -Path target/autocard-$version.jar -Destination action/autocard.jar
    Copy-Item templete/shutdown.sh.temp $targetDir/shutdown.sh
    Copy-Item templete/startup.sh.temp $targetDir/startup.sh
    Copy-Item -Recurse config $targetDir/config
    Compress-Archive -LiteralPath $targetDir -DestinationPath "$targetDir.zip"
}

 