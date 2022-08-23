/* 
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
 */
package top.gcszhn.autocard.utils;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


public class DigestUtils {
    public static String digest(String message, String algorithm) throws NoSuchAlgorithmException {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(data);
        return new String(Base64.getEncoder().encode(md.digest()), StandardCharsets.UTF_8);
    }
}
