package com.sparkle.lyrics.utils;

import java.io.File;
import java.text.DecimalFormat;

/**
 * 文件工具类
 * 
 * @author yuyi2003
 */
public class FileUtils {
    
    /**
     * 获取文件扩展名
     * 
     * @param file 文件对象
     * @return 文件扩展名
     */
    public static String getFileExt(File file) {
        return getFileExt(file.getName());
    }

    /**
     * 移除文件扩展名
     * 
     * @param s 文件名
     * @return 去除扩展名后的文件名
     */
    public static String removeExt(String s) {
        int index = s.lastIndexOf(".");
        if (index == -1) {
            index = s.length();
        }
        return s.substring(0, index);
    }

    /**
     * 获取文件扩展名
     * 
     * @param fileName 文件名
     * @return 文件扩展名
     */
    public static String getFileExt(String fileName) {
        int pos = fileName.lastIndexOf(".");
        if (pos == -1) {
            return "";
        }
        return fileName.substring(pos + 1).toLowerCase();
    }

    /**
     * 计算文件的大小，返回相关的大小字符串
     * 
     * @param fileS 文件大小（字节）
     * @return 格式化后的文件大小字符串
     */
    public static String getFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }
    
    /**
     * 检查文件是否存在
     * 
     * @param filePath 文件路径
     * @return true如果文件存在
     */
    public static boolean fileExists(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }
    
    /**
     * 获取文件名（不包含路径）
     * 
     * @param filePath 文件路径
     * @return 文件名
     */
    public static String getFileName(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return "";
        }
        File file = new File(filePath);
        return file.getName();
    }
}
