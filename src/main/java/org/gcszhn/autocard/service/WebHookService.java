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
package org.gcszhn.autocard.service;

import org.gcszhn.autocard.utils.StatusCode;

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
