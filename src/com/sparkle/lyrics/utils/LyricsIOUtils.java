package com.sparkle.lyrics.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.sparkle.lyrics.LyricsFileReader;
import com.sparkle.lyrics.LyricsFileWriter;
import com.sparkle.lyrics.formats.krc.KrcLyricsFileReader;
import com.sparkle.lyrics.formats.krc.KrcLyricsFileWriter;
import com.sparkle.lyrics.formats.ksc.KscLyricsFileReader;
import com.sparkle.lyrics.formats.ksc.KscLyricsFileWriter;
import com.sparkle.lyrics.formats.hrcx.HrcxLyricsFileReader;
import com.sparkle.lyrics.formats.hrcx.HrcxLyricsFileWriter;
import com.sparkle.lyrics.formats.hrcs.HrcsLyricsFileReader;
import com.sparkle.lyrics.formats.hrcs.HrcsLyricsFileWriter;
import com.sparkle.lyrics.formats.lrc.LrcLyricsFileReader;
import com.sparkle.lyrics.formats.lrc.LrcLyricsFileWriter;

/**
 * 歌词IO操作工具类
 * 
 * @author yuyi2003
 */
public class LyricsIOUtils {
    private static ArrayList<LyricsFileReader> readers;
    private static ArrayList<LyricsFileWriter> writers;

    static {
        readers = new ArrayList<LyricsFileReader>();
        readers.add(new KrcLyricsFileReader());
        readers.add(new KscLyricsFileReader());
        readers.add(new HrcxLyricsFileReader());
        readers.add(new HrcsLyricsFileReader());
        readers.add(new LrcLyricsFileReader());
        
        writers = new ArrayList<LyricsFileWriter>();
        writers.add(new KrcLyricsFileWriter());
        writers.add(new KscLyricsFileWriter());
        writers.add(new HrcxLyricsFileWriter());
        writers.add(new HrcsLyricsFileWriter());
        writers.add(new LrcLyricsFileWriter());
    }

    /**
     * 获取支持的歌词文件格式
     * 
     * @return 支持的文件扩展名列表
     */
    public static List<String> getSupportLyricsExts() {
        List<String> lrcExts = new ArrayList<String>();
        for (LyricsFileReader lyricsFileReader : readers) {
            lrcExts.add(lyricsFileReader.getSupportFileExt());
        }
        return lrcExts;
    }

    /**
     * 获取歌词文件读取器
     * 
     * @param file 歌词文件
     * @return 对应的读取器，如果不支持则返回null
     */
    public static LyricsFileReader getLyricsFileReader(File file) {
        return getLyricsFileReader(file.getName());
    }

    /**
     * 获取歌词文件读取器
     * 
     * @param fileName 文件名
     * @return 对应的读取器，如果不支持则返回null
     */
    public static LyricsFileReader getLyricsFileReader(String fileName) {
        String ext = FileUtils.getFileExt(fileName);
        for (LyricsFileReader lyricsFileReader : readers) {
            if (lyricsFileReader.isFileSupported(ext)) {
                return lyricsFileReader;
            }
        }
        return null;
    }

    /**
     * 获取歌词保存器
     * 
     * @param file 歌词文件
     * @return 对应的写入器，如果不支持则返回null
     */
    public static LyricsFileWriter getLyricsFileWriter(File file) {
        return getLyricsFileWriter(file.getName());
    }

    /**
     * 获取歌词保存器
     * 
     * @param fileName 文件名
     * @return 对应的写入器，如果不支持则返回null
     */
    public static LyricsFileWriter getLyricsFileWriter(String fileName) {
        String ext = FileUtils.getFileExt(fileName);
        for (LyricsFileWriter lyricsFileWriter : writers) {
            if (lyricsFileWriter.isFileSupported(ext)) {
                return lyricsFileWriter;
            }
        }
        return null;
    }
    
    /**
     * 判断是否支持该文件格式
     * 
     * @param fileName 文件名
     * @return true如果支持该格式
     */
    public static boolean isSupportedFile(String fileName) {
        return getLyricsFileReader(fileName) != null;
    }
    
    /**
     * 添加歌词读取器
     * 
     * @param reader 歌词读取器
     */
    public static void addLyricsFileReader(LyricsFileReader reader) {
        if (reader != null && !readers.contains(reader)) {
            readers.add(reader);
        }
    }
    
    /**
     * 添加歌词写入器
     * 
     * @param writer 歌词写入器
     */
    public static void addLyricsFileWriter(LyricsFileWriter writer) {
        if (writer != null && !writers.contains(writer)) {
            writers.add(writer);
        }
    }
}
