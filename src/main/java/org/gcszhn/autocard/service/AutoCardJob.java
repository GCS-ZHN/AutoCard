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
public class AutoCardJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try (AutoCardService cardService = SpringUtils.getBean(AutoCardService.class)) {
            MailService mailService = SpringUtils.getBean(MailService.class);
            DingTalkHookService dingTalkHookService = SpringUtils.getBean(DingTalkHookService.class);
            execute(context.getMergedJobDataMap(), mailService, cardService, dingTalkHookService);
        }
    }
    public static void execute(
        JobDataMap dataMap, 
        MailService mailService, 
        AutoCardService cardService,
        DingTalkHookService dingTalkHookService) throws JobExecutionException {
        boolean isDelay = dataMap.getBooleanValue("delay");
        String username = dataMap.getString("username");
        String password = dataMap.getString("password");
        String mail = dataMap.getString("mail");
        String dingtalkURL = dataMap.getString("dingtalkurl");
        String dingtalkSecret = dataMap.getString("dingtalksecret");
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
        try {
            if (username==null||password==null||username.isEmpty()||password.isEmpty()) 
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
            if (mailService.isServiceAvailable() && mail != null && !mail.isEmpty()) {
                mailService.sendMail(
                    mail,
                    "健康打卡通知", 
                    statusCode.getMessage(),
                    "text/html;charset=utf-8");
            }

            if (dingtalkURL!=null && !dingtalkURL.isEmpty()) {
                if (dingtalkSecret!=null && !dingtalkSecret.isEmpty()) {
                    dingtalkURL = dingTalkHookService.getSignature(dingtalkSecret, dingtalkURL);
                }
                StatusCode status;
                if (statusCode.getJsonMessage() != null) {
                    String message = String.format("### 【健康打卡通知】\n%s\n![photo](%s)",
                        statusCode.getJsonMessage().getString("message"),
                        statusCode.getJsonMessage().getString("photo"));

                    status = dingTalkHookService.sendMarkdown(dingtalkURL, "【健康打卡通知】", message);
                         
                } else {
                    status = dingTalkHookService.sendMarkdown(
                        dingtalkURL, 
                        "【健康打卡通知】", 
                        String.format("### 【健康打卡通知】\n%s",
                            statusCode.getMessage()));
                }
                
                if (status.getStatus() == 0) {
                    LogUtils.printMessage("钉钉推送成功");
                } else {
                    LogUtils.printMessage("钉钉推送失败：" + status.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.printMessage(e.getMessage(), LogUtils.Level.ERROR);
        }
    }
}
