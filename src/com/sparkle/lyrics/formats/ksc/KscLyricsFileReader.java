package com.sparkle.lyrics.formats.ksc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Base64;

import com.sparkle.lyrics.LyricsFileReader;
import com.sparkle.lyrics.model.LyricsInfo;
import com.sparkle.lyrics.model.LyricsLineInfo;
import com.sparkle.lyrics.model.LyricsTag;
import com.sparkle.lyrics.utils.CharUtils;
import com.sparkle.lyrics.utils.TimeUtils;

/**
 * KSC歌词解析器
 * 
 * @author yuyi2003
 */
public class KscLyricsFileReader extends LyricsFileReader {
    /**
     * 歌曲名 字符串
     */
    private final static String LEGAL_SONGNAME_PREFIX = "karaoke.songname";
    /**
     * 歌手名 字符串
     */
    private final static String LEGAL_SINGERNAME_PREFIX = "karaoke.singer";
    /**
     * 时间补偿值 字符串
     */
    private final static String LEGAL_OFFSET_PREFIX = "karaoke.offset";
    /**
     * 歌词 字符串
     */
    public final static String LEGAL_LYRICS_LINE_PREFIX = "karaoke.add";

    /**
     * 歌词Tag
     */
    private final static String LEGAL_TAG_PREFIX = "karaoke.tag";

    public KscLyricsFileReader() {
    }

    @Override
    public LyricsInfo readFile(File file) throws Exception {
        if (file != null && file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            LyricsInfo lyricsInfo = readInputStream(fis);
            fis.close();
            return lyricsInfo;
        }
        return null;
    }

    @Override
    public LyricsInfo readLrcText(String base64FileContentString, File saveLrcFile) throws Exception {
        byte[] data = Base64.decodeBase64(base64FileContentString);
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        LyricsInfo lyricsInfo = readInputStream(stream);
        
        if (saveLrcFile != null && lyricsInfo != null) {
            if (!saveLrcFile.getParentFile().exists()) {
                saveLrcFile.getParentFile().mkdirs();
            }
            FileOutputStream out = new FileOutputStream(saveLrcFile);
            out.write(data);
            out.close();
        }
        
        return lyricsInfo;
    }

    @Override
    public LyricsInfo readLrcText(byte[] base64ByteArray, File saveLrcFile) throws Exception {
        if (saveLrcFile != null) {
            // 生成歌词文件
            FileOutputStream os = new FileOutputStream(saveLrcFile);
            os.write(base64ByteArray);
            os.close();
        }

        return readInputStream(new ByteArrayInputStream(base64ByteArray));
    }

    @Override
    public LyricsInfo readInputStream(InputStream in) throws Exception {
        LyricsInfo lyricsInfo = new LyricsInfo();
        lyricsInfo.setLyricsFileExt(getSupportFileExt());
        
        if (in != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, getDefaultCharset()));
            String line;
            TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = new TreeMap<Integer, LyricsLineInfo>();
            Map<String, Object> lyricsTags = new HashMap<String, Object>();
            int index = 0;

            while ((line = reader.readLine()) != null) {
                try {
                    // 行读取，并解析每行歌词的内容
                    LyricsLineInfo lyricsLineInfo = parseLineInfos(lyricsTags, line, lyricsInfo);
                    if (lyricsLineInfo != null) {
                        lyricsLineInfos.put(index, lyricsLineInfo);
                        index++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            reader.close();

            // 设置歌词的标签类
            lyricsInfo.setLyricsTags(lyricsTags);
            lyricsInfo.setLyricsLineInfoTreeMap(lyricsLineInfos);
        }
        return lyricsInfo;
    }

    /**
     * 解析行信息
     */
    private LyricsLineInfo parseLineInfos(Map<String, Object> lyricsTags, String lineInfo, LyricsInfo lyricsInfo) throws Exception {
        if (lineInfo.startsWith(LEGAL_SONGNAME_PREFIX)) {
            String temp = lineInfo.substring(LEGAL_SONGNAME_PREFIX.length() + 2);
            temp = temp.substring(0, temp.length() - 3);
            lyricsTags.put(LyricsTag.TAG_TITLE, temp);
        } else if (lineInfo.startsWith(LEGAL_SINGERNAME_PREFIX)) {
            String temp = lineInfo.substring(LEGAL_SINGERNAME_PREFIX.length() + 2);
            temp = temp.substring(0, temp.length() - 3);
            lyricsTags.put(LyricsTag.TAG_ARTIST, temp);
        } else if (lineInfo.startsWith(LEGAL_OFFSET_PREFIX)) {
            String temp = lineInfo.substring(LEGAL_OFFSET_PREFIX.length() + 2);
            temp = temp.substring(0, temp.length() - 3);
            lyricsTags.put(LyricsTag.TAG_OFFSET, Integer.parseInt(temp));
        } else if (lineInfo.startsWith(LEGAL_TAG_PREFIX)) {
            String temp = lineInfo.substring(LEGAL_TAG_PREFIX.length() + 2);
            temp = temp.substring(0, temp.length() - 3);
            String[] keyValueArray = temp.split(":");
            if (keyValueArray.length == 2) {
                lyricsTags.put(keyValueArray[0], keyValueArray[1]);
            }
        } else if (lineInfo.startsWith(LEGAL_LYRICS_LINE_PREFIX)) {
            // Parse lyrics line
            String lyricsLineText = lineInfo.substring(lineInfo.indexOf("('") + 2, lineInfo.lastIndexOf("')"));
            String[] lyricsLineTextArray = lyricsLineText.split("','");
            
            if (lyricsLineTextArray.length >= 3) {
                String startTimeText = lyricsLineTextArray[0];
                String lyricsText = lyricsLineTextArray[1];
                String wordsDisIntervalText = lyricsLineTextArray[2];
                
                int startTime = TimeUtils.parseInteger(startTimeText);
                LyricsLineInfo lyricsLineInfo = new LyricsLineInfo();
                lyricsLineInfo.setStartTime(startTime);
                lyricsLineInfo.setLineLyrics(lyricsText);
                
                // Parse word intervals
                String[] wordsDisIntervalArray = wordsDisIntervalText.split(",");
                int[] wordsDisInterval = new int[wordsDisIntervalArray.length];
                for (int i = 0; i < wordsDisIntervalArray.length; i++) {
                    wordsDisInterval[i] = TimeUtils.parseInteger(wordsDisIntervalArray[i]);
                }
                lyricsLineInfo.setWordsDisInterval(wordsDisInterval);
                
                // Parse words
                List<String> lineLyricsList = getLyricsWords(lyricsText);
                String[] lyricsWords = lineLyricsList.toArray(new String[lineLyricsList.size()]);
                lyricsLineInfo.setLyricsWords(lyricsWords);
                
                // Calculate end time
                int endTime = startTime;
                for (int interval : wordsDisInterval) {
                    endTime += interval;
                }
                lyricsLineInfo.setEndTime(endTime);
                
                return lyricsLineInfo;
            }
        }
        return null;
    }

    /**
     * 分隔每个歌词
     * 
     * @param lineLyricsStr 歌词字符串
     * @return 歌词单词列表
     */
    private List<String> getLyricsWords(String lineLyricsStr) {
        List<String> lineLyricsList = new ArrayList<String>();
        String temp = "";
        boolean isEnter = false;
        for (int i = 0; i < lineLyricsStr.length(); i++) {
            char c = lineLyricsStr.charAt(i);
            if (CharUtils.isChinese(c) || CharUtils.isHangulSyllables(c)
                    || CharUtils.isHiragana(c)
                    || (!CharUtils.isWord(c) && c != '[' && c != ']')) {
                if (isEnter) {
                    temp += String.valueOf(lineLyricsStr.charAt(i));
                } else {
                    lineLyricsList.add(String.valueOf(lineLyricsStr.charAt(i)));
                }
            } else if (c == '[') {
                isEnter = true;
            } else if (c == ']') {
                isEnter = false;
                lineLyricsList.add(temp);
                temp = "";
            } else {
                temp += String.valueOf(lineLyricsStr.charAt(i));
            }
        }
        return lineLyricsList;
    }

    @Override
    public boolean isFileSupported(String ext) {
        return getSupportFileExt().equals(ext);
    }

    @Override
    public String getSupportFileExt() {
        return ".ksc";
    }
}
