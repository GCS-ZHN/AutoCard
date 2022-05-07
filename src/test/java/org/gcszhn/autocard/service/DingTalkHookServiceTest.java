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

import org.gcszhn.autocard.AppTest;
import org.gcszhn.autocard.utils.SpringUtils;
import org.gcszhn.autocard.utils.StatusCode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DingTalkHookServiceTest extends AppTest {
    @Autowired
    DingTalkHookService service;
    private String encrypt_url = null;

    @Before
    public void addSignature() {
        encrypt_url = service.getSignature(SECRET, PAYLOAD_URL);
        System.out.println(encrypt_url);
    }

    @Test
    public void sendTextTest() {
        Assert.assertEquals(service.sendText(encrypt_url, "打卡信息获取失败, @"+ PHONE, false, PHONE).getStatus(), 0);
    }

    @Test
    public void sendMarkdownTest() {
        Assert.assertEquals(service.sendMarkdown(encrypt_url, "杭州天气", "### 杭州天气 \n> 9度，西北风1级，空气良89，相对温度73%\n> ![screenshot](https://img.alicdn.com/tfs/TB1NwmBEL9TBuNjy1zbXXXpepXa-2400-1218.png)\n> ###### 10点20分发布 [天气](https://www.dingalk.com) \n", true).getStatus(), 0);
    }

    @Test
    public void sendPhotoTest() {
        ZJUClientService clientService = SpringUtils.getBean(ZJUClientService.class);
        if (clientService.login(USERNAME, PASSWORD)) {
            String photo = clientService.getUserPhoto();
            StatusCode statusCode = service.sendMarkdown(encrypt_url, USERNAME, "### "+USERNAME+"\n你好\n![img](data:image/gif;base64,"+photo+")");
            System.out.println(statusCode.getMessage());
            Assert.assertEquals(statusCode.getStatus(), 0);
        }
    }
}
