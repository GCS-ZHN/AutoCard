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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONObject;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.gcszhn.autocard.AppConfig;
import org.gcszhn.autocard.utils.DigestUtils;
import org.gcszhn.autocard.utils.ImageUtils;
import org.gcszhn.autocard.utils.LogUtils;
import org.gcszhn.autocard.utils.StatusCode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
public class AutoCardService implements AppService {
    /**时间格式化 */
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd");
    /**表达校验数据缓存关键字 */
    private static final String FORM_MD5_VALUE= "FORM_MD5_VALUE";
    /**打卡信息网址 */
    @Value("${app.autoCard.reportUrl}")
    /**打卡提交网址 */
    private String reportUrl;
    @Value("${app.autoCard.submitUrl}")
    private String submitUrl;
    /**浙大通行证客户端 */
    @Autowired
    private ZJUClientService client;
    /**应用配置实例 */
    @Autowired
    private AppConfig appConfig;
    /**
     * 用于访问打卡页面
     * @param username 用户名
     * @param password 密码
     * @return 打卡页面HTML源码
     */
    public String getPage(String username, String password) {
        if(client.login(username, password)) {
            String page1 = client.doGetText(reportUrl);
            Boolean formvalidation = appConfig.getConfigItem("formvalidation", Boolean.class);
            if (formvalidation!=null && !formvalidation) {
                return page1;
            }
            String page2 = client.doGetText(reportUrl);
            boolean page1Flag = formValidation(page1);
            boolean page2Flag = formValidation(page2);
            if ( page1Flag == (page1!=null) && page2Flag == (page2!=null)) {
                LogUtils.printMessage("表单校验通过", LogUtils.Level.INFO);
                return page1;
            }  else  if (page1 != null && page2 != null && page1Flag != page2Flag) {
                // 意味着两次获取的页面表单是变化的，无法作为校验依据
                LogUtils.printMessage("表单校验功能失效，已忽略校验，请联系作者", LogUtils.Level.ERROR);
                return page1;
            } else {
                LogUtils.printMessage("表单校验失败，请检查健康打卡页面是否更新或等待一会再次尝试，若更新请删除缓存文件并重启打卡程序", LogUtils.Level.ERROR);
            }
        }
        return null;
    }
    /**
     * 表单数据的MD5校验，浙大健康打卡时常更新，但后端验证不够及时，因此进行前端验证
     * @param html 表单的html页面
     * @return true为验证通过
     */
    public boolean formValidation(String html) {
        try {
            if (html != null) {
                Document document = Jsoup.parse(html);
                Element form = document.getElementsByClass("form-detail2").last();
                if (form != null) {
                    String digest = DigestUtils.digest(form.html(), "MD5");
                    if (appConfig.getCacheItem(FORM_MD5_VALUE) == null) {
                        appConfig.addCacheItem(FORM_MD5_VALUE, digest);
                    } 
                    if (appConfig.getCacheItem(FORM_MD5_VALUE).equals(digest)) {
                        return true;
                    }
                } else {
                    LogUtils.printMessage("未捕获表单信息，捕获信息如下", LogUtils.Level.ERROR);
                    System.out.println(html);
                }
            }
        } catch (Exception e) {
            LogUtils.printMessage(e.getMessage(), e, LogUtils.Level.ERROR);
        }
        return false;
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
                    case "date"  : value=SDF.format(new Date());break;
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
            statusCode.setMessage(username+"的打卡信息获取失败，可能是打卡更新了或网络不稳定，请查看后台打卡日志输出");
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
        JSONObject userInfo = client.getUserInfo();
        String message = String.format("%s，你好，今日自动健康打卡状态：%s，打卡地区为：%s（如若区域不符，请次日手动打卡更改地址）",
            userInfo==null? username: userInfo.getString("userName"), 
            resp.getString("m"),
            area);
        statusCode.setStatus(status);
        statusCode.setMessage(message);

        if (appConfig.isEnablePreview()) {
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put("id", username);
            jsonMessage.put("name", userInfo==null? username: userInfo.getString("userName"));
            jsonMessage.put("message", message);
            String photo = client.getUserPhoto();
            if (photo != null) {
                photo = ImageUtils.toBase64(ImageUtils.resize(ImageUtils.toImage(photo), 75, 100), "gif");
                jsonMessage.put("photo", "data:image/gif;base64,"+photo);
            }
            statusCode.setJsonMessage(jsonMessage);
        }

        LogUtils.printMessage(resp.getString("m"), level);
        LogUtils.printMessage("地点："+area);
        return statusCode;
    }
    @Override
    public void close() {
        try {
            client.close();
            System.out.println("AutoCardService stopped");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 通过清除cookie来登出
     */
    public void logout() {
        client.clearCookie();
    }
}
