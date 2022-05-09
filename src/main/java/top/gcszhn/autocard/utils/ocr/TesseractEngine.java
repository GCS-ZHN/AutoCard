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
package top.gcszhn.autocard.utils.ocr;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import top.gcszhn.autocard.App;
import top.gcszhn.autocard.AppConfig;
import top.gcszhn.autocard.utils.IOUtils;
import top.gcszhn.autocard.utils.ImageUtils;
import top.gcszhn.autocard.utils.LogUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 基于tesseract-ocr引擎，使用tess4j进行OCR识别，需要安装tessseract-ocr动态库
 * libtesseract。
 */
class TesseractEngine implements OCREngine {
    /** 提取静态模型资源 */
    static {
        try {
            IOUtils.extractJarResource("/tesseract/eng.traineddata", new File(AppConfig.TMP_DIR, "eng.traineddata"));
        } catch (IOException e) {
            LogUtils.printMessage("tesseract数据提取异常", LogUtils.Level.ERROR);
        }
    }

    /**tesseract实例 */
    private ITesseract engine;
    /**初始化引擎 */
    public TesseractEngine() {
        engine = new Tesseract();
        engine.setDatapath(AppConfig.TMP_DIR.getAbsolutePath());
        engine.setLanguage("eng");
    }

    @Override
    public String recognize(BufferedImage image)  {
        try {
            if (image == null || engine == null) return null;
            image = ImageUtils.toGray(image);
            image = ImageUtils.toBinary(image);
            return engine.doOCR(image);
        } catch (Exception e) {
            LogUtils.printMessage("OCR识别异常", e, LogUtils.Level.ERROR);
            return null;
        }
    }
}
