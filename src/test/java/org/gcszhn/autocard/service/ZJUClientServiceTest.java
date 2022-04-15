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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Base64;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.gcszhn.autocard.AppTest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 浙大通行证客户端测试
 * @author Zhang.H.N
 * @version 1.0
 */
public class ZJUClientServiceTest extends AppTest {
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
        Assert.assertEquals(true, client.login(trueZjuPassPortUser, trueZjuPassPortPass));
        Assert.assertEquals(false, client.login("dadadada", "dadad"));
    }
    @Test
    public void getUserInfoTest() {
        if (client.login(trueZjuPassPortUser, trueZjuPassPortPass)) {
            System.out.println(client.getUserInfo());
        }
    }
    @Test
    public void getUserPhotoTest() {
        if (client.login(trueZjuPassPortUser, trueZjuPassPortPass)) {
            String photo = client.getUserPhoto();
            if (photo != null) {
                try (
                    FileOutputStream fileOutputStream = new FileOutputStream(trueZjuPassPortUser+".gif")) {
                    fileOutputStream.write(Base64.getDecoder().decode(photo));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Test
    public void loginCourseTest() throws FileNotFoundException {
        String userUrl="https://courses.zju.edu.cn/user/index";
        String coursesUrl="https://courses.zju.edu.cn/api/users/%s/courses";
        String memberUrl="https://courses.zju.edu.cn/api/course/%d/enrollments?fields=user(email,name,user_no,department(name))";
        ArrayList<String> peoples = new ArrayList<>();
        PrintWriter pw = new PrintWriter("studentInfo.csv");
        pw.println("\"id\",\"name\",\"department\",\"email\"");
        if (client.login(trueZjuPassPortUser, trueZjuPassPortPass)) {
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
                    //System.out.println(name+"\t"+studentId+"\t"+email+"\t"+department);
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