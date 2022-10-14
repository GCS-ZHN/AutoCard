/*
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
 */
package top.gcszhn.autocard;

import java.util.ResourceBundle;

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
    protected static final String USERNAME ;
    protected static final String PASSWORD;
    protected static final String MAIL;
    protected static final String PAYLOAD_URL;
    protected static final String SECRET;
    protected static final String PHONE;

    protected static final String NICKNAME;
    static {
        ResourceBundle bundle = ResourceBundle.getBundle("test_config");
        USERNAME = bundle.getString("username");
        PASSWORD = bundle.getString("password");
        MAIL = bundle.getString("mail");
        PAYLOAD_URL = bundle.getString("dingtalkurl");
        SECRET = bundle.getString("dingtalksecret");
        PHONE = bundle.getString("telephone");
        NICKNAME = bundle.getString("nickname");
    }
}
