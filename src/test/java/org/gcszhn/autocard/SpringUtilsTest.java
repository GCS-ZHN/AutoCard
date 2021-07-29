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
package org.gcszhn.autocard;

import org.gcszhn.autocard.service.JobService;
import org.gcszhn.autocard.service.ZJUClientService;
import org.gcszhn.autocard.utils.SpringUtils;
import org.junit.Assert;
import org.junit.Test;

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
