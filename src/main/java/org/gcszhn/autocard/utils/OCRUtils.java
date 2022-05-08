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
package org.gcszhn.autocard.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.ImageHelper;

import javax.imageio.ImageIO;

/**
 * OCR utils
 * @author GCS-ZHN
 */
public class OCRUtils {
    /**
     * @param filename 图片名称
     * @return 识别的字符信息
     * @throws TesseractException tesseract-ocr识别异常
     * @throws IOException 图片读写等异常
     */
    public static String recognize(String filename) throws TesseractException, IOException {
        return recognize(new File(filename));
    }

    /**
     * @param file 图片文件对象
     * @return 识别的字符信息
     * @throws TesseractException tesseract-ocr识别异常
     * @throws IOException 图片读写等异常
     */
    public static String recognize(File file) throws TesseractException, IOException {
        return  recognize(ImageIO.read(file));
    }

    /**
     * @param image 图片对象
     * @return 识别的字符信息
     * @throws TesseractException tesseract-ocr识别异常
     */
    public static  String recognize(BufferedImage image) throws TesseractException {
        if (image == null) return null;
        ITesseract instance = new Tesseract();
        instance.setDatapath("./config/tessdata");
        instance.setLanguage("eng");
        image = ImageHelper.convertImageToGrayscale(image);
        image = ImageHelper.convertImageToBinary(image);
        return instance.doOCR(image);
    }
}
