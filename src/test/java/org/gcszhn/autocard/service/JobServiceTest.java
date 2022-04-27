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

import org.gcszhn.autocard.AppTest;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

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
        dataMap.put("mail", MAIL);
        dataMap.put("dingtalkurl", PAYLOAD_URL);
        dataMap.put("dingtalksecret", SECRET);
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
