/*
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
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
