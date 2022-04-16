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
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 定时任务服务测试
 * @author Zhang.H.N
 * @version 1.0
 */
public class JobServiceTest extends AppTest {
    @Autowired
    JobService jobService;
    @Test
    public void test() throws InterruptedException {
       synchronized(jobService) {
        jobService.addJob(AutoCardJob.class, null, null);
        jobService.start();
        jobService.wait(50000); 
       }
    }
}
