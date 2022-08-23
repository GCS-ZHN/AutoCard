/* 
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
 */
package top.gcszhn.autocard.service;

import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import top.gcszhn.autocard.AppTest;

/**
 * 定时任务服务测试
 * @author Zhang.H.N
 * @version 1.0
 */
public class JobServiceTest extends AppTest {
    @Autowired
    JobService jobService;
    private JobDataMap dataMap;
    @Autowired
    private MailService mailService;
    @Autowired
    private AutoCardService cardService;
    @Autowired
    private DingTalkHookService dingTalkHookService;

    @Before
    public void initDataMap() {
        dataMap = new JobDataMap();
        dataMap.put("username", USERNAME);
        dataMap.put("password", PASSWORD);
//        dataMap.put("mail", MAIL);
//        dataMap.put("dingtalkurl", PAYLOAD_URL);
//        dataMap.put("dingtalksecret", SECRET);
    }

    @Test
    public void cronTest() throws InterruptedException {
       synchronized(jobService) {
        jobService.addJob(AutoCardJob.class, null, dataMap);
        jobService.start();
        jobService.wait(50000); 
       }
    }
    @Test
    public void executeTest() {
        try {
            AutoCardJob.execute(dataMap, mailService, cardService, dingTalkHookService);
        } catch (JobExecutionException e) {
            e.printStackTrace();
        }
    }
}
