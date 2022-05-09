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
import java.util.Arrays;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession.Result;
import top.gcszhn.autocard.AppTest;
import top.gcszhn.autocard.utils.ONNXRuntimeUtils;

public class ONNXServiceTest extends AppTest {
    @Autowired
    ONNXRuntimeUtils service;
    @Test
    public void runTest() throws OrtException, IOException {
        long[] shape = {1, 1, 64, 64};
        float[] data = new float[(int)(shape[0] * shape[1] * shape[2] * shape[3])];
        Arrays.fill(data, 1);
        OnnxTensor dataTensor = service.createTensor(data, shape);
        Map<String, OnnxTensor> inputs = Map.of("input1", dataTensor);
        Result result = service.run("d4/common_old.onnx", inputs);
        OnnxTensor scoreTensor = (OnnxTensor) result.get(0);
        System.out.println("Tensor type: " + scoreTensor.getInfo().type.name());
        System.out.println("Tensor shape: " + Arrays.toString(scoreTensor.getInfo().getShape()));
        long[][] score = (long[][])scoreTensor.getValue();
        System.out.println(Arrays.toString(score[0]));
    }
}
