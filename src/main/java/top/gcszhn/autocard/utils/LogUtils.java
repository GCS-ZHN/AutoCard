/*
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
 */
package top.gcszhn.autocard.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 应用日志类
 * @author Zhang.H.N
 * @version 1.0
 */
public class LogUtils {
    public enum Level {
        DEBUG, INFO, ERROR;
    }
    /**屏蔽构造函数 */
    private LogUtils(){};
    /**
     * 输出指定级别日志，指定信息源类名，并输出堆栈信息
     * @param message 信息内容
     * @param t 信息堆栈
     * @param level 信息级别
     * @param className 信息源类名
     */
    public static void printMessage(String message, Throwable t, Level level, String className) {
        Logger logger = LoggerFactory.getLogger(className);
        message = (t!=null&&message==null)?t.getMessage():message;
        switch(level) {
            case DEBUG:logger.debug(message, t);break;
            case INFO: logger.info(message, t);break;
            case ERROR:logger.error(message, t);break;
            default:logger.error("Unsupport log level");
        }
    }
    /**
     * 输出指定级别日志，指定信息源类名
     * @param message 信息内容
     * @param level 信息级别
     * @param className 信息源类名
     */
    public static void printMessage(String message, Level level, String className) {
        printMessage(message, null, level, className);
    }
    /**
     * 输出指定级别日志，并输出堆栈信息
     * @param message 信息内容
     * @param t 信息堆栈
     * @param level 信息级别
     */
    public static void printMessage(String message, Throwable t, Level level) {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        printMessage(message, t, level, stack[stack.length > 1?1:0].getClassName());
    }
    /**
     * 输出指定级别日志，信息源类指定为本方法的调用类
     * @param message 信息内容
     * @param level 信息级别
     */
    public static void printMessage(String message, Level level) {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        printMessage(message, null, level, stack[stack.length > 1?1:0].getClassName());
    } 
    /**
     * 输入INFO级别日志，信息源类指定为本方法的调用类
     * @param message 信息内容
     */
    public static void printMessage(String message) {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        printMessage(message, Level.INFO, stack[stack.length > 1?1:0].getClassName());
    }
}