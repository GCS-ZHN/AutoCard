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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.alibaba.fastjson.JSONObject;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.gcszhn.autocard.AppConfig;
import org.gcszhn.autocard.utils.HttpClientUtils;
import org.gcszhn.autocard.utils.LogUtils;
import org.gcszhn.autocard.utils.RSAEncryptUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;



/**
 * 访问浙大通行证的客户端
 * @author Zhang.H.N
 * @version 1.1
 */
@Scope("prototype")
@Service
public class ZJUClientService extends HttpClientUtils {
    /**获取公钥n,e值的API */
    @Value("${app.zjuClient.pubkeyUrl}")
    private String pubkeyUrl;
    /**浙大通行证登录页面 */
    @Value("${app.zjuClient.loginUrl}")
    private String loginUrl;
    /**是否将cookie缓存至文件 */
    @Value("${app.zjuClient.cookieCached}")
    private boolean cookieCached;
    /**
     * 获取RSA公钥的模和幂
     * @return 模、幂的十六进制字符串组
     */
    public String[] getPublicKey() {
        LogUtils.printMessage("Try to get modulus, exponent for public key", LogUtils.Level.DEBUG);
        try {
            String info = doGetText(pubkeyUrl);
            JSONObject json = JSONObject.parseObject(info);
            return new String[]{json.getString("modulus"), json.getString("exponent")};
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
        return null;
    }
    /**
     * 获取Execution参数
     * @return Execution参数的字符串
     */
    public String getExecution() {
        LogUtils.printMessage("Try to get execution value", LogUtils.Level.DEBUG);
        try {
            String body = doGetText(loginUrl);
            if (body==null) return null;
            Document document = Jsoup.parse(body);
            if (!document.title().equals("应用中心")) {
                return document.getElementsByAttributeValue("name", "execution").val();
            }
            LogUtils.printMessage("Login by cookie directly...");
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
        return null;
    }
    /**
     * 登录浙大通行证
     * @param username 用户名，即学工号
     * @param password 密码
     * @return 登录状态，true为成功，false为失败
     */
    public boolean login(String username, String password) {
        return login(username, password, null, false) != null;
    }
    /**
     * 登录采用浙大通行证API的指定服务
     * @param username 用户名，即学工号
     * @param password 密码
     * @param targetService 使用浙大通行证的目标服务，即浏览器页面的service参数，必须是URL编码后的参数
     * @return 响应正文
     */
    public String login(String username, String password, String targetService) {
        return login(username, password, targetService, false);
    }
    /**
     * 登录采用浙大通行证API的指定服务
     * @param username 用户名，即学工号
     * @param password 密码
     * @param targetService 使用浙大通行证的目标服务，即浏览器页面的service参数
     * @param urlAutoEncode 是否需要对服务参数url进行URL编码，若targetService为非编码，需要设置为true
     * @return 响应正文
     */
    public String login(String username, String password, String targetService, boolean urlAutoEncode) {
        String execution = getExecution();
        if (execution==null) return null;
        String[] publicKey = getPublicKey();
        if (publicKey != null && username!=null && password!=null) {
            String pwdEncrypt=RSAEncryptUtils.encrypt(
                password.getBytes(AppConfig.APP_CHARSET), publicKey[0], publicKey[1]);
            LogUtils.printMessage("Try to login", LogUtils.Level.INFO);
            ArrayList<NameValuePair> parameters = new ArrayList<>(5);
            parameters.add(new BasicNameValuePair("username", username));
            parameters.add(new BasicNameValuePair("password", pwdEncrypt));
            parameters.add(new BasicNameValuePair("_eventId", "submit"));
            parameters.add(new BasicNameValuePair("authcode", ""));
            parameters.add(new BasicNameValuePair("execution", execution));
            Header[] headers = {
                new BasicHeader("Content-Type", "application/x-www-form-urlencoded"),
                new BasicHeader("Referer", "https://zjuam.zju.edu.cn/cas/login"),
                new BasicHeader("Origin", "https://zjuam.zju.edu.cn"),
                new BasicHeader("Upgrade-Insecure-Requests", "1"),
                new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"),
                new BasicHeader("Accept-Encoding", "gzip, deflate, br"),
                new BasicHeader("Accept-Language","zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
            };
            if (targetService != null) {
                loginUrl += "?service="+(urlAutoEncode?URLEncoder.encode(targetService, Consts.UTF_8):targetService);
            }

            //登录正常时，返回为302重定向
            CloseableHttpResponse response = doPost(loginUrl, parameters, headers);
            boolean status = response.getStatusLine().getStatusCode() == 302;
            String textContent = entityToString(getResponseContent(response));
            if (status && textContent!=null) {
                LogUtils.printMessage("Login successfully: " + username, LogUtils.Level.INFO);
                return textContent;
            }
        }
        LogUtils.printMessage("Login failed: " + username, LogUtils.Level.ERROR);
        return null;
    }
    @Override
    public void close() throws IOException {
        setCookieCached(cookieCached);
        super.close();
    }
}