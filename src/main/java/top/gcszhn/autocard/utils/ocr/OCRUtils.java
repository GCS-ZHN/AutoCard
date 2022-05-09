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

/**
 * OCR识别抽象接口，具体实现根据不同OCR引擎
 * @author GCS-ZHN
 */
public interface OCRUtils {
    /**
     * OCR引擎的初始化
     * @param engineType 选择的引擎类型
     * @return 返回的OCR引擎实例
     * @throws Exception 初始化失败的异常
     */
    public static OCREngine instance(EngineType engineType) throws Exception {
        switch (engineType) {
            case TESSERACT_OCR: return new TesseractEngine();
            case D4_OCR: return new D4Engine();
            default: throw new Exception("不支持的OCR引擎");
        }
    }
}