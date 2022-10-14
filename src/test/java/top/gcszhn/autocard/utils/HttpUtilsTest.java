/*
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
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
        System.out.println(client.doGet("https://www.baidu.com/"));
        LogUtils.printMessage("结束");
    }
    @Test
    public void getWithParamTest() {
        ArrayList<NameValuePair> params = new ArrayList<>(1);
        params.add(new BasicNameValuePair("wd", "HttpPost和httpclient"));
        System.out.println(client.doGet("https://www.baidu.com/s", params));
    }
}
