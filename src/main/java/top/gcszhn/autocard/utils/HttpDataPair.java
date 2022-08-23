/* 
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
 */
package top.gcszhn.autocard.utils;

import lombok.Data;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

import java.io.Closeable;
import java.io.IOException;

@Data
public class HttpDataPair implements Closeable {
    HttpRequestBase request;
    CloseableHttpResponse response;

    @Override
    public void close() throws IOException {
        if (response != null) {
            EntityUtils.consumeQuietly(response.getEntity());
            response.close();
        }
        if (request != null) {
            request.releaseConnection();
        }
        LogUtils.printMessage("关闭连接" + request.getURI(), LogUtils.Level.DEBUG);
    }
}
