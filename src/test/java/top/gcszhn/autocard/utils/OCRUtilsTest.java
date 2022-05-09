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
package top.gcszhn.autocard.utils;

import java.io.File;

import javax.imageio.ImageIO;

import org.junit.Test;

import top.gcszhn.autocard.AppTest;
import top.gcszhn.autocard.utils.ocr.EngineType;
import top.gcszhn.autocard.utils.ocr.OCRUtils;
public class OCRUtilsTest extends AppTest {
    @Test
    public void recongnizeTest() {
        try {
            System.out.println(OCRUtils.instance(EngineType.D4_OCR).recognize(ImageIO.read(new File("code/AENZ.png"))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
