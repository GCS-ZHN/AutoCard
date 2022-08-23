/* 
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
 */
package top.gcszhn.autocard.service;

import java.util.Optional;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.springframework.util.StringUtils;
import top.gcszhn.autocard.utils.LogUtils;
import top.gcszhn.autocard.utils.SpringUtils;
import top.gcszhn.autocard.utils.StatusCode;

/**
 * 自动打卡的定时任务
 * 
 * @author Zhang.H.N
 * @version 1.2
 */
public class AutoCardJob implements Job {
    private static final int DEFAULT_MAX_TRIAL = 3;

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
        // 参数初始化
        boolean isDelay = dataMap.getBooleanValue("delay");
        String username = dataMap.getString("username");
        String password = dataMap.getString("password");
        String nickname = dataMap.getString("nickname");
        String mail = dataMap.getString("mail");
        String dingtalkURL = dataMap.getString("dingtalkurl");
        String dingtalkSecret = dataMap.getString("dingtalksecret");
        int maxTrial = Optional.ofNullable(dataMap.get("maxtrial"))
                .map((Object value) -> {
                    try {
                        if (value instanceof Integer)
                            return (int) value;
                        if (value instanceof String) {
                            if (value.equals(""))
                                return DEFAULT_MAX_TRIAL;
                            return Integer.parseInt((String) value);
                        } else {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException e) {
                        LogUtils.printMessage("无效的整数格式", LogUtils.Level.ERROR);
                        return DEFAULT_MAX_TRIAL;
                    }
                })
                .orElse(DEFAULT_MAX_TRIAL);
        // 开启随机延迟，这样可以避免每次打卡时间过于固定
        try {
            if (isDelay) {
                long delaySec = (long) (Math.random() * 1800);
                LogUtils.printMessage("任务随机延时" + delaySec + "秒");
                Thread.sleep(delaySec * 1000);
            }
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }

        try {
            LogUtils.printMessage("自动打卡开始");
            if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
                throw new NullPointerException("用户名/密码不能为空");

            StatusCode cardStatus = new StatusCode();
            int trial = maxTrial;
            LOOP: while (trial > 0) {
                cardStatus = cardService.submit(username, password, nickname);
                switch (cardStatus.getStatus()) {
                    case 0:
                    case 1: {
                        break LOOP;
                    }
                    default: {
                        int delay = (maxTrial - trial + 1) * 10;
                        LogUtils.printMessage(delay + "秒后再次尝试",
                                LogUtils.Level.ERROR);
                        Thread.sleep(delay * 1000);
                        trial--;
                    }
                }

            }
            if (trial == 0) {
                LogUtils.printMessage(String.format("%s打卡尝试失败%d次", username, maxTrial),
                        LogUtils.Level.ERROR);
            }

            // 邮件通知
            if (mailService.isServiceAvailable() && mail != null && !mail.isEmpty()) {
                mailService.sendMail(
                        mail,
                        "健康打卡通知",
                        cardStatus.getMessage(),
                        "text/html;charset=utf-8");
            }

            if (dingtalkURL != null && !dingtalkURL.isEmpty()) {
                if (dingtalkSecret != null && !dingtalkSecret.isEmpty()) {
                    dingtalkURL = dingTalkHookService.getSignature(dingtalkSecret, dingtalkURL);
                }
                String message;
                String photo = null;
                if (cardStatus.getJsonMessage() != null) {
                    message = cardStatus.getJsonMessage().getString("message");
                    photo = cardStatus.getJsonMessage().getString("photo");
                    if (photo != null) {
                        message += "\n![photo](" + photo + ")";
                    }
                } else {
                    message = cardStatus.getMessage();
                }
                StatusCode pushStatus;
                if (photo != null) {
                    // 在通知栏显示状态
                    String title;
                    switch(cardStatus.getStatus()) {
                        case 0:
                            title = "今日打卡成功";
                            break;
                        case 1:
                            title = "今日重复打卡";
                            break;
                        default:
                            title = "今日打卡异常";
                            break;
                    }
                    message = String.format("### 【%s】\n%s", title, message);
                    pushStatus = dingTalkHookService.sendMarkdown(
                        dingtalkURL, 
                        title, 
                        message);
                } else {
                    pushStatus = dingTalkHookService.sendText(
                        dingtalkURL, 
                        message);
                }
                if (pushStatus.getStatus() == 0) {
                    LogUtils.printMessage("钉钉推送成功");
                } else {
                    LogUtils.printMessage("钉钉推送失败：" + pushStatus.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.printMessage(e.getMessage(), LogUtils.Level.ERROR);
        }
    }
}
