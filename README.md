# 使用方法
## 依赖
本项目使用spring-boot、quartz和httpclient开发，编译版本为jdk-14.0.2。
## 基本运行
- 执行`java -jar autocard-1.0.jar`。此时需要在工作目录下有`config/application.properties`。注意工作目录是输入命令时的shell终端目录。

    username=学工号
    password=密码
    mail.auth.username=浙大邮箱
    mail.auth.password=邮箱密码

邮箱用于打卡的通知，默认需要浙大邮箱，否则需要`mail.smtp.host`和`mail.smtp.port`参数配置为指定第三方邮箱如QQ邮箱的配置。

- 此外，可以在上述命令中使用`--name=学工号`与`--password=密码`启动。该参数会覆盖配置文件。
## 额外参数
- --app.autoCard.cornExpresssion

corn表达式是用于定时任务的经典表达式，该参数允许用户自定义打卡定时方式。网上有很多现成的表达式模板以及表达式在线生成工具。

- --app.zjuClient.cookieCached

该参数默认为false，设置为true则会启动cookie缓存。