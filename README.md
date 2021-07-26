<h1 style="text-align: center">浙江大学自动健康打卡AutoCard</h1>
<div style="text-align: center">

![AUR](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)
![star](https://gitee.com/GCSZHN/AutoCard/badge/star.svg?theme=white)
![GitHub stars](https://img.shields.io/github/stars/GCS-ZHN/AutoCard.svg?style=social&label=Stars)
![GitHub forks](https://img.shields.io/github/forks/GCS-ZHN/AutoCard.svg?style=social&label=Fork)

</div>

## 项目概述
本项目为解决浙江大学每日重复的健康打卡而开发，在完成首次手动打卡后，可以自动进行定时打卡。并通过邮件形式提醒通知打卡结果。本项目使用spring-boot、quartz和httpclient开发，使用maven进行项目管理，编译版本为jdk-14.0.2。

## 基本使用步骤
1. **STEP 1 用来跑程序的设备**

定时打卡任务意味着程序需要一直保持运行，因此个人建议将项目运行在一台服务器上。阿里云、华为云、腾讯云等都提供许多服务器租赁。当然，你要是保持个人电脑一直不关，那么用个人电脑也OK。

2. **STEP 2 安装java语言**

正如所说，本项目是一个java项目（src文件夹源码文件是*.java），因此需要用户事先安装java语言。作者的发行版用的是jdk-14.0.2，即java SE 14.0.2，[官方下载地址](https://www.oracle.com/java/technologies/javase/jdk14-archive-downloads.html)。根据自己设备的操作系统选择对应的安装包即可。

|安装包扩展名 |对应系统                  |
|:----------|:------------------------|
|deb        |Linux发行版的Debian/Ubuntu|
|rpm        |Linux发行版的CentOS       |
|dmg        |MacOS                    |
|exe        |Win                      |

3. **STEP 3 下载作者提供的发行版**

在[gitee](https://gitee.com/GCSZHN/AutoCard/releases/)或[github](https://github.com/GCS-ZHN/AutoCard/releases)的项目发行版页面，下载最新的发行版（autocard-XXX.zip，XXX为版本号）。并解压。可以看到解压后目录结构如下

        --autocard-XXX/
        ----autocard-XXX.jar                    ## 核心java程序，是编译后打包的jar包
        ----startup.sh                          ## 在linux下，用于启动java程序的shell脚本
        ----shutdown.sh                         ## 在linux下，用于关闭java程序的shell脚本
        ----config/
        ------application.properties            ## 用户配置，如账号密码等
        ------log4j2.xml                        ## 日志配置，不用修改

4. **STEP 4 修改application.properties**

用任意文本编辑器打开config目录下的application.properties，配置下列信息（即把文件里面的*号内容替换，#号开头的行是注释，可以直接删除）。

        zjupassport.username=学工号
        zjupassport.password=密码
        mail.auth.username=邮箱
        mail.auth.password=邮箱密码

邮箱用于打卡的通知，默认使用浙大邮箱，否则需要`mail.smtp.host`和`mail.smtp.port`参数配置为指定第三方邮箱如QQ邮箱的配置。若不配置邮箱信息，将不会邮件提醒。

5. **STEP 5 运行程序**

需要通过命令行来运行程序，在Windows下，常见的命令行是cmd和powershell，打开方式“WIN + R”，输入"cmd"或"powershell"，确定即可。linux服务器打开即是shell命令行页面（To小白：如何连接Linux服务器请自行百度一下，拥有服务器用户名、密码、IP、端口，通过ssh客户端访问）。

        java -jar autocard-XXX.jar   # 方式一，在auotcard-XXX的解压目录下，直接通过java命令运行
        ....                         # 然后你会看到日志输出到屏幕，此方法仅适合不关闭命令行页面，在自己电脑跑


        bash startup.sh              # 方式二，运行上面说的shell脚本启动，但仅限于linux服务器。可以关闭服务器连接
        ....                         # 会弹出nohup的信息，直接enter下去就好

对于方式一，关闭命令行页面即为关闭程序。方式二请通过`bash shutdown.sh`关闭程序。还是推荐服务器上，用方式二运行。

此外，可以通过命令行参数直接指定用户配置，该方法会覆盖配置文件中的相同配置。例如，参数名同配置文件中参数名（但需要“--”开头）。不建议长期使用，可以用来临时测试。

        java -jar autocard-XXX.jar --zjupassport.name=XXXXX --zjupassport.password=XXXXX

通过方式一，运行正常可以看到下列日志输出屏幕。不论哪种方式，相同的程序日志会在`app.log`文件中看到。最后日志显示JVM running。（等到了打卡时间，日志会继续输出）

![方式一截图](templete/fig1.png)

## 额外参数
- app.autoCard.cronExpresssion

cron表达式是用于定时任务的经典表达式，该参数允许用户自定义打卡定时方式。网上有很多现成的表达式模板以及表达式在线生成工具。**默认定时设定是每天早上9点自动打卡**。网上有很多介绍或[在线生成器](https://cron.qqe2.com/)。cron表达式从左到右（空格分开）指的是“秒 分 时 每月第几天 月份 每周第几天 年份”，特殊符号表示通配。

        0 0 0 * * ? *      ## 每天00:00:00打卡
        0 30 6 * * ? *     ## 每天06:30:00打卡
        0 0 9 * * ? *      ## 每天09:00:00打卡

- app.zjuClient.cookieCached

该参数默认为false，设置为true则会启动cookie缓存。

- mail.smtp.host

该参数修改了SMTP服务器主机域名，默认采用浙大服务器smtp.zju.edu.cn。在需要使用QQ邮箱等其他邮箱是需要配套修改设定，例如

        mail.smtp.host=smtp.qq.com

- mail.smtp.port
  
该参数为SMTP服务端口，默认为994，具体看SMTP邮件服务提供商。

## 自己打包
若用户需要使用低版本如jdk 1.8，需要在对应版本（安装对应版本jdk并修改pom.xml中版本信息）下重新编译打包maven项目（要求用户得安装了[maven](https://maven.apache.org/download.cgi)），建议配置maven工具的镜像为阿里云（这样首次打包时下载依赖库会快一点，[阿里云程](https://maven.aliyun.com/mvn/guide)）。

1. 克隆或下载本项目

在github或gitee中，都提供了项目的clone、fork或者直接下载zip，下载完成后进入目录。

利用git工具，命令行下克隆项目

        git clone https://gitee.com/GCSZHN/AutoCard.git
        git clone https://github.com/GCS-ZHN/AutoCard.git

2. 修改pom.xml
   
        <maven.compiler.source>你的java版本</maven.compiler.source>
        <maven.compiler.target>你的java版本</maven.compiler.target>

用任意文本编辑器编辑pom.xml，修改上述配置。

3. 运行打包脚本

        bash build.sh         ## linux
        powershell build.ps1  ## windows

根据平台，运行打包脚本。会产生一个release子文件夹。不过个人没有macOS，故没有编写macOS打包脚本，用户可以直接执行`mvn package spring-boot:repackage`打包生成jar文件，然后按照前面的目录结构放置。

## 注意
若打卡题目被更新，请先手动打卡一次。本项目仅供学习参考。使用时请确保信息的正确性。滥用造成的后果请自行承担。

## 反馈
任何使用问题，欢迎通过邮箱**zhang.h.n@foxmail.com**交流。
