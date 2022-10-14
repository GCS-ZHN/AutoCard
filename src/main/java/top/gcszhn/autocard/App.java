/*
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
 */
package top.gcszhn.autocard;

import java.io.File;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.quartz.JobDataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.ConfigurableApplicationContext;

import top.gcszhn.autocard.service.AutoCardJob;
import top.gcszhn.autocard.service.AutoCardService;
import top.gcszhn.autocard.service.DingTalkHookService;
import top.gcszhn.autocard.service.JobService;
import top.gcszhn.autocard.service.MailService;
import top.gcszhn.autocard.utils.LogUtils;

/**
 * App入口
 */
@SpringBootApplication
public class App {
    /**配置的默认任务定时cron表达式 */
    @Value("${app.autoCard.cronExpression}")
    private String defaultCronExpression;
    /**配置是否立即运行 */
    @Value("${app.autoCard.immediate}")
    private boolean immediate;
    /**spring内容 */
    private static ConfigurableApplicationContext applicationContext;
    /**工作目录 */
    public static String workDir;
    /**
     * 启动定时服务
     * @param jobService 定时服务注入
     * @param mailService 邮件服务注入
     * @param cardService 打卡服务注入
     * @param dingTalkHookService 钉钉通知服务注入
     * @param appConfig 应用依赖配置注入
     */
    @Autowired
    public void start(
        JobService jobService, 
        MailService mailService, 
        AutoCardService cardService, 
        DingTalkHookService dingTalkHookService, 
        AppConfig appConfig) {
        try {
            JSONArray jsonArray =  appConfig.getUserJobs();
            LogUtils.printMessage(jsonArray.size()+" user job added");
            jsonArray.forEach((Object obj)->{
                if (obj instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) obj;
                    JobDataMap jobDataMap = new JobDataMap(jsonObject);
                    if (immediate) {
                        try {
                            AutoCardJob.execute(jobDataMap, mailService, cardService, dingTalkHookService);
                            cardService.logout();
                        } catch (Exception e) {
                            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
                        } finally {
                            cardService.close();
                        }
                    } else {
                        String cron = jsonObject.getString("cron");
                        jobService.addJob(AutoCardJob.class, cron==null?defaultCronExpression:cron, jobDataMap); 
                    }
                }
            });
            if (immediate) {
                exit(0);
            }
            if (!jsonArray.isEmpty()) jobService.start();
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
    }
    /**
     * 配置项目目录，实际上Spring自身日志有该信息，但无法获取
     */
    public static void setWorkDir() {
        workDir = App.class.getProtectionDomain().getCodeSource().getLocation().toString();
        workDir = workDir.replaceFirst("^[^/]*file:", "").replaceFirst("!/BOOT-INF/classes!/", "");
        workDir = new File(workDir).getParent();
    }
    /**
     * 程序入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        setWorkDir();
        SpringApplication application = new SpringApplication(App.class);
        application.addListeners(new ApplicationPidFileWriter("app.pid"));
        applicationContext = application.run(args);
    }
    /**
     * 发生意外时主动退出APP
     * @param code 状态码
     */
    public static void exit(int code) {
        if (applicationContext!=null) {
            System.exit(SpringApplication.exit(applicationContext, ()->code));
        } else {
            System.exit(code);
        }
    }
}