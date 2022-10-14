/*
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
 */
package top.gcszhn.autocard.service;

import top.gcszhn.autocard.AppTest;
import top.gcszhn.autocard.utils.StatusCode;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 打卡服务测试
 * @author Zhang.H.N
 * @version 1.0
 */
public class AutoCardServiceTest extends AppTest {
    @Autowired
    AutoCardService autoCardService;

    @Autowired
    DingTalkHookService dingTalkHookService;
    @After
    public void afterTest() throws Exception {
        autoCardService.close();
    }
    @Test
    public void getPageTest() {
        try {
            for (int i = 0; i < 2; i++) {
                autoCardService.login(USERNAME, PASSWORD);
                String page = autoCardService.getPage();
                Assert.assertNotNull(page);
                Assert.assertTrue(autoCardService.formValidation(page));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void isSubmitTest() {
        autoCardService.login(USERNAME, PASSWORD);
        if(autoCardService.isOnline()) Assert.assertEquals(autoCardService.isSubmited(autoCardService.getPage()), true);
    }
    @Test
    public void getOldInfoTest() {
        autoCardService.login(USERNAME, PASSWORD);
        System.out.println(autoCardService.getOldInfo(autoCardService.getPage()));
    }
    @Test
    public void submitReportTest() {
        StatusCode statusCode = autoCardService.submit(USERNAME, PASSWORD, NICKNAME);
        Assert.assertNotEquals(statusCode.getStatus(), -1);
    }
}
