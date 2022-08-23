/* 
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
 */
package top.gcszhn.autocard.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import top.gcszhn.autocard.AppTest;

/**
 * 邮件服务测试
 * @author Zhang.H.N
 * @version 1.0
 */
public class MailServiceTest extends AppTest {
    @Autowired
    MailService mailService;
    @Test
    public void sendTest() {
        mailService.sendMail("zhanghn@zju.edu.cn","test", "test", "text/html;charset=utf-8");
    }
}