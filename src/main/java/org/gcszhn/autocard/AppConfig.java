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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.gcszhn.autocard.service.MailService;
import org.gcszhn.autocard.utils.LogUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;


/**
 * App通用配置和组件注册
 * @author Zhang.H.N
 * @version 1.1
 */
@Configuration
public class AppConfig {
    /**默认字符集 */
    public static final Charset APP_CHARSET = StandardCharsets.UTF_8;
    /**JSON配置文件 */
    private JSONObject jsonConfig;
    public AppConfig() {
        loadJSONConfig();
    }
    /**
     * 初始化json配置
     */
    public void loadJSONConfig() {
        try(FileInputStream fis = new FileInputStream("config/application.json")) {
            jsonConfig = JSONObject.parseObject(new String(fis.readAllBytes(), APP_CHARSET));
            LogUtils.printMessage("User config loaded");
         } catch (IOException e) {
             LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
         }
    }
    /**
     * 注册邮件服务
     * @param env spring环境
     * @return 邮件服务实例
     */
    @Bean
    public MailService registerMailService(ConfigurableEnvironment env) {
        JSONObject mailConfig = jsonConfig.getJSONObject("mail");
        MailService mailService = new MailService();
        if (mailConfig != null){
            String nickname = mailConfig.getString("nickname");
            Object port = mailConfig.get("port");

            mailService.setNickname(nickname==null?"AutoCard":nickname);
            mailService.setUsername(mailConfig.getString("username"));
            mailService.setPassword(mailConfig.getString("password"));
            mailService.setSmtpHost(mailConfig.getString("smtp"));
            if (port instanceof String||port instanceof Integer) {
                mailService.setSmtpPort(String.valueOf(port));
            }
        }
        mailService.setEnvironment(env);
        return mailService;
    }
    /**
     * 返回用户任务
     * @return 用户任务
     */
    public JSONArray getUserJobs() {
        JSONArray jsonArray = jsonConfig.getJSONArray("jobs");
        return jsonArray==null?new JSONArray():jsonArray;
    }
}