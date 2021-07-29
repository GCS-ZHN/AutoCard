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
package org.gcszhn.autocard.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.gcszhn.autocard.App;
import org.gcszhn.autocard.AppConfig;

import lombok.Getter;
import lombok.Setter;

/**
 * 通用Http客户端工具
 * @author Zhang.H.N
 * @version 1.2
 */
public class HttpClientUtils implements Closeable {
    /**User-Agent请求头 */
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0";
    /**是否将cookie缓存 */
    private @Setter @Getter boolean cookieCached;
    /**是否将cookie缓存 */
    private @Getter boolean redirectsEnabled;
    /**cookie缓存文件 */
    private String cookieFile;
    /**Http客户端 */
    private CloseableHttpClient httpClient;
    /**cookie储存实例 */
    CookieStore cookieStore;
    /**默认参数构造 */
    public HttpClientUtils() {
        this("cookies.cache", true, 10);
    }
    /**
     * 创建Http客户端实例
     * @param cookieFile cookie文件, null时不缓存
     * @param redirectsEnabled 是否自动重定向，仅对GET请求有效，POST等请求需要自行重定向
     * @param maxRedirects 最大重定向次数
     */
    public HttpClientUtils(String cookieFile, boolean redirectsEnabled, int maxRedirects) {
        setCookieCached(cookieFile!=null);
        this.cookieFile = cookieFile;
        this.redirectsEnabled = redirectsEnabled;
        //运行循环重定向但限制循环次数，
        RequestConfig config = RequestConfig.custom()
            .setCircularRedirectsAllowed(true)
            .setMaxRedirects(maxRedirects)
            .setRedirectsEnabled(redirectsEnabled)
            .build(); 
        initCookieStore();
        httpClient = HttpClients.custom()
            .setDefaultCookieStore(cookieStore)
            .setDefaultRequestConfig(config)
            .setUserAgent(USER_AGENT).build();
    }
    /**
     * 初始化cookie储存实例，根据设定选择是否加载缓存
     */
    private void initCookieStore() {
        if (!isCookieCached()) {
            cookieStore = new BasicCookieStore();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
            new File(App.workDir, cookieFile)))) {
            cookieStore = (CookieStore) ois.readObject();
            LogUtils.printMessage("Cookie loaded from cache...", LogUtils.Level.INFO);
        } catch (Exception e) {
            cookieStore = new BasicCookieStore();
            LogUtils.printMessage(e.getMessage(), LogUtils.Level.DEBUG);
        }
    }
    /**
     * 判断HTTP响应状态是否成功
     * @param response 响应实例
     * @return true为成功
     */
    public CloseableHttpResponse isSuccess(CloseableHttpResponse response) {
        if (response==null) return null;
        StatusLine statusLine = response.getStatusLine();
        try {
            if (statusLine.getStatusCode()<400) {
                return doRedirects(response);
            }
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
        LogUtils.printMessage(statusLine.toString(), LogUtils.Level.ERROR);
        return null;
    }
    private CloseableHttpResponse doRedirects(CloseableHttpResponse response) {
        if (!isRedirectsEnabled()||response==null) return response;
        try {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 302) {
                String url = response.getFirstHeader("Location").getValue();
                if (url!=null) {
                    response.close();
                    return doGet(url);
                }
            }
            return response;
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
        return null;
    }
    /**
     * 获取响应正文的字符串编码数据
     * @param response 响应实例
     * @return 请求正文实体
     */
    public HttpEntity getResponseContent(CloseableHttpResponse response) {
        response = isSuccess(response);
        try {
            if (response!=null) {
                return response.getEntity();
            };
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
        return null;
    }
    /**
     * 获取请求的响应
     * @param request 请求实例 
     * @param headers 可选请求头
     * @return 响应实例
     */
    private CloseableHttpResponse getResponse(HttpUriRequest request, Header... headers) {
        request.setHeaders(headers);
        
        try  {
            return httpClient.execute(request);
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
        return null;
    }
    /**
     * 无参数的get请求
     * @param url 目标地址
     * @param headers 可选请求头
     * @return 响应
     */
    public CloseableHttpResponse doGet(String url, Header... headers) {
        return doGet(url, null, headers);
    }
    /**
     * 带参数的get请求
     * @param url 目标地址
     * @param parameters 参数键值对的列表
     * @param headers 可选请求头
     * @return 响应
     */
    public CloseableHttpResponse doGet(String url, List<NameValuePair> parameters,Header... headers) {
        try {
            URIBuilder uriBuilder = new URIBuilder(url, AppConfig.APP_CHARSET);
            if (parameters != null) uriBuilder.setParameters(parameters);
            URI uri = uriBuilder.build();
            HttpGet request = new HttpGet(uri);
            LogUtils.printMessage("Try to get " + uri.toString(), LogUtils.Level.DEBUG);
            return getResponse(request, headers);
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
        return null;
    }
    /**
     * 无参数的post请求
     * @param url 目标地址
     * @param headers 可选请求头
     * @return 响应正文
     */
    public CloseableHttpResponse doPost(String url, Header... headers) {
        return doPost(url, null, headers);
    }
    /**
     * 带参数的post请求
     * @param url 目标地址
     * @param parameters 参数键值对的列表
     * @param headers 可选请求头
     * @return 响应
     */
    public CloseableHttpResponse doPost(String url, List<NameValuePair> parameters, Header... headers) {
        LogUtils.printMessage("Try to post " + url, LogUtils.Level.DEBUG);
        try {
            HttpPost request = new HttpPost(url);
            if (parameters != null) {
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters, AppConfig.APP_CHARSET);
                request.setEntity(formEntity);
            }
            return getResponse(request, headers);
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
        return null;
    }
    /**
     * 无参数get请求获取文本内容
     * @param url 目标地址
     * @param headers 请求头
     * @return 文本
     */
    public String doGetText(String url, Header... headers) {
        return doGetText(url, null, headers);
    }
    /**
     * 带参数get请求获取文本内容
     * @param url 目标地址
     * @param parameters 参数列表
     * @param headers 请求头
     * @return 文本
     */
    public String doGetText(String url, List<NameValuePair> parameters, Header... headers) {
        return entityToString(getResponseContent(doGet(url, parameters, headers)));
    }
    /**
     * 无参数Post请求获取文本
     * @param url 目标地址
     * @param headers 请求头
     * @return 文本
     */
    public String doPostText(String url, Header... headers) {
        return doPostText(url, null, headers);
    }
    /**
     * 带参数Post请求获取文本
     * @param url 目标地址
     * @param parameters 参数列表
     * @param headers 请求头
     * @return 文本
     */
    public String doPostText(String url, List<NameValuePair> parameters, Header... headers) {
        return entityToString(getResponseContent(doPost(url, parameters, headers)));
    }
    public void doDownload(String filename, String url, Header...headers) {
        doDownload(filename, url, Methods.GET, headers);
    }
    public void doDownload(String filename, String url, Methods methods, Header... headers) {
        try(FileOutputStream fos = new FileOutputStream(filename)) {
            HttpEntity entity = null;
            switch (methods) {
                case GET:entity=getResponseContent(doGet(url, headers)); break;
                case POST:entity=getResponseContent(doPost(url, headers)); break;
                default:throw new UnsupportedOperationException("Only support GET/POST currently");
            }
            if (entity!=null) {
                byte[] buffer = new byte[1024];
                InputStream inputStream = entity.getContent();
                int len;
                while((len=inputStream.read(buffer))!=-1) {
                    fos.write(buffer, 0, len);
                }
            }
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
    }
    /**
     * 将响应实体编码为字符串，主要用于文本响应解析
     * @param entity 响应实体
     * @return 编码为字符串
     */
    public String entityToString(HttpEntity entity){
        try {
            if (entity != null) {
                return EntityUtils.toString(entity);
            }
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
        return null;
    }
    /**方法名称 */
    public enum Methods {
        GET, POST, PUT, DELETE, HEAD
    }
    /**
     * 关闭资源时缓存cookie
     */
    @Override
    public void close() throws IOException {
        if (isCookieCached()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(new File(App.workDir, cookieFile)))) {
                oos.writeObject(cookieStore);
            }
        }
        httpClient.close();
        LogUtils.printMessage("Close basic client...", LogUtils.Level.INFO);
    }
}