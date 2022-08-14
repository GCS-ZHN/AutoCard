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


import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.core.env.Environment;

import lombok.Setter;
import top.gcszhn.autocard.AppConfig;
import top.gcszhn.autocard.utils.LogUtils;
import top.gcszhn.autocard.utils.LogUtils.Level;

/**
 * 邮件服务
 * @author Zhang.H.N
 * @version 1.1
 */
public class MailService implements AppService {
    /**服务是否可用 */
    private @Setter boolean serviceAvailable;
    /**邮件会话对象 */
    private Session mailSession = null;
    /**发件昵称 */
    private @Setter String nickname = "AutoCard";
    /**发件邮箱用户名 */
    private @Setter String username = null;
    /**发件邮箱密码 */
    private @Setter String password = null;
    /**SMTP服务host */
    private @Setter String smtpHost = null;
    /**SMTP服务port */
    private @Setter String smtpPort = null;
    /**
     * 环境配置注入
     * @param env spring环境
     */
    public void setEnvironment(Environment env) {
        Properties props = new Properties(10);
        String[] keys = {
            "mail.transport.protocol",
            "mail.smtp.class",
            "mail.smtp.host",
            "mail.smtp.port",
            "mail.smtp.ssl.enable",
            "mail.smtp.ssl.protocols",
            "mail.smtp.auth",
            "mail.smtp.starttls.enable",
            "mail.debug"
        };
        for (String key: keys) {
            props.setProperty(key, env.getProperty(key));
        }
        if (smtpHost!=null) {
            props.setProperty("mail.smtp.host", smtpHost);
        }
        if (smtpPort!=null) {
            props.setProperty("mail.smtp.port", smtpPort);
        }
        if (username==null|password==null) {
            LogUtils.printMessage("Mail user not set");
            setServiceAvailable(false);
            return;
        }
        setServiceAvailable(true);
        LogUtils.printMessage("Mail sender is "+username);
        LogUtils.printMessage("Mail password is "+username , LogUtils.Level.DEBUG);
        mailSession = Session.getDefaultInstance(props , new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username , password);
            }
        });
    }
    /**邮件服务是否可用 */
    public boolean isServiceAvailable() {
        if (!serviceAvailable) LogUtils.printMessage("Mail service unavailable");
        return serviceAvailable;
    }
    
    /**
     * 发送指定邮箱特定邮件
     * @param toAddress 目标邮箱地址
     * @param subject 主题
     * @param content 内容
     * @param contentType 内容Mine类型与编码
     */
    public void sendMail(String toAddress, String subject, Object content, String contentType) {
        if (!isServiceAvailable() || toAddress == null || subject == null || content == null || contentType == null) return;
        MimeMessage msg = new MimeMessage(mailSession);
        Folder sent = null;
        try {
            InternetAddress[] toAddrs = InternetAddress.parse(toAddress, false);
            msg.setRecipients(Message.RecipientType.TO, toAddrs);
            msg.setSubject(subject, AppConfig.APP_CHARSET.name());
            msg.setFrom(new InternetAddress(username, nickname, AppConfig.APP_CHARSET.name()));
            msg.setContent(content, contentType);
            Transport.send(msg);
            LogUtils.printMessage("发送给" + toAddress +"通知邮件成功!");
        } catch (Exception e) {
            LogUtils.printMessage("发送给" + toAddress +"通知邮件失败!",  Level.ERROR);
            LogUtils.printMessage(e.getMessage(), Level.ERROR);
            e.printStackTrace();
        } finally {
            Optional.ofNullable(sent).ifPresent((Folder folder)->{
                try {
                    if (folder.isOpen()) folder.close(true);
                } catch (MessagingException e) {
                    LogUtils.printMessage(e.getMessage(), Level.ERROR);
                }
            });
        }
    }
    @Override
    public void close() throws IOException {
        mailSession = null;
        setServiceAvailable(false);
    }
}