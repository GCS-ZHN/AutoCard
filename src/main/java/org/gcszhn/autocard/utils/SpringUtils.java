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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 非bean组件与Spring交互的工具
 */
@Component
public class SpringUtils implements ApplicationContextAware {
    /**Spring容器 */
    private static ApplicationContext context;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;       
    }
    /**
     * 根据Bean名称获取Bean实例
     * @param name 名称
     * @return Bean组件实例
     */
    public static Object getBean(String name) {
        if (context == null) return null;
        return context.getBean(name);
    }
    /**
     * 根据Bean类型获取Bean实例，要求没有歧义
     * @param <T> 具体类型
     * @param cls class实例
     * @return Bean组件实例
     */
    public static <T> T getBean(Class<T> cls) {
        if (context == null) return null;
        return context.getBean(cls);
    }
}
