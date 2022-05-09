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

import ai.onnxruntime.OnnxTensor;
import com.alibaba.fastjson.JSONArray;
import top.gcszhn.autocard.AppConfig;
import top.gcszhn.autocard.utils.ONNXRuntimeUtils;
import top.gcszhn.autocard.utils.IOUtils;
import top.gcszhn.autocard.utils.ImageUtils;
import top.gcszhn.autocard.utils.LogUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

/**
 * DDDD-OCR引擎，基于开源项目dddd-ocr的训练模型
 * @author GCS-ZHN
 * @apiNote https://github.com/sml2h3/ddddocr/
 */
class D4Engine implements OCREngine {
    /**字符集 */
    private static JSONArray charsetArray;
    /**ONNX模型文件 */
    private static File modelFile;
    /**模型和字符的静态初始化 */
    static {
        try (InputStream is = D4Engine.class.getResourceAsStream("/d4/common_old_charset.json")) {
            charsetArray = JSONArray.parseArray(new String(is.readAllBytes(), "UTF-8"));
            modelFile = new File(AppConfig.TMP_DIR, "common_old.onnx");
            IOUtils.extractJarResource("/d4/common_old.onnx", modelFile);
        } catch (Exception e) {
            LogUtils.printMessage("DDDDOCR字符集加载异常", e, LogUtils.Level.ERROR);
        }
    }

    @Override
    public String recognize(BufferedImage image) {
        try (ONNXRuntimeUtils onnx = new ONNXRuntimeUtils()) {
            if (image == null) {
                LogUtils.printMessage("OCR输入图像不能为空", LogUtils.Level.ERROR);
                return null;
            }
            if (modelFile == null || !modelFile.exists() || charsetArray == null) {
                LogUtils.printMessage("OCR模型配置缺失", LogUtils.Level.ERROR);
                return null;
            }
            if (onnx == null) {
                LogUtils.printMessage("onnx运行时服务未启动", LogUtils.Level.ERROR);
            }

            // 预处理图像
            image = ImageUtils.resize(image, 64 * image.getWidth() / image.getHeight(), 64);
            image = ImageUtils.toGray(image);
            long[] shape = {1, 1, image.getHeight(), image.getWidth()};
            float[] data = new float[(int)(shape[0] * shape[1] * shape[2] * shape[3])];
            image.getData().getPixels(0, 0, image.getWidth(), image.getHeight(), data);
            for (int i = 0; i < data.length; i++) {
                data[i] /= 255;
                data[i] = (float) ((data[i] - 0.5) / 0.5);
            }

            // 输出字符索引
            OnnxTensor indexTensor = (OnnxTensor) onnx.run(
                    modelFile.getAbsolutePath(),
                    Map.of("input1",  onnx.createTensor(data, shape))).get(0);
            LogUtils.printMessage("score type: " + indexTensor.getInfo().type.name(), LogUtils.Level.DEBUG);
            LogUtils.printMessage("score shape: " + Arrays.toString(indexTensor.getInfo().getShape()), LogUtils.Level.DEBUG);
            long[][] index = (long[][])indexTensor.getValue();

            StringBuilder words = new StringBuilder();
            for (long i: index[0]) {
                words.append((String) charsetArray.get((int) i));
            }
            return words.toString();
        } catch (Exception e) {
            LogUtils.printMessage("OCR识别异常", e, LogUtils.Level.ERROR);
            return null;
        }
    }
}
