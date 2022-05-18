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
package top.gcszhn.autocard.service;

import top.gcszhn.autocard.AppTest;
import top.gcszhn.autocard.utils.StatusCode;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.image.BufferedImage;
import java.io.File;

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
        StatusCode statusCode = autoCardService.submit(USERNAME, PASSWORD);
        Assert.assertNotEquals(statusCode.getStatus(), -1);
    }
}
