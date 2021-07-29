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
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.gcszhn.autocard.service.ZJUClientService;
import org.junit.After;
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
    public void getTest() {
        System.out.println(client.doGet("https://www.cc98.org/"));;
    }
    @Test
    public void getWithParamTest() {
        ArrayList<NameValuePair> params = new ArrayList<>(1);
        params.add(new BasicNameValuePair("wd", "HttpPost和httpclient"));
        System.out.println(client.doGet("https://www.baidu.com/s", params));
    }
    @Test
    public void getPublicKeyTest() {
        System.out.println(client.getPublicKey());;
    }
    @Test
    public void getExecutionTest() {
        System.out.println(client.getExecution());
    }
    @Test
    public void postTest() {
        ArrayList<NameValuePair> params = new ArrayList<>(1);
        params.add(new BasicNameValuePair("username", "zhanghn"));
        System.out.println(client.doPost("https://zjuam.zju.edu.cn/cas/login", params));
    }
    @Test
    public void loginTest() {
        client.login("12019018", "zju244220");
    }
}