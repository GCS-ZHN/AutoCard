/*
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
 */
package top.gcszhn.autocard.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import top.gcszhn.autocard.AppTest;
import top.gcszhn.autocard.utils.SpringUtils;
import top.gcszhn.autocard.utils.StatusCode;

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
        Assert.assertEquals(0, service.sendMarkdown(encrypt_url, "杭州天气", "### 杭州天气 \n> 9度，西北风1级，空气良89，相对温度73%\n> ![screenshot](https://img.alicdn.com/tfs/TB1NwmBEL9TBuNjy1zbXXXpepXa-2400-1218.png)\n> ###### 10点20分发布 [天气](https://www.dingalk.com) \n", true).getStatus());
    }

    @Test
    public void sendPhotoTest() {
        ZJUClientService clientService = SpringUtils.getBean(ZJUClientService.class);
        if (clientService.login(USERNAME, PASSWORD)) {
            String photo = clientService.getUserPhoto();
            StatusCode statusCode = service.sendMarkdown(encrypt_url, USERNAME, "### "+USERNAME+"\n你好\n![img](data:image/gif;base64,"+photo+")");
            System.out.println(statusCode.getMessage());
            Assert.assertEquals(0, statusCode.getStatus());
        }
    }
}
