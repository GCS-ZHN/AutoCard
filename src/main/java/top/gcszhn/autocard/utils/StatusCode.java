/*
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
 */
package top.gcszhn.autocard.utils;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;

/**
 * 用于创建状态及信息
 * @author Zhang.H.N
 * @version 1.0
 */
@Data
public class StatusCode {
    /**状态码 */
    private int status = 0;
    /**信息，可以是返回正文，也就是状态描述 */
    private String message = null;
    /**JSON格式信息，部分数据返回报文复杂，采用json格式更加合适 */
    private JSONObject jsonMessage = null;
}
