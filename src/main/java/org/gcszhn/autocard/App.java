/*
 * Copyright © 2021 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 *
 * Licensed under the Apache License, Version 2.0 (thie "License");
 * You may not use this file except in compliance with the license.
 * You may obtain a copy of the License at
 *
 *       http://wwww.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language govering permissions and
 * limitations under the License.
 */
package org.gcszhn.autocard;


import java.io.File;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.gcszhn.autocard.service.AutoClockinJob;
import org.gcszhn.autocard.service.ClockinService;
import org.gcszhn.autocard.service.JobService;
import org.gcszhn.autocard.service.MailService;
import org.gcszhn.autocard.utils.LogUtils;
import org.quartz.JobDataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.context.ConfigurableApplicationContext;


/**
 * App入口
 */
@SpringBootApplication
public class App {
    /**配置的默认任务定时cron表达式 */
    @Value("${app.autoCard.cronExpression}")
    private String defaultCronExpression;
    @Value("${app.autoCard.immediate}")
    private boolean immediate;
    /**APP配置 */
    @Autowired
    AppConfig appConfig;
    /**spring内容 */
    private static ConfigurableApplicationContext applicationContext;
    /**工作目录 */
    public static String workDir;
    /**
     * 启动定时服务
     * @param jobService 定时服务注入
     * @param mailService
     * @param cardService
     */
    @Autowired
    public void start(JobService jobService, MailService mailService, ClockinService cardService) {
        try {
            JSONArray jsonArray =  appConfig.getUserJobs();
            jsonArray.forEach((Object obj)->{
                if (obj instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) obj;
                    JobDataMap jobDataMap = new JobDataMap(jsonObject);
                    if (immediate) {
                        try {
                            AutoClockinJob.execute(jobDataMap, mailService, cardService);
                        } catch (Exception e) {
                            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
                        }
                    } else {
                        String cron = jsonObject.getString("cron");
                        jobService.addJob(AutoClockinJob.class, cron==null?defaultCronExpression:cron, jobDataMap); 
                    }
                }
            });
            if (immediate) {
                exit(0);
            }
            LogUtils.printMessage(jsonArray.size()+" user job added");
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