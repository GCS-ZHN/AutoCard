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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.gcszhn.autocard.service.ZJUClientService;
import org.gcszhn.autocard.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;


/**
 * App通用配置和组件注册
 * @author Zhang.H.N
 * @version 1.0
 */
@Configuration
public class AppConfig {
    /**默认字符集 */
    public static final Charset APP_CHARSET = StandardCharsets.UTF_8;
    /**默认用户名 */
    private String defaultUserName;
    /**默认密码 */
    private String defaultPassword;
    /**
     * 配置默认用户名与密码
     * @param environment spring环境
     */
    @Autowired
    public void setDefaultUser(Environment environment) {
        defaultUserName = environment.getProperty("username");
        defaultPassword = environment.getProperty("password");
        if (defaultPassword==null||defaultUserName==null) {
            LogUtils.printMessage("No user set", LogUtils.Level.ERROR);
            App.exit(-1);
        }
        LogUtils.printMessage("User is "+defaultUserName, LogUtils.Level.INFO);
        LogUtils.printMessage("Password is "+defaultUserName, LogUtils.Level.DEBUG);
    }
    /**
     * 注册ZJUClientService的Bean
     */
    @Scope("prototype")
    @Bean
    public ZJUClientService addZjuClientService() {
        return new ZJUClientService(defaultUserName, defaultPassword);
    }
}