/*
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
 */
package top.gcszhn.autocard.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.Setter;
import top.gcszhn.autocard.AppConfig;
import top.gcszhn.autocard.utils.*;
import top.gcszhn.autocard.utils.LogUtils.Level;
import lombok.Getter;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * 访问浙大通行证的客户端
 * 
 * @author Zhang.H.N
 * @version 1.2
 */
@Scope("prototype")
@Service
public class ZJUClientService extends HttpClientUtils {
    /** 获取公钥n,e值的API */
    @Value("${app.zjuClient.pubkeyUrl}")
    private String pubkeyUrl;
    /** 浙大通行证登录页面 */
    @Value("${app.zjuClient.loginUrl}")
    private String loginUrl;
    /** 是否将cookie缓存至文件 */
    @Value("${app.zjuClient.cookieCached}")
    private boolean cookieCached;

    /**
     * 获取RSA公钥的模和幂
     * 
     * @return 模、幂的十六进制字符串组
     */
    private String[] getPublicKey() {
        LogUtils.printMessage("Try to get modulus, exponent for public key", Level.DEBUG);
        try {
            String info = doGetText(pubkeyUrl);
            return Optional.ofNullable(JSONObject.parseObject(info))
                    .map((JSONObject json) -> {
                        return new String[] { json.getString("modulus"), json.getString("exponent") };
                    }).orElse(null);
        } catch (Exception e) {
            LogUtils.printMessage(null, e, Level.ERROR);
        }
        return null;
    }

    /**
     * 获取Execution参数
     * 
     * @return 获取的状态码实例
     *         1 已经登录
     *         0 获取成功
     *         -1 异常
     */
    private StatusCode getExecution() {
        LogUtils.printMessage("Try to get execution value", Level.DEBUG);
        StatusCode statusCode = new StatusCode();
        try {
            /** 暂时创建一个禁止自动重定向的客户端 */
            setHttpClient(false, 0);
            HttpDataPair dataPair = doGet(loginUrl);
            int httpStatus = dataPair.getResponse().getStatusLine().getStatusCode();
            if (httpStatus == 302) {
                statusCode.setStatus(1);
            } else {
                String textContent = getTextContent(dataPair);
                Document document = Jsoup.parse(textContent);
                statusCode.setStatus(0);
                statusCode.setMessage(document.getElementsByAttributeValue("name", "execution").val());
            }
        } catch (Exception e) {
            LogUtils.printMessage(null, e, Level.ERROR);
            statusCode.setStatus(-1);
        } finally {
            // 恢复自动GET重定向
            setHttpClient(true, 10);
        }
        return statusCode;
    }

    /**
     * 登录浙大通行证
     * 
     * @param username 用户名，即学工号
     * @param password 密码
     * @return 登录状态，true为成功，false为失败
     */
    public boolean login(String username, String password) {
        return login(username, password, null, false) != null;
    }

    /**
     * 登录采用浙大通行证API的指定服务
     * 
     * @param username      用户名，即学工号
     * @param password      密码
     * @param targetService 使用浙大通行证的目标服务，即浏览器页面的service参数，必须是URL编码后的参数
     * @return 响应正文
     */
    public String login(String username, String password, String targetService) {
        return login(username, password, targetService, false);
    }

    /**
     * 登录采用浙大通行证API的指定服务
     * 
     * @param username      用户名，即学工号
     * @param password      密码
     * @param targetService 使用浙大通行证的目标服务，即浏览器页面的service参数
     * @param urlAutoEncode 是否需要对服务参数url进行URL编码，若targetService为非编码，需要设置为true
     * @return 响应正文
     */
    public String login(String username, String password, String targetService, boolean urlAutoEncode) {
        try {
            String targetUrl = loginUrl;
            if (targetService != null) {
                targetUrl += "?service="
                        + (urlAutoEncode ? URLEncoder.encode(targetService, Consts.UTF_8) : targetService);
            }
            // 获取提交参数
            StatusCode execution = getExecution();
            switch (execution.getStatus()) {
                case 1: { // 已经登录
                    if (checkUserInfo(username)) {
                        return doGetText(targetUrl);
                    }
                    return null;
                }
                case -1:
                    return null; // 获取异常
                case 0:
                    break; // 获取正常
                default:
                    return null;
            }
            LogUtils.printMessage("正在登录 " + username);
            if (username == null || username.isEmpty())
                throw new ZJUClientException(ZJUClientException.USER_MISS_ERROR);
            if (password == null || password.isEmpty())
                throw new ZJUClientException(ZJUClientException.PASSWORD_MISS_ERROR);
            String[] publicKey = getPublicKey();
            if (publicKey == null) {
                throw new ZJUClientException(ZJUClientException.PUBKEY_MISS_ERROR);
            }
            String pwdEncrypt = RSAEncryptUtils.encrypt(
                    password.getBytes(AppConfig.APP_CHARSET), publicKey[0], publicKey[1]);
            ArrayList<NameValuePair> parameters = new ArrayList<>(5);
            parameters.add(new BasicNameValuePair("username", username));
            parameters.add(new BasicNameValuePair("password", pwdEncrypt));
            parameters.add(new BasicNameValuePair("_eventId", "submit"));
            parameters.add(new BasicNameValuePair("authcode", ""));
            parameters.add(new BasicNameValuePair("execution", execution.getMessage()));

            HttpDataPair dataPair = doPost(loginUrl, parameters);
            String textContent = targetService != null ? getTextContent(dataPair) : "";
            if (checkUserInfo(username)) {
                return textContent;
            }
            if (dataPair != null)
                dataPair.close();
        } catch (Exception e) {
            LogUtils.printMessage(null, e, Level.ERROR);
        }
        LogUtils.printMessage("登录失败 " + username, Level.ERROR);
        return null;
    }

    /**
     * 获取用户信息
     * 
     * @return 包含用户信息的json对象
     */
    public JSONObject getUserInfo() {
        Optional<String> infoOp = Optional.ofNullable(doGetText("https://service.zju.edu.cn"));
        return infoOp.map((String info) -> {
            Pattern pattern = Pattern.compile("\"site\": \"(\\w+)\"");
            Matcher matcher = pattern.matcher(info);
            if (matcher.find()) {
                String portalContext = matcher.group(1);
                String userInfo = doGetText(
                        "https://service.zju.edu.cn/_web/portal/api/user/loginInfo.rst?_p=" + portalContext);
                JSONObject res = JSON.parseObject(userInfo);
                if (!res.getString("result").equals("1")) {
                    LogUtils.printMessage(res.getString("reason"), Level.ERROR);
                }
                return res.getJSONObject("data");
            }
            return null;
        }).orElseGet(() -> {
            LogUtils.printMessage("账号信息获取失败", Level.ERROR);
            return null;
        });
    }

    /**
     * 获取用户头像
     * 
     * @return Base64编码的image/gif类型图像
     */
    public String getUserPhoto() {
        Optional<JSONObject> userInfo = Optional.ofNullable(getUserInfo());
        return userInfo.map((JSONObject uInfo) -> {
            String id = uInfo.getString("loginName");
            if (id == null)
                return null;
            doGet("http://mapp.zju.edu.cn/_web/_customizes/pc/public/person.html");
            Optional<String> photo = Optional
                    .ofNullable(doGetText("http://mapp.zju.edu.cn/getPhotoBase64.do?xgh=" + id));
            return photo.map((String p) -> p.replaceAll("\\{img=|\\}|\n", "")).orElse(null);
        }).orElseGet(() -> {
            LogUtils.printMessage("账号照片获取失败", Level.ERROR);
            return null;
        });
    }

    /**
     * 检查用户信息
     * 
     * @param username 预期用户名
     * @return 是否一致
     */
    public boolean checkUserInfo(String username) {
        Optional<JSONObject> userInfoOp = Optional.ofNullable(getUserInfo());
        Optional<Boolean> result = userInfoOp.map((JSONObject userInfo) -> {
            String id = userInfo.getString("loginName");
            String name = userInfo.getString("userName");
            if (id.equals(username) && name != null && id != null) {
                LogUtils.printMessage("登录成功 " + id);
                return true;
            }
            LogUtils.printMessage("想要登录用户与已经登录用户不一致", Level.ERROR);
            LogUtils.printMessage("想要登录用户：" + username, Level.ERROR);
            LogUtils.printMessage("已经登录用户：" + id, Level.ERROR);
            return false;
        });
        if (!result.orElse(false)) {
            LogUtils.printMessage("登录失败：" + username, Level.ERROR);
        }
        return result.orElse(false);
    }

    @Override
    public void close() throws IOException {
        setCookieCached(cookieCached);
        super.close();
    }
}

class ZJUClientException extends RuntimeException {
    public static final int PUBKEY_MISS_ERROR = 1;
    public static final int USER_MISS_ERROR = 2;
    public static final int PASSWORD_MISS_ERROR = 3;
    public static final int EXECUTION_MISS_ERROR = 4;

    private @Getter @Setter int errorCode;

    public ZJUClientException(int errorCode) {
        super(getMessage(errorCode));
        setErrorCode(errorCode);
    }

    private static String getMessage(int errorCode) {
        switch (errorCode) {
            case PUBKEY_MISS_ERROR:
                return "缺少公钥";
            case USER_MISS_ERROR:
                return "确少用户名";
            case PASSWORD_MISS_ERROR:
                return "缺少密码";
            case EXECUTION_MISS_ERROR:
                return "缺少执行码";
            default:
                return "未知错误";
        }
    }
}