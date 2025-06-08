package com.sparkle.lyrics;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.sparkle.lyrics.model.LyricsInfo;

/**
 * 歌词文件读取器抽象类
 * 
 * @author yuyi2003
 */
public abstract class LyricsFileReader {
    /**
     * 默认编码
     */
    protected Charset defaultCharset = Charset.forName("utf-8");

    /**
     * 读取歌词文件
     * 
     * @param file 歌词文件
     * @return 歌词信息对象
     * @throws Exception 读取异常
     */
    public abstract LyricsInfo readFile(File file) throws Exception;
    
    /**
     * 读取歌词文本内容
     * 
     * @param base64FileContentString base64位文件内容
     * @param saveLrcFile 要保存的歌词文件
     * @return 歌词信息对象
     * @throws Exception 读取异常
     */
    public abstract LyricsInfo readLrcText(String base64FileContentString, File saveLrcFile) throws Exception;
    
    /**
     * 读取歌词文本内容
     * 
     * @param base64ByteArray base64内容数组
     * @param saveLrcFile 要保存的歌词文件
     * @return 歌词信息对象
     * @throws Exception 读取异常
     */
    public abstract LyricsInfo readLrcText(byte[] base64ByteArray, File saveLrcFile) throws Exception;
    
    /**
     * 读取歌词文件流
     * 
     * @param in 输入流
     * @return 歌词信息对象
     * @throws Exception 读取异常
     */
    public abstract LyricsInfo readInputStream(InputStream in) throws Exception;

    /**
     * 是否支持该文件格式
     * 
     * @param ext 文件后缀名
     * @return true如果支持该格式
     */
    public abstract boolean isFileSupported(String ext);

    /**
     * 获取支持的文件后缀名
     * 
     * @return 文件后缀名
     */
    public abstract String getSupportFileExt();

    /**
     * 设置默认字符编码
     * 
     * @param charset 字符编码
     */
    public void setDefaultCharset(Charset charset) {
        defaultCharset = charset;
    }

    /**
     * 获取默认字符编码
     * 
     * @return 字符编码
     */
    public Charset getDefaultCharset() {
        return defaultCharset;
    }
}
