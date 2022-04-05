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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.gcszhn.autocard.AppConfig;
import org.gcszhn.autocard.utils.HttpClientUtils;
import org.gcszhn.autocard.utils.LogUtils;
import org.gcszhn.autocard.utils.LogUtils.Level;
import org.gcszhn.autocard.utils.RSAEncryptUtils;
import org.gcszhn.autocard.utils.StatusCode;
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
    private String[] getPublicKey() {
        LogUtils.printMessage("Try to get modulus, exponent for public key", Level.DEBUG);
        try {
            String info = doGetText(pubkeyUrl);
            JSONObject json = JSONObject.parseObject(info);
            return new String[]{json.getString("modulus"), json.getString("exponent")};
        } catch (Exception e) {
            LogUtils.printMessage(null, e, Level.ERROR);
        }
        return null;
    }
    /**
     * 获取Execution参数
     * @return 获取的状态码实例
     *  1   已经登录
     *  0   获取成功
     * -1   异常
     */
    private StatusCode getExecution() {
        LogUtils.printMessage("Try to get execution value", Level.DEBUG);
        StatusCode statusCode = new StatusCode();
        try {
            /**暂时创建一个禁止自动重定向的客户端 */
            setHttpClient(false, 0);
            CloseableHttpResponse response = doGet(loginUrl);
            int httpStatus = response.getStatusLine().getStatusCode();
            if (httpStatus==302) {
                statusCode.setStatus(1);
            } else {
                String textContent = entityToString(getResponseContent(response));
                Document document = Jsoup.parse(textContent);
                statusCode.setStatus(0);
                statusCode.setMessage(document.getElementsByAttributeValue("name", "execution").val());
            }
        } catch (Exception e) {
            LogUtils.printMessage(null, e, Level.ERROR);
            statusCode.setStatus(-1);
        } finally{
            //恢复自动GET重定向
            setHttpClient(true, 10);
        }
        return statusCode;
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
        try {
            String targetUrl = loginUrl;
            if (targetService != null) {
                targetUrl += "?service="+(urlAutoEncode?URLEncoder.encode(targetService, Consts.UTF_8):targetService);
            }
            //获取提交参数
            StatusCode execution = getExecution();
            switch(execution.getStatus()) {
                case  1: {                                 // 已经登录
                    if (checkUserInfo(username)) {
                        return doGetText(targetUrl);
                    }
                    return null;
                }  
                case -1: return null;                  // 获取异常
                case  0: break;                        // 获取正常
                default:return null;
            }
            LogUtils.printMessage("正在登录 " + username);
            if (username==null||password==null||username.isEmpty()||password.isEmpty()) 
                throw new NullPointerException("User not set");
            String[] publicKey = getPublicKey();
            String pwdEncrypt=RSAEncryptUtils.encrypt(
                password.getBytes(AppConfig.APP_CHARSET), publicKey[0], publicKey[1]);
            ArrayList<NameValuePair> parameters = new ArrayList<>(5);
            parameters.add(new BasicNameValuePair("username", username));
            parameters.add(new BasicNameValuePair("password", pwdEncrypt));
            parameters.add(new BasicNameValuePair("_eventId", "submit"));
            parameters.add(new BasicNameValuePair("authcode", ""));
            parameters.add(new BasicNameValuePair("execution", execution.getMessage()));
    
            //登录正常时，返回为302重定向
            CloseableHttpResponse response = doPost(loginUrl, parameters);
            String textContent = targetService!=null?entityToString(getResponseContent(response)):"";
            if (checkUserInfo(username)) {
                return textContent;
            }
        } catch (Exception e) {
            LogUtils.printMessage(null, e, Level.ERROR);
        }
        LogUtils.printMessage("登录失败 " + username, Level.ERROR);
        return null;
    }
    public JSONObject getUserInfo() {
        String info = doGetText("https://service.zju.edu.cn");
        if (info != null) {
            Pattern pattern = Pattern.compile("\"site\": \"(\\w+)\"");
            Matcher matcher = pattern.matcher(info);
            if (matcher.find()) {
                String portalContext = matcher.group(1);
                String userInfo = doGetText("https://service.zju.edu.cn/_web/portal/api/user/loginInfo.rst?_p="+portalContext);
                JSONObject res = JSON.parseObject(userInfo);
                if (!res.getString("result").equals("1")) {
                    LogUtils.printMessage(res.getString("reason"), Level.ERROR);
                }
                return res.getJSONObject("data");
            }
        }
        LogUtils.printMessage("No port context found", Level.ERROR);
        return null;
    }
    public boolean checkUserInfo(String username) {
        JSONObject userInfo = getUserInfo();
        if (userInfo != null) {
            String id = userInfo.getString("loginName");
            String name = userInfo.getString("userName");
            if (id.equals(username) && name != null && id != null) {
                LogUtils.printMessage("登录成功，学工号："+id+"，姓名："+name);
                return true;
            }
            LogUtils.printMessage("想要登录用户与已经登录用户不一致", Level.ERROR);
            LogUtils.printMessage("想要登录用户："+username, Level.ERROR);
            LogUtils.printMessage("已经登录用户："+id, Level.ERROR);
        }
        LogUtils.printMessage("登录失败：" + username, Level.ERROR);
        return false;
    }
    @Override
    public void close() throws IOException {
        setCookieCached(cookieCached);
        super.close();
    }
}