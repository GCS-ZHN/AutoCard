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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import java.awt.Graphics2D;
import java.awt.Image;

public class ImageUtils {
    public static BufferedImage resize(BufferedImage image, int width, int height) {
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Image tmp = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        Graphics2D g2d = newImage.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return newImage;
    }

    public static String toBase64(BufferedImage image, String formatName) {
        try {
            return Base64.getEncoder().encodeToString(toByteArray(image, formatName));
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
        return null;
    }

    public static byte[] toByteArray(BufferedImage image, String formatName) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, formatName , outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
        return null;
    }

    public static BufferedImage toImage(String base64) {
        try  {
            return toImage(Base64.getDecoder().decode(base64));
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
        return null;
    }

    public static BufferedImage toImage(byte[] byteArray) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray)) {
            return ImageIO.read(inputStream);
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
        return null;
    }

    public static void write(BufferedImage image, String formatName, File file) {
        try {
            ImageIO.write(image, formatName, file);
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
    }

    public static void write(byte[] byteArray, File file) {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(byteArray);
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
    }

    public static void write(String base64, File file) {
        try {
            write(Base64.getDecoder().decode(base64), file);
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
    }
}
