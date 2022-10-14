/*
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
 */
package top.gcszhn.autocard.service;

import top.gcszhn.autocard.utils.StatusCode;

/**
 * 通用的Webhook服务接口
 * @author Zhang.H.N
 */
public interface WebHookService extends AppService {
    /**
     * 发送文本消息
     * @param payLoadURL 目标webhook
     * @param info 文本消息
     * @return 发送状态对象
     */
    public StatusCode sendText(String payLoadURL, String info);

    /**
     * 发送markdown格式消息
     * @param payLoadURL 目标webhook
     * @param title 消息标题
     * @param content 消息内容
     * @return 发送状态对象
     */
    public StatusCode sendMarkdown(String payLoadURL, String title, String content);
}
