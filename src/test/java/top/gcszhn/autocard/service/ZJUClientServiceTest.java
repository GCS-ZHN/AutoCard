/*
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
 */
package top.gcszhn.autocard.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;

import top.gcszhn.autocard.AppTest;
import top.gcszhn.autocard.utils.ImageUtils;

/**
 * 浙大通行证客户端测试
 * @author Zhang.H.N
 * @version 1.0
 */
public class ZJUClientServiceTest extends AppTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Autowired
    ZJUClientService client;
    //ZJUClient是prototpye的bean，IOC容器不负责销毁
    @After
    public void afterTest() throws IOException {
        client.close();
    }
    /**
     * 浙大通行证登录测试
     */
    @Test
    public void loginTest() {
        Assert.assertEquals(false, client.login("dadadada", "dadad"));
        Assert.assertEquals(true, client.login(USERNAME, PASSWORD));
    }
    @Test
    public void getUserInfoTest() {
        if (client.login(USERNAME, PASSWORD)) {
            System.out.println(client.getUserInfo());
        }
    }
    @Test
    public void getUserPhotoTest() throws IOException {
        if (client.login(USERNAME, PASSWORD)) {
            String photo = client.getUserPhoto();
            if (photo != null) {
                ImageUtils.write(photo, folder.newFile(USERNAME+ "-raw.gif"));
                BufferedImage image = ImageUtils.toImage(photo);
                ImageUtils.write(image, "gif", folder.newFile(USERNAME+ "-t.gif"));
                image = ImageUtils.resize(image, 75, 100);
                ImageUtils.write(image, "gif", folder.newFile(USERNAME+ "-resize.gif"));
            }
        }
    }
    
    @Test
    public void loginCourseTest() throws IOException {
        String userUrl="https://courses.zju.edu.cn/user/index";
        String coursesUrl="https://courses.zju.edu.cn/api/users/%s/courses";
        String memberUrl="https://courses.zju.edu.cn/api/course/%d/enrollments?fields=user(email,name,user_no,department(name))";
        ArrayList<String> peoples = new ArrayList<>();
        PrintWriter pw = new PrintWriter(folder.newFile("studentInfo.csv"));
        pw.println("\"id\",\"name\",\"department\",\"email\"");
        if (client.login(USERNAME, PASSWORD)) {
            String userIndexPage=client.doGetText(userUrl);
            Document document = Jsoup.parse(userIndexPage);
            String userId = document.getElementById("userId").attr("value");
            String userCourses = client.doGetText(String.format(coursesUrl, userId));
            JSONObject courses = JSONObject.parseObject(userCourses);
            for (Object obj: courses.getJSONArray("courses")) {
                JSONObject course = (JSONObject) obj;
                String courseName = course.getString("name");
                int courseId = course.getIntValue("id");
                System.out.println(courseName+"\t"+courseId);
                String enrollments = client.doGetText(String.format(memberUrl, courseId));
                JSONArray members = JSONObject.parseObject(enrollments).getJSONArray("enrollments");
                for (Object userObj: members) {
                    JSONObject user = ((JSONObject) userObj).getJSONObject("user");
                    String studentId = user.getString("user_no");
                    if (peoples.contains(studentId)) {
                        continue;
                    }
                    peoples.add(studentId);
                    String name = user.getString("name");
                    String department = user.getJSONObject("department").getString("name");
                    String email = user.getString("email");
                    pw.println(String.format("\"%s\",\"%s\",\"%s\",\"%s\"", studentId, name, department, email));
                }
            }
        }
        pw.close();
    }
}