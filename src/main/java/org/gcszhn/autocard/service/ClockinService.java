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

import java.io.Closeable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONObject;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.gcszhn.autocard.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * 健康打卡实现类
 * @author Zhang.H.N
 * @version 1.1
 */
@Scope("prototype")
@Service
public class ClockinService implements Closeable {
    /**打卡信息网址 */
    @Value("${app.autoCard.reportUrl}")
    /**打卡提交网址 */
    private String reportUrl;
    @Value("${app.autoCard.submitUrl}")
    private String submitUrl;
    /**浙大通行证客户端 */
    @Autowired
    private ZJUClientService client;
    /**时间格式化 */
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    /**
     * 用于访问打卡页面
     * @param username 用户名
     * @param password 密码
     * @return 打卡页面HTML源码
     */
    public String getPage(String username, String password) {
        if(client.login(username, password)) {
            return client.doGet(reportUrl);
        }
        return null;
    }
    /**
     * 用于提取已有提交信息
     * @param username 用户名
     * @param password 密码
     * @return 已有提交信息组成的键值对列表
     */
    public ArrayList<NameValuePair> getOldInfo(String username, String password) {
        String page = getPage(username, password);
        if (page==null) return null;
        ArrayList<NameValuePair> res = new ArrayList<>();
        try {
            Pattern pattern = Pattern.compile("oldInfo: (\\{.+\\})");
            Matcher matcher = pattern.matcher(page);
            if (matcher.find()) {
                JSONObject oldInfoJson = JSONObject.parseObject(matcher.group(1));
                oldInfoJson.forEach((String name, Object value)->{
                    switch (name) {
                        //case "sfzx":value="1";break;
                        case "date":value=sdf.format(new Date());break;
                    }

                    res.add(new BasicNameValuePair(name, String.valueOf(value)));
                });
            }
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
        return res;
    }
    /**
     * 用于提交打卡信息
     * @param username 用户名
     * @param password 密码
     * @return 打卡成败
     */
    public boolean submit(String username, String password) {
        LogUtils.printMessage("Try to submit for " + username);
        ArrayList<NameValuePair> info = getOldInfo(username, password);
        if (info==null) {
            LogUtils.printMessage("Submit failed", LogUtils.Level.ERROR);
            return false;
        }
        client.doPost(submitUrl, info);
        LogUtils.printMessage("Finish info submit...", LogUtils.Level.INFO);
        return true;
    }
    @Override
    public void close() {
        try {
            client.close();
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
    }
}
