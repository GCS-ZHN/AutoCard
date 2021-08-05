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
        LogUtils.printMessage("Automatic clock-in job starts...");
        // 三次打卡尝试，失败后发送邮件提示。
        int change = 3;
        try (ClockinService cardService = SpringUtils.getBean(ClockinService.class)) {
            MailService mailService = SpringUtils.getBean(MailService.class);
            String username = context.getMergedJobDataMap().getString("username");
            String password = context.getMergedJobDataMap().getString("password");
            String mail = context.getMergedJobDataMap().getString("mail");
            if (username==null||password==null) 
                throw new NullPointerException("Empty username or password of zjupassport");

            //打卡
            StatusCode statusCode = new StatusCode();
            while (change>0 && (statusCode = cardService.submit(username, password)).getStatus()==-1) {
                int delay = (4-change) * 10;
                LogUtils.printMessage("Try to submit again after sleeping "+delay+"s ...", 
                    LogUtils.Level.ERROR);
                Thread.sleep(delay * 1000);
                change--;
            }
            if (change==0) {
                LogUtils.printMessage("Submit failed 3 times for " + username, 
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
