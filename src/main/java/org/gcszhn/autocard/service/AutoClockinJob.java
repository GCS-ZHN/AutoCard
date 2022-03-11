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
package org.gcszhn.autocard.service;

import org.gcszhn.autocard.utils.LogUtils;
import org.gcszhn.autocard.utils.SpringUtils;
import org.gcszhn.autocard.utils.StatusCode;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 自动打卡的定时任务
 * @author Zhang.H.N
 * @version 1.1
 */
public class AutoClockinJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        execute(context.getMergedJobDataMap());
    }
    public static void execute(JobDataMap dataMap) throws JobExecutionException {
        boolean isDelay = dataMap.getBooleanValue("delay");
        String username = dataMap.getString("username");
        String password = dataMap.getString("password");
        String mail = dataMap.getString("mail");
        //开启随机延迟，这样可以避免每次打卡时间过于固定
        try {
            if (isDelay) {
                long delaySec = (long)(Math.random()*1800);
                LogUtils.printMessage("任务随机延时" + delaySec+"秒");
                Thread.sleep(delaySec * 1000); 
            }
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
        LogUtils.printMessage("自动打卡开始");
        // 三次打卡尝试，失败后发送邮件提示。
        int change = 3;
        try (ClockinService cardService = SpringUtils.getBean(ClockinService.class)) {
            MailService mailService = SpringUtils.getBean(MailService.class);

            if (username==null||password==null) 
                throw new NullPointerException("Empty username or password of zjupassport");

            //打卡
            StatusCode statusCode = new StatusCode();
            while (change>0 && (statusCode = cardService.submit(username, password)).getStatus()==-1) {
                int delay = (4-change) * 10;
                LogUtils.printMessage(delay+"秒后再次尝试", 
                    LogUtils.Level.ERROR);
                Thread.sleep(delay * 1000);
                change--;
            }
            if (change==0) {
                LogUtils.printMessage("打卡尝试失败3次 " + username, 
                    LogUtils.Level.ERROR);
            }

            //邮件通知
            if (mailService.isServiceAvailable() && mail != null) {
                mailService.sendMail(
                    mail,
                    "健康打卡通知", 
                    statusCode.getMessage(),
                    "text/html;charset=utf-8");
            }
        } catch (Exception e) {
            LogUtils.printMessage(e.getMessage(), LogUtils.Level.ERROR);
        }
    }
}
