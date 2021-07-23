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


import java.io.Closeable;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.gcszhn.autocard.AppConfig;
import org.gcszhn.autocard.utils.LogUtils;
import org.gcszhn.autocard.utils.LogUtils.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import lombok.Setter;

/**
 * 邮件服务
 * @author Zhang.H.N
 * @version 1.0
 */
@Service
public class MailService implements Closeable {
    /**服务是否可用 */
    private @Setter boolean serviceAvailable;
    /**邮件会话对象 */
    private Session mailSession = null;
    /**邮件储存空间 */
    private Store store = null;
    /**发件昵称 */
    @Value("${mail.auth.nickname}")
    private String nickname;
    /**发件邮箱用户名 */
    @Value("${mail.auth.username}")
    private String username;
    /**发件邮箱密码 */
    @Value("${mail.auth.password}")
    private String password;
    /**imap的host */
    @Value("${mail.imap.host}")
    private String imapHost;
    /**
     * 环境配置注入
     * @param env spring环境
     */
    @Autowired
    public void setEnvironment(Environment env) {
        Properties props = new Properties(10);
        String[] keys = {
            "mail.transport.protocol",
            "mail.store.protocol",
            "mail.smtp.class",
            "mail.imap.class",
            "mail.smtp.host",
            "mail.smtp.port",
            "mail.imap.port",
            "mail.smtp.ssl.enable",
            "mail.smtp.auth",
            "mail.smtp.starttls.enable",
            "mail.debug"
        };
        for (String key: keys) {
            props.setProperty(key, env.getProperty(key));
        }
        if (username.equals("")|password.equals("")) {
            LogUtils.printMessage("Mail user not set");
            setServiceAvailable(false);
            return;
        }
        setServiceAvailable(true);
        LogUtils.printMessage("Mail account is "+username , LogUtils.Level.INFO);
        LogUtils.printMessage("Mail password is "+username , LogUtils.Level.DEBUG);
        mailSession = Session.getDefaultInstance(props , new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username , password);
            }
        });
        try {
            store = mailSession.getStore("imap");
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }
    /**邮件服务是否可用 */
    public boolean isServiceAvailable() {
        if (!serviceAvailable) LogUtils.printMessage("Mail service unavailable");
        return serviceAvailable;
    }
    /**连接IMAP服务 */
    public void connection() {
        if (!isServiceAvailable()) return;
        try {
            store.connect(imapHost, username, password);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    /**关闭IMAP服务 */
    public void close() {
        if (store==null) return;
        try {
            store.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    /**
     * 发送给自己邮箱的邮件
     * @param subject 主题
     * @param content 内容
     * @param contentType 内容Mine类型与编码
     */
    public synchronized void sendMySelfMail(String subject, Object content, String contentType) {
        sendMail(username, subject, content, contentType);
    }
    /**
     * 发送指定邮箱特定邮件
     * @param toAddress 目标邮箱地址
     * @param subject 主题
     * @param content 内容
     * @param contentType 内容Mine类型与编码
     */
    public synchronized void sendMail(String toAddress, String subject, Object content, String contentType) {
        if (!isServiceAvailable()) return;
        MimeMessage msg = new MimeMessage(mailSession);
        Folder sent = null;
        try {
            InternetAddress[] toAddrs = InternetAddress.parse(toAddress, false);
            msg.setRecipients(Message.RecipientType.TO, toAddrs);
            msg.setSubject(subject, AppConfig.APP_CHARSET.name());
            msg.setFrom(new InternetAddress(username, nickname, AppConfig.APP_CHARSET.name()));
            msg.setContent(content, contentType);

            Transport.send(msg);
            LogUtils.printMessage("Send to " + toAddress +" successfully!");
        } catch (Exception e) {
            LogUtils.printMessage("Send to " + toAddress +" failed!", Level.ERROR);
            LogUtils.printMessage(e.getMessage(), Level.ERROR);
            e.printStackTrace();
        } finally {
            if (sent != null && sent.isOpen())
                try {
                    sent.close(true);
                } catch (MessagingException e) {
                    LogUtils.printMessage(e.getMessage(), Level.ERROR);
                }
        }
    }
    /**
     * 读取邮箱文件夹信息
     * @param mailfolder 邮箱文件夹名称
     * @param openModel 打开邮箱文件夹模式，在Folder类中定义相关常量
     * @return 邮箱文件夹对象
     */
    public Folder readMailFolder(String mailfolder, int openModel) {
        if (!isServiceAvailable()) return null;
        Folder inbox = null;
        try {
            connection();
            inbox = store.getFolder(mailfolder);
            inbox.open(openModel);
            System.out.println("You have " + inbox.getMessageCount() + " emails in " + mailfolder);
            System.out.println("You have " + inbox.getUnreadMessageCount() + " unread emails in " + mailfolder);
        } catch (MessagingException e) {
            LogUtils.printMessage(e.getMessage(), Level.ERROR);
        } finally {
            close();
        }
        return inbox;
    }
    /**
     * 读取收件箱信息
     * @param openModel 打开邮箱文件夹模式，在Folder类中定义相关常量
     * @return 邮箱文件夹对象
     */
    public Folder readInbox(int openModel) {
        return readMailFolder("inbox", openModel);
    }
    /**
     * 读取发件箱信息
     * @param openModel 打开邮箱文件夹模式，在Folder类中定义相关常量
     * @return 邮箱文件夹对象
     */
    public Folder readSentbox(int openModel) {
        return readMailFolder("Sent Messages", openModel);
    }
}