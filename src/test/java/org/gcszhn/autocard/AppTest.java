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

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * App统一的单元测试抽象类
 * @author Zhang.H.N
 * @version 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class AppTest {
    protected static final String trueZjuPassPortUser = "0****";
    protected static final String trueZjuPassPortPass = "*****";
    protected final String PAYLOAD_URL = "https://oapi.dingtalk.com/robot/send?access_token=****";
    protected final String SECRET = "SEC****";
}
