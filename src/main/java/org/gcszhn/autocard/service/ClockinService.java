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
import org.gcszhn.autocard.utils.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * 健康打卡实现类
 * @author Zhang.H.N
 * @version 1.2.1
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
            return client.doGetText(reportUrl);
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
        // 该部分模拟网页JS代码进行信息合并
        try {
            Pattern defPattern = Pattern.compile("var def = (\\{.+?\\});", Pattern.DOTALL);
            Matcher matcher = defPattern.matcher(page);
            JSONObject defJsonObject = null;
            if (matcher.find()) {
                defJsonObject = JSONObject.parseObject(matcher.group(1));
            } else {
                return null;
            }
            Pattern info = Pattern.compile("\\$\\.extend\\((\\{.+?\\}), def, (\\{.+?\\})\\)", Pattern.DOTALL);
            JSONObject infoJsonObject1 = null;
            JSONObject infoJsonObject2 = null;
            matcher = info.matcher(page);
            if (matcher.find()) {
                infoJsonObject1 = JSONObject.parseObject(matcher.group(1));
                infoJsonObject2 = JSONObject.parseObject(matcher.group(2));
            } else {
                return null;
            }
            Pattern oldInfoPattern = Pattern.compile("oldInfo: (\\{.+?\\}),\n");
            matcher = oldInfoPattern.matcher(page);
            JSONObject oldInfoJson = null;
            if (matcher.find()) {
                oldInfoJson = JSONObject.parseObject(matcher.group(1));
            } else {
                return null;
            }
            infoJsonObject1.putAll(defJsonObject);
            infoJsonObject1.putAll(infoJsonObject2);
            infoJsonObject1.putAll(oldInfoJson);
            infoJsonObject1.forEach((String name, Object value)->{
                switch (name) {
                    case "date"  : value=sdf.format(new Date());break;
                    case "bztcyy": value="";break;   //地区变更需要手动打卡一次，过滤上一次的地区变更原因
                }
                // fix bug for "是否从下列地区返回浙江错误"
                if (name.equals("jrdqtlqk") && value.equals("")) return;
                if (value==null||value.toString().equals("[]")) return; //空数组不上报
                res.add(new BasicNameValuePair(name, String.valueOf(value)));
            });
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
        return res;
    }
    /**
     * 用于提交打卡信息
     * @param username 用户名
     * @param password 密码
     * @return 打卡状态
     *  0   打卡成功
     *  1   今日已经打卡
     * -1   打卡失败
     */
    public StatusCode submit(String username, String password) {
        StatusCode statusCode = new StatusCode();
        LogUtils.printMessage("准备提交打卡 " + username);
        ArrayList<NameValuePair> info = getOldInfo(username, password);
        if (info==null) {
            LogUtils.printMessage("打卡信息获取失败", LogUtils.Level.ERROR);
            statusCode.setMessage(username+", 打卡信息获取失败");
            statusCode.setStatus(-1);
            return statusCode;
        }
        String area = null;
        for (NameValuePair pair: info) {
            if (pair.getName().equals("area")) {
                area = pair.getValue();
                break;
            }
        }
        JSONObject resp = JSONObject.parseObject(client.doPostText(submitUrl, info));
        int status = resp.getIntValue("e");
        LogUtils.Level level = null;
        switch(status) {
            case 0:{level= LogUtils.Level.INFO;break;}
            case 1:{level= LogUtils.Level.ERROR;break;}
        }
        statusCode.setStatus(status);
        statusCode.setMessage(client.getUserInfo().getString("userName")+"您好，"+resp.getString("m")+"，打卡地区为："+ area+"（如若区域不符，请次日手动打卡更改地址）");
        LogUtils.printMessage(resp.getString("m"), level);
        LogUtils.printMessage("地点："+area);
        return statusCode;
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
