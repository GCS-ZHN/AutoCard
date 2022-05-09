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
