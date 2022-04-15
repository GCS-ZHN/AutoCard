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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.gcszhn.autocard.utils.HttpClientUtils;
import org.gcszhn.autocard.utils.LogUtils;
import org.gcszhn.autocard.utils.StatusCode;
import org.gcszhn.autocard.utils.LogUtils.Level;
import org.springframework.stereotype.Service;

/**
 * Dingtalk robots的webhook实现
 * @apiNote https://open.dingtalk.com/document/robots
 */
@Service
public class DingTalkHookService implements WebHookService {
    /**Http服务 */
    private HttpClientUtils client = new HttpClientUtils();
    /**Dingtalk robots基础API */
    private static final String DINGTALK_URL = "https://oapi.dingtalk.com/robot/send?access_token=";
    /**
     * 发送消息
     * @param payLoadURL 机器人URL
     * @param message 信息对象
     * @param type 信息类型
     * @param isAtAll 是否at全体
     * @return 发送状态对象
     */
    public StatusCode send(String payLoadURL, JSONObject message, String type, boolean isAtAll) {
        StatusCode statusCode = checkURL(payLoadURL);
        if (statusCode.getStatus() == 0)  {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msgtype", type);
            jsonObject.put(type, message);
            jsonObject.put("at", new JSONObject());
            jsonObject.getJSONObject("at").put("isAtAll", isAtAll);
            JSONObject res = JSON.parseObject(client.entityToString(client.getResponseContent(client.doPost(payLoadURL, jsonObject.toJSONString(), "application/json"))));
            statusCode.setStatus(res.getIntValue("errcode"));
            statusCode.setMessage(res.getString("errmsg"));
        }
        return statusCode;
    }

    @Override
    public StatusCode sendText(String payLoadURL, String info) {
        return sendText(payLoadURL, info, false);
    }

    /**
     * 发送文本
     * @param payLoadURL 机器人URL
     * @param info 信息
     * @param isAtAll 是否at全体
     * @return 发送状态对象
     */
    public StatusCode sendText(String payLoadURL, String info, boolean isAtAll) {
        JSONObject message = new JSONObject();
        message.put("content", info);
        return send(payLoadURL, message, "text", isAtAll);
    }

    @Override
    public StatusCode sendMarkdown(String payLoadURL, String title, String content) {
        return sendMarkdown(payLoadURL, title, content, false);
    }

    /**
     * 发送Markdown格式信息
     * @param payLoadURL 机器人URL
     * @param title 消息标题
     * @param content 消息内容
     * @param isAtAll 是否at全体
     * @return 发送状态对象
     */
    public StatusCode sendMarkdown(String payLoadURL, String title, String content, boolean isAtAll) {
        JSONObject message = new JSONObject();
        message.put("title", title);
        message.put("text", content);
        return send(payLoadURL, message, "markdown", isAtAll);
    }

    /**
     * 对机器人URL进行加签，加签的URL具有有效期，必须在一定时间内发送给dingtalk
     * @param secret 加签的密钥
     * @param payLoadURL 机器人URL
     * @return 加签后的机器人URL
     */
    public String getSignature(String secret, String payLoadURL) {
        try {
            Long timestamp = System.currentTimeMillis();
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            return payLoadURL +"&timestamp="+timestamp+ "&sign=" + URLEncoder.encode(new String(Base64.encodeBase64(signData)),"UTF-8");
        } catch (Exception e) {
            LogUtils.printMessage(e.getMessage(), Level.ERROR);
        }
        return payLoadURL;
    }

    /**
     * 检查是否为合法的dingtalk url
     * @param payLoadURL 待检测的URL
     * @return 是否合法
     */
    private StatusCode checkURL(String payLoadURL) {
        StatusCode statusCode = new StatusCode();
        if (!payLoadURL.startsWith(DINGTALK_URL)) {
            statusCode.setStatus(1);
            statusCode.setMessage("无效的钉钉机器人URL");
        } else {
            statusCode.setStatus(0);
            statusCode.setMessage("钉钉机器人URL合法");
        }
        return statusCode;
    }

    @Override
    public void close() throws IOException {
        client.close();
        System.out.println("DingTalkHookService stopped");
        //LogUtils.printMessage("Service stopped");
    }
}
