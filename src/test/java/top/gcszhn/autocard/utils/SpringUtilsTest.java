/*
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
 */
package top.gcszhn.autocard.utils;

import org.junit.Assert;
import org.junit.Test;

import top.gcszhn.autocard.AppTest;
import top.gcszhn.autocard.service.JobService;
import top.gcszhn.autocard.service.ZJUClientService;

/**
 * Spring工具测试
 * @author Zhang.H.N
 * @version 1.1
 */
public class SpringUtilsTest extends AppTest {
    @Test
    public void protoTypeTest() {
        Assert.assertNotEquals(
            "两个原型bean不应该相同", 
            SpringUtils.getBean(ZJUClientService.class), 
            SpringUtils.getBean(ZJUClientService.class)
            );
    }
    @Test
    public void rawTypeTest() {
        Assert.assertEquals(
            "两个单例bean应该相同", 
            SpringUtils.getBean(JobService.class), 
            SpringUtils.getBean(JobService.class)
            );
    }
}
