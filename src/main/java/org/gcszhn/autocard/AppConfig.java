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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.gcszhn.autocard.service.MailService;
import org.gcszhn.autocard.utils.LogUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import lombok.Getter;


/**
 * App通用配置和组件注册
 * @author Zhang.H.N
 * @version 1.1
 */
@Configuration
public class AppConfig implements EnvironmentAware {
    /**默认字符集 */
    public static final Charset APP_CHARSET = StandardCharsets.UTF_8;
    /**APP缓存文件 */
    private static final String APP_CACHE = "autocard_cache.json";
    /**应用缓存 */
    private  JSONObject appCache;
    /**JSON配置文件 */
    private JSONObject appConfig;
    /**是否为测试模式 */
    private @Getter boolean testMode = false;
    public AppConfig() {
        LogUtils.printMessage("Test mode is " + testMode, LogUtils.Level.DEBUG);
        try (FileInputStream fis = new FileInputStream(APP_CACHE)) {
            appCache = JSON.parseObject(new String(fis.readAllBytes(), APP_CHARSET));
        } catch (Exception e) {
            appCache = new JSONObject();
        }
    }
    /**
     * SpringBoot 2.x无法在Configuration中使用@Value，因此需要获取springboot环境
     */
    @Override
    public void setEnvironment(Environment env) {
        loadJSONConfig(env.getProperty("app.autoCard.config"));
        testMode = appConfig.getBooleanValue("testmode");

        // 通过系统环境变量添加单个打卡用户
        
        String username = System.getenv("AUTOCARD_USER");
        String password = System.getenv("AUTOCARD_PWD");
        if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
            JSONObject global_user = new JSONObject();
            global_user.put("username", username);
            global_user.put("password", password);
            global_user.put("mail", System.getenv("AUTOCARD_MAIL"));
            global_user.put("cron", System.getenv("AUTOCARD_CRON"));
            global_user.put("dingtalkurl", System.getenv("AUTOCARD_DINGTALK_URL"));
            global_user.put("dingtalksecret",  System.getenv("AUTOCARD_DINGTALK_SECRET"));
            global_user.put("delay", System.getenv("AUTOCARD_DELAY") != null);
            appConfig.getJSONArray("jobs").add(global_user);
        }

    }
    /**
     * 初始化json配置
     */
    public void loadJSONConfig(String configSource) {
        String jsonString = null;
        try {
            if (configSource.startsWith("file://")) {
                try(FileInputStream fis = new FileInputStream(configSource.substring(7))) {
                    jsonString = new String(fis.readAllBytes(), APP_CHARSET);
                } catch (IOException e) {
                    LogUtils.printMessage("读取配置文件失败", LogUtils.Level.ERROR);
                }
            } else if (configSource.startsWith("json://")) {
                jsonString = configSource.substring(7);
            }
            if (jsonString != null) {
                appConfig = JSONObject.parseObject(jsonString);
                LogUtils.printMessage("用户配置已加载");
             } else {
                 appConfig = new JSONObject();
                appConfig.put("jobs", new JSONArray());
             }
        }
        catch (Exception e) {
            System.out.println(configSource);
             LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
             App.exit(-1);
         }
    }
    /**
     * 注册邮件服务
     * @param env spring环境
     * @return 邮件服务实例
     */
    @Bean
    public MailService registerMailService(ConfigurableEnvironment env) {
        JSONObject mailConfig = appConfig.getJSONObject("mail");
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
        JSONArray jsonArray = appConfig.getJSONArray("jobs");
        return jsonArray==null?new JSONArray():jsonArray;
    }

    public void addCacheItem(String key, Object value) {
        appCache.put(key, value);
        cache();
    }

    public <T> T getCacheItem(String key, Class<T> type) {
        return appCache.getObject(key, type);
    }

    public String getCacheItem(String key) {
        return appCache.getObject(key, String.class);
    }

    public void removeCacheItem(String key) {
        appCache.remove(key);
        cache();
    }

    public void cache() {
        try {
            JSON.writeJSONString(new FileOutputStream(APP_CACHE), APP_CHARSET, appCache);
        } catch (Exception e) {
            LogUtils.printMessage("保存缓存失败", LogUtils.Level.ERROR);
        }
    }

    public <T> T getConfigItem(String key, Class<T> type) {
        return appConfig.getObject(key, type);
    }
}