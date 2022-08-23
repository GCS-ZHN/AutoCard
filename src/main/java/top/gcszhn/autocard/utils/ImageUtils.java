/* 
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
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

    public static BufferedImage toGray(BufferedImage image) {
        BufferedImage target = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = target.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return target;
    }

    public static BufferedImage toBinary(BufferedImage image) {
        BufferedImage target = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = target.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return target;
    }

}
