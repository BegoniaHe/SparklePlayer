package com.sparkle.lyrics.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * 字符串解压和压缩工具类
 * 
 * @author yuyi2003
 */
public class StringCompressUtils {

    /**
     * 压缩
     * 
     * @param text 要压缩的文本
     * @param charset 字符编码
     * @return 压缩后的字节数组
     */
    public static byte[] compress(String text, Charset charset) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            OutputStream out = new DeflaterOutputStream(baos);
            out.write(text.getBytes(charset));
            out.close();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return baos.toByteArray();
    }

    /**
     * 解压
     * 
     * @param input 输入流
     * @param charset 字符编码
     * @return 解压后的字符串
     * @throws IOException IO异常
     */
    public static String decompress(InputStream input, Charset charset)
            throws IOException {
        return decompress(toByteArray(input), charset);
    }

    /**
     * 解压
     * 
     * @param bytes 压缩的字节数组
     * @param charset 字符编码
     * @return 解压后的字符串
     */
    public static String decompress(byte[] bytes, Charset charset) {
        InputStream in = new InflaterInputStream(
                new ByteArrayInputStream(bytes));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0)
                baos.write(buffer, 0, len);
            return new String(baos.toByteArray(), charset);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 将输入流转换为字节数组
     * 
     * @param input 输入流
     * @return 字节数组
     * @throws IOException IO异常
     */
    private static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    /**
     * 复制输入流到输出流
     * 
     * @param input 输入流
     * @param output 输出流
     * @return 复制的字节数
     * @throws IOException IO异常
     */
    private static int copy(InputStream input, OutputStream output)
            throws IOException {
        long count = copyLarge(input, output);
        if (count > 2147483647L) {
            return -1;
        }
        return (int) count;
    }

    /**
     * 复制大量数据
     * 
     * @param input 输入流
     * @param output 输出流
     * @return 复制的字节数
     * @throws IOException IO异常
     */
    private static long copyLarge(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[4096];
        long count = 0L;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
