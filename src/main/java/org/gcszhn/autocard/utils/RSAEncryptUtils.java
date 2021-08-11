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

import java.math.BigInteger;
/**
 * RSA不对称加密的工具，由于浙大通行证的RSA加密采用最原始的RSA加密方式，故不能用java提供的API
 * @author Zhang.H.N
 */
public class RSAEncryptUtils {
    /**屏蔽构造函数 */
    private RSAEncryptUtils() {}
    /**
     * 利用公钥的模和幂次，直接进行RSA加密
     * @param info 信息的字节数据
     * @param modulus 模的十六进制字符串 
     * @param exponent 幂的十六进制字符串
     * @return 加密后字节的十六进制字符串编码
     */
    public static String encrypt(byte[] info, String modulus, String exponent) {
        BigInteger modulusInt = new BigInteger(modulus, 16);
        BigInteger exponentInt = new BigInteger(exponent, 16);
        return encrypt(info, modulusInt, exponentInt);
    }
    /**
     * 利用公钥的模和幂次，直接进行RSA加密
     * @param info 信息的字节数据
     * @param modulus 模的大整数类型
     * @param exponent 幂的大整数类型
     * @return 加密后字节的十六进制字符串编码
     */
    public static String encrypt(byte[] info, BigInteger modulus, BigInteger exponent) {
        BigInteger infoInt = new BigInteger(info);
        BigInteger result = infoInt.modPow(exponent, modulus);
        return result.toString(16);
    }
}