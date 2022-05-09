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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class IOUtils {
    /**
     * 将JAR包内资源提取出来
     * @param source jar内部文件URI
     * @param target 目标文件名
     * @throws IOException
     */
    public static void extractJarResource(String source, File target) throws IOException {
        try(InputStream in = IOUtils.class.getResourceAsStream(source);
            FileOutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[1 << 11];
            int len;
            while ((len = in.read(buffer)) != -1){
                out.write(buffer, 0, len);
                out.flush();
            }
        }
    }
}
