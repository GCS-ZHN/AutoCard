/*
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
 */
package top.gcszhn.autocard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import lombok.Getter;
import top.gcszhn.autocard.service.MailService;
import top.gcszhn.autocard.utils.LogUtils;


/**
 * App通用配置和组件注册
 * @author Zhang.H.N
 * @version 1.1
 */
@Configuration
public class AppConfig implements EnvironmentAware {
    /**默认字符集 */
    public static final Charset APP_CHARSET = StandardCharsets.UTF_8;
    /** 创建APP临时目录*/
    public static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir") , "autocard");
    static {
        try {
            TMP_DIR.mkdirs();
        } catch (Exception e) {
            LogUtils.printMessage("APP创建临时目录失败", e, LogUtils.Level.ERROR);
            App.exit(-1);
        }
    }
    /**APP缓存文件 */
    private static final String APP_CACHE = "autocard_cache.json";
    /**应用缓存 */
    private  JSONObject appCache;
    /**JSON配置文件 */
    private JSONObject appConfig;
    /**是否为测试模式 */
    private @Getter boolean testMode = false;
    /**是否启用预览特性 */
    private @Getter boolean enablePreview = false;
    /** 加载APP配置文件*/
    public AppConfig() {
        try (FileInputStream fis = new FileInputStream(APP_CACHE)) {
            appCache = JSON.parseObject(new String(fis.readAllBytes(), APP_CHARSET));
            LogUtils.printMessage("缓存加载成功", LogUtils.Level.DEBUG);
        } catch (Exception e) {
            appCache = new JSONObject();
            LogUtils.printMessage("缓存加载失败", LogUtils.Level.DEBUG);
        }
    }
    /**
     * SpringBoot 2.x无法在Configuration中使用@Value，因此需要获取springboot环境
     */
    @Override
    public void setEnvironment(Environment env) {
        try {
            loadJSONConfig(env.getProperty("app.autoCard.config"));
            testMode = appConfig.getBooleanValue("testmode");
            enablePreview = appConfig.getBooleanValue("enablepreview");
            
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
                global_user.put("maxtrial", System.getenv("AUTOCARD_MAX_TRIAL"));
                global_user.put("nickname", System.getenv("AUTOCARD_NICKNAME"));
                appConfig.getJSONArray("jobs").add(global_user);
            }
            String javaTmpDir = System.getProperty("java.io.tmpdir");
            if (javaTmpDir == null) {
                javaTmpDir = ".";
            }
            if (testMode) {
                LogUtils.printMessage("测试模式已开启");
            }
        } catch (Exception e) {
            LogUtils.printMessage("APP环境初始化失败", e, LogUtils.Level.ERROR);
            App.exit(-1);
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
            LogUtils.printMessage("保存缓存成功", LogUtils.Level.DEBUG);
        } catch (Exception e) {
            LogUtils.printMessage("保存缓存失败", LogUtils.Level.ERROR);
        }
    }

    public <T> T getConfigItem(String key, Class<T> type) {
        return appConfig.getObject(key, type);
    }
}