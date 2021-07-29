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

import java.io.IOException;

import org.gcszhn.autocard.service.ZJUClientService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 浙大通行证客户端测试
 * @author Zhang.H.N
 * @version 1.0
 */
public class ZJUClientTest extends AppTest {
    @Autowired
    ZJUClientService client;
    //ZJUClient是prototpye的bean，IOC容器不负责销毁
    @After
    public void afterTest() throws IOException {
        client.close();
    }
    @Test
    public void getPublicKeyTest() {
        String[] pub = client.getPublicKey();
        Assert.assertNotNull(pub);
    }
    @Test
    public void getExecutionTest() {
        Assert.assertNotEquals(-1, client.getExecution().getStatus());
    }
    /**
     * 浙大通行证登录测试
     */
    @Test
    public void loginTest() {
        Assert.assertEquals(false, client.login("dadadada", "dadad"));
        Assert.assertEquals(true, client.login(trueZjuPassPortUser, trueZjuPassPortPass));
    }
    /**
     * 以研究生教务网为例，测试基于浙大通行证登录第三方
     */
    @Test
    public void loginGrsTest() {
        String text = client.doGetText("https://grs.zju.edu.cn/cas/login?service=http%3A%2F%2Fgrs.zju.edu.cn%2Fallogene%2Fpage%2Fhome.htm");
        Document document = Jsoup.parse(text);
        String href = document.getElementsByTag("fieldset").first().getElementsByTag("a").first().attr("href");
        if (client.login(trueZjuPassPortUser, trueZjuPassPortPass)){
            client.doGetText(href);
            client.doDownload(
                "test/fig/test.png", 
                "http://grs.zju.edu.cn/allogene/page/home.htm?pageAction=getPic");
        }
    }
}