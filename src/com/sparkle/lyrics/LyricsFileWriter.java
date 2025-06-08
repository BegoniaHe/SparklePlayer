package com.sparkle.lyrics;

import java.nio.charset.Charset;

import com.sparkle.lyrics.model.LyricsInfo;

/**
 * 歌词文件写入器抽象类
 * 
 * @author yuyi2003
 */
public abstract class LyricsFileWriter {
    /**
     * 默认编码
     */
    protected Charset defaultCharset = Charset.forName("utf-8");

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
     * 保存歌词文件
     * 
     * @param lyricsInfo 歌词数据
     * @param lyricsFilePath 歌词文件路径
     * @return true如果保存成功
     * @throws Exception 保存异常
     */
    public abstract boolean writer(LyricsInfo lyricsInfo, String lyricsFilePath) throws Exception;

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
