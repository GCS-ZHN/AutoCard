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

@Service
public class DingTalkHookService implements WebHookService {
    private HttpClientUtils utils = new HttpClientUtils();
    private static final String DINGTALK_URL = "https://oapi.dingtalk.com/robot/send?access_token=";
    @Override
    public StatusCode sendText(String payLoadURL, String info) {
        StatusCode statusCode = new StatusCode();
        if (!payLoadURL.startsWith(DINGTALK_URL)) {
            statusCode.setStatus(1);
            statusCode.setMessage("无效的钉钉机器人URL");
        } else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msgtype", "text");
            JSONObject textObject = new JSONObject();
            textObject.put("content", info);
            jsonObject.put("text", textObject);
            JSONObject res = JSON.parseObject(utils.entityToString(utils.getResponseContent(utils.doPost(payLoadURL, jsonObject.toJSONString(), "application/json"))));
            statusCode.setStatus(res.getIntValue("errcode"));
            statusCode.setMessage(res.getString("errmsg"));
        }

        return statusCode;
    }

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
    
}
