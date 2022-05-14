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
package top.gcszhn.autocard.utils;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import top.gcszhn.autocard.AppTest;

/**
 * HttpClientUtils的单元测试
 * @author Zhang.H.N
 * @version 1.0
 */
public class HttpUtilsTest extends AppTest {
    HttpClientUtils client;
    @Before
    public void beforeTest() {
        client = new HttpClientUtils("test.cache");
    }
    @After
    public void afterTest() throws IOException {
        client.close();
    }
    @Test
    public void getTest() {
        LogUtils.printMessage("开始");
        System.out.println(client.doGet("https://www.google.com/"));
        LogUtils.printMessage("结束");
    }
    @Test
    public void getWithParamTest() {
        ArrayList<NameValuePair> params = new ArrayList<>(1);
        params.add(new BasicNameValuePair("wd", "HttpPost和httpclient"));
        System.out.println(client.doGet("https://www.baidu.com/s", params));
    }
}
