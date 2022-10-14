/*
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
 */
package top.gcszhn.autocard.utils;

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
