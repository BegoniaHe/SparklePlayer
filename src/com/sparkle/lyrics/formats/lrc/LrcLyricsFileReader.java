package com.sparkle.lyrics.formats.lrc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

import com.sparkle.lyrics.LyricsFileReader;
import com.sparkle.lyrics.model.LyricsInfo;
import com.sparkle.lyrics.model.LyricsLineInfo;
import com.sparkle.lyrics.model.LyricsTag;
import com.sparkle.lyrics.utils.CharUtils;

/**
 * LRC歌词读取器
 * 
 * @author yuyi2003
 */
public class LrcLyricsFileReader extends LyricsFileReader {
    
    /**
     * 歌曲名 字符串
     */
    private static final String LEGAL_TITLE_PREFIX = "[ti:";
    
    /**
     * 歌手名 字符串
     */
    private static final String LEGAL_ARTIST_PREFIX = "[ar:";
    
    /**
     * 专辑名 字符串
     */
    private static final String LEGAL_ALBUM_PREFIX = "[al:";
    
    /**
     * 时间补偿值 字符串
     */
    private static final String LEGAL_OFFSET_PREFIX = "[offset:";
    
    /**
     * 歌词上传者
     */
    private static final String LEGAL_BY_PREFIX = "[by:";
    
    /**
     * 右方括号
     */
    private static final String RIGHT_BRACKET = "]";
    
    /**
     * 时间戳正则表达式 [mm:ss.xx] 或 [mm:ss]
     */
    private static final Pattern TIME_PATTERN = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:\\.(\\d{2,3}))?\\]");

    public LrcLyricsFileReader() {
    }

    @Override
    public LyricsInfo readFile(File file) throws Exception {
        if (file == null || !file.exists()) {
            return null;
        }
        
        FileInputStream fis = new FileInputStream(file);
        LyricsInfo lyricsInfo = readInputStream(fis);
        fis.close();
        return lyricsInfo;
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
            java.io.FileOutputStream out = new java.io.FileOutputStream(saveLrcFile);
            out.write(data);
            out.close();
        }
        
        return lyricsInfo;
    }

    @Override
    public LyricsInfo readLrcText(byte[] base64ByteArray, File saveLrcFile) throws Exception {
        ByteArrayInputStream stream = new ByteArrayInputStream(base64ByteArray);
        LyricsInfo lyricsInfo = readInputStream(stream);
        
        if (saveLrcFile != null && lyricsInfo != null) {
            if (!saveLrcFile.getParentFile().exists()) {
                saveLrcFile.getParentFile().mkdirs();
            }
            java.io.FileOutputStream out = new java.io.FileOutputStream(saveLrcFile);
            out.write(base64ByteArray);
            out.close();
        }
        
        return lyricsInfo;
    }

    @Override
    public LyricsInfo readInputStream(InputStream in) throws Exception {
        LyricsInfo lyricsInfo = new LyricsInfo();
        lyricsInfo.setLyricsFileExt(getSupportFileExt());
        
        if (in == null) {
            return lyricsInfo;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(in, getDefaultCharset()));
        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = new TreeMap<Integer, LyricsLineInfo>();
        Map<String, Object> lyricsTags = new HashMap<String, Object>();
        
        String line;
        int index = 0;
        
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            
            try {
                // 解析标签信息
                if (line.startsWith(LEGAL_TITLE_PREFIX)) {
                    String title = line.substring(LEGAL_TITLE_PREFIX.length(), 
                            line.lastIndexOf(RIGHT_BRACKET));
                    lyricsTags.put(LyricsTag.TAG_TITLE, title);
                } else if (line.startsWith(LEGAL_ARTIST_PREFIX)) {
                    String artist = line.substring(LEGAL_ARTIST_PREFIX.length(), 
                            line.lastIndexOf(RIGHT_BRACKET));
                    lyricsTags.put(LyricsTag.TAG_ARTIST, artist);
                } else if (line.startsWith(LEGAL_ALBUM_PREFIX)) {
                    String album = line.substring(LEGAL_ALBUM_PREFIX.length(), 
                            line.lastIndexOf(RIGHT_BRACKET));
                    lyricsTags.put("lyrics.tag.album", album);
                } else if (line.startsWith(LEGAL_OFFSET_PREFIX)) {
                    String offset = line.substring(LEGAL_OFFSET_PREFIX.length(), 
                            line.lastIndexOf(RIGHT_BRACKET));
                    lyricsTags.put(LyricsTag.TAG_OFFSET, offset);
                } else if (line.startsWith(LEGAL_BY_PREFIX)) {
                    String by = line.substring(LEGAL_BY_PREFIX.length(), 
                            line.lastIndexOf(RIGHT_BRACKET));
                    lyricsTags.put(LyricsTag.TAG_BY, by);
                } else {
                    // 解析歌词行
                    LyricsLineInfo lyricsLineInfo = parseLyricsLine(line);
                    if (lyricsLineInfo != null) {
                        lyricsLineInfos.put(index, lyricsLineInfo);
                        index++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        reader.close();
        
        optimizeWordsInterval(lyricsLineInfos);
        
        // 设置歌词的标签和行信息
        lyricsInfo.setLyricsTags(lyricsTags);
        lyricsInfo.setLyricsLineInfoTreeMap(lyricsLineInfos);
        
        return lyricsInfo;
    }
    
    /**
     * 优化单词时间间隔，根据相邻歌词行的时间差动态分配
     * 
     * @param lyricsLineInfos 歌词行信息集合
     */
    private void optimizeWordsInterval(TreeMap<Integer, LyricsLineInfo> lyricsLineInfos) {
        if (lyricsLineInfos == null || lyricsLineInfos.isEmpty()) {
            return;
        }
        
        Integer[] keys = lyricsLineInfos.keySet().toArray(new Integer[0]);
        
        for (int i = 0; i < keys.length; i++) {
            LyricsLineInfo currentLine = lyricsLineInfos.get(keys[i]);
            if (currentLine == null || currentLine.getLyricsWords().length == 0) {
                continue;
            }
            
            int startTime = currentLine.getStartTime();
            int availableTime;
            
            // 计算可用时间：到下一行歌词的时间差，或使用默认值
            if (i < keys.length - 1) {
                LyricsLineInfo nextLine = lyricsLineInfos.get(keys[i + 1]);
                availableTime = nextLine.getStartTime() - startTime;
            } else {
                // 最后一行，使用基于单词数量的估算
                availableTime = currentLine.getLyricsWords().length * 200; // 每个单词200ms
            }
            
            // 确保最小可用时间
            availableTime = Math.max(availableTime, 500);
            
            String[] words = currentLine.getLyricsWords();
            int[] wordsDisInterval = new int[words.length];
            
            if (words.length == 1) {
                // 只有一个单词，使用全部可用时间
                wordsDisInterval[0] = availableTime;
            } else {
                // 多个单词，根据单词类型分配时间
                int totalWeight = 0;
                int[] wordWeights = new int[words.length];
                
                // 计算每个单词的权重
                for (int j = 0; j < words.length; j++) {
                    String word = words[j];
                    int weight;
                    
                    if (word.length() == 1) {
                        char c = word.charAt(0);
                        if (CharUtils.isChinese(c) || CharUtils.isHangulSyllables(c) || CharUtils.isHiragana(c)) {
                            // 中文、韩文、日文单字符，权重较小
                            weight = 1;
                        } else if (Character.isLetter(c)) {
                            // 英文单字符，权重很小
                            weight = 1;
                        } else {
                            // 标点符号等，权重最小
                            weight = 1;
                        }
                    } else {
                        // 多字符单词，权重基于长度
                        weight = Math.max(2, word.length());
                    }
                    
                    wordWeights[j] = weight;
                    totalWeight += weight;
                }
                
                // 分配时间，保留10%作为缓冲
                int distributionTime = (int)(availableTime * 0.9);
                int remainingTime = availableTime - distributionTime;
                
                for (int j = 0; j < words.length; j++) {
                    int allocatedTime = (distributionTime * wordWeights[j]) / totalWeight;
                    
                    // 确保最小时间间隔
                    allocatedTime = Math.max(allocatedTime, 50);
                    
                    // 最后一个单词获得剩余时间
                    if (j == words.length - 1) {
                        allocatedTime += remainingTime;
                    }
                    
                    wordsDisInterval[j] = allocatedTime;
                }
            }
            
            // 更新单词间隔和结束时间
            currentLine.setWordsDisInterval(wordsDisInterval);
            
            // 计算实际结束时间
            int totalInterval = 0;
            for (int interval : wordsDisInterval) {
                totalInterval += interval;
            }
            currentLine.setEndTime(startTime + totalInterval);
        }
    }
    
    /**
     * 解析歌词行
     * 
     * @param line 歌词行文本
     * @return 歌词行信息对象
     */
    private LyricsLineInfo parseLyricsLine(String line) {
        Matcher matcher = TIME_PATTERN.matcher(line);
        
        if (matcher.find()) {
            try {
                // 解析时间
                int minutes = Integer.parseInt(matcher.group(1));
                int seconds = Integer.parseInt(matcher.group(2));
                String millisecondsStr = matcher.group(3);
                int milliseconds = 0;
                
                if (millisecondsStr != null) {
                    // 处理毫秒部分，可能是2位或3位
                    if (millisecondsStr.length() == 2) {
                        milliseconds = Integer.parseInt(millisecondsStr) * 10;
                    } else if (millisecondsStr.length() == 3) {
                        milliseconds = Integer.parseInt(millisecondsStr);
                    }
                }
                
                int startTime = (minutes * 60 + seconds) * 1000 + milliseconds;
                
                // 获取歌词文本
                String lyricsText = line.substring(matcher.end()).trim();
                
                LyricsLineInfo lyricsLineInfo = new LyricsLineInfo();
                lyricsLineInfo.setStartTime(startTime);
                lyricsLineInfo.setLineLyrics(lyricsText);
                
                // 分割歌词为单词
                String[] lyricsWords = getLyricsWords(lyricsText);
                lyricsLineInfo.setLyricsWords(lyricsWords);
                
                // 先设置临时的间隔，后续会在optimizeWordsInterval中优化
                int[] wordsDisInterval = new int[lyricsWords.length];
                if (lyricsWords.length > 0) {
                    int averageInterval = 300; // 临时设置为300ms，将在后面优化
                    for (int i = 0; i < wordsDisInterval.length; i++) {
                        wordsDisInterval[i] = averageInterval;
                    }
                }
                lyricsLineInfo.setWordsDisInterval(wordsDisInterval);
                
                // 临时设置结束时间，将在后面优化
                lyricsLineInfo.setEndTime(startTime + lyricsWords.length * 300);
                
                return lyricsLineInfo;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }
    
    /**
     * 分割歌词为单词
     * 
     * @param lyricsText 歌词文本
     * @return 歌词单词数组
     */
    private String[] getLyricsWords(String lyricsText) {
        if (lyricsText == null || lyricsText.trim().isEmpty()) {
            return new String[]{""};
        }
        
        java.util.List<String> wordsList = new java.util.ArrayList<String>();
        StringBuilder temp = new StringBuilder();
        
        for (int i = 0; i < lyricsText.length(); i++) {
            char c = lyricsText.charAt(i);
            
            if (CharUtils.isChinese(c) || CharUtils.isHangulSyllables(c) || CharUtils.isHiragana(c)) {
                // 中文、韩文、日文字符，每个字符作为一个单词
                if (temp.length() > 0) {
                    wordsList.add(temp.toString().trim());
                    temp.setLength(0);
                }
                wordsList.add(String.valueOf(c));
            } else if (Character.isSpaceChar(c)) {
                // 空格分隔
                if (temp.length() > 0) {
                    wordsList.add(temp.toString().trim());
                    temp.setLength(0);
                }
            } else {
                // 其他字符累积
                temp.append(c);
            }
        }
        
        // 添加最后的累积内容
        if (temp.length() > 0) {
            wordsList.add(temp.toString().trim());
        }
        
        // 如果没有任何单词，至少返回原始文本
        if (wordsList.isEmpty()) {
            wordsList.add(lyricsText);
        }
        
        return wordsList.toArray(new String[wordsList.size()]);
    }

    @Override
    public boolean isFileSupported(String ext) {
        return "lrc".equalsIgnoreCase(ext);
    }

    @Override
    public String getSupportFileExt() {
        return "lrc";  // Return without dot to match FileUtils.getFileExt() format
    }
}
