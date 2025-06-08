package com.sparkle.lyrics.formats.hrcs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;

import com.sparkle.lyrics.LyricsFileReader;
import com.sparkle.lyrics.model.LyricsInfo;
import com.sparkle.lyrics.model.LyricsLineInfo;
import com.sparkle.lyrics.model.LyricsTag;
import com.sparkle.lyrics.model.TranslateLrcLineInfo;
import com.sparkle.lyrics.model.TranslateLyricsInfo;
import com.sparkle.lyrics.model.TransliterationLyricsInfo;
import com.sparkle.lyrics.utils.StringCompressUtils;

/**
 * HRCS歌词解析器.
 * 
 * @author yuyi2003
 */
public class HrcsLyricsFileReader extends LyricsFileReader {
    
    /** 歌词 字符串. */
    public static final String LEGAL_LYRICS_LINE_PREFIX = "haplayer.lrc";
    
    /** 歌曲名 字符串. */
    private static final String LEGAL_TITLE_PREFIX = "[ti:";
    
    /** 歌手名 字符串. */
    private static final String LEGAL_ARTIST_PREFIX = "[ar:";
    
    /** 时间补偿值 字符串. */
    private static final String LEGAL_OFFSET_PREFIX = "[offset:";
    
    /** 歌曲长度. */
    private static final String LEGAL_TOTAL_PREFIX = "[total:";
    
    /** 上传者. */
    private static final String LEGAL_BY_PREFIX = "[by:";
    
    /** Tag标签. */
    private static final String LEGAL_TAG_PREFIX = "haplayer.tag[";

    /** 额外歌词. */
    private static final String LEGAL_EXTRA_LYRICS_PREFIX = "haplayer.extra.lrc";
    
    /** 分隔符字符串. */
    private static final String SEPARATOR_STRING = "','";
    
    /** 左括号字符串. */
    private static final String LEFT_BRACKET_STRING = "('";
    
    /** 右括号字符串. */
    private static final String RIGHT_BRACKET_STRING = "')";
    
    /** 左尖括号字符串. */
    private static final String LEFT_ANGLE_STRING = "<";
    
    /** 右尖括号字符串. */
    private static final String RIGHT_ANGLE_STRING = ">";
    
    /** 逗号字符串. */
    private static final String COMMA_STRING = ",";
    
    /** 空格字符串. */
    private static final String SPACE_STRING = " ";
    
    /** 冒号字符串. */
    private static final String COLON_STRING = ":";
    
    /** 换行符字符串. */
    private static final String NEWLINE_STRING = "\n";
    
    /** 内容字段名. */
    private static final String CONTENT_FIELD = "content";
    
    /** 歌词类型字段名. */
    private static final String LYRIC_TYPE_FIELD = "lyricType";
    
    /** 歌词内容字段名. */
    private static final String LYRIC_CONTENT_FIELD = "lyricContent";
    
    /** 翻译歌词类型. */
    private static final int TRANSLATE_LYRICS_TYPE = 1;
    
    /** 音译歌词类型. */
    private static final int TRANSLITERATION_LYRICS_TYPE = 0;
    
    /** 右方括号字符串. */
    private static final String RIGHT_SQUARE_BRACKET = "]";
    
    /** 最小解析部分数量. */
    private static final int MIN_PARSE_PARTS = 3;

    /**
     * 默认构造函数.
     */
    public HrcsLyricsFileReader() {
    }

    /**
     * 读取歌词文件.
     *
     * @param file 歌词文件
     * @return 歌词信息对象
     * @throws Exception 读取异常
     */
    @Override
    public LyricsInfo readFile(final File file) throws Exception {
        if (file != null && file.exists()) {
            final FileInputStream fis = new FileInputStream(file);
            final LyricsInfo lyricsInfo = readInputStream(fis);
            fis.close();
            return lyricsInfo;
        }
        return null;
    }

    /**
     * 读取Base64编码的歌词文本.
     *
     * @param base64FileContentString Base64编码的歌词内容
     * @param saveLrcFile 保存的歌词文件
     * @return 歌词信息对象
     * @throws Exception 读取异常
     */
    @Override
    public LyricsInfo readLrcText(final String base64FileContentString, final File saveLrcFile) throws Exception {
        final byte[] data = Base64.decodeBase64(base64FileContentString);
        final ByteArrayInputStream stream = new ByteArrayInputStream(data);
        final LyricsInfo lyricsInfo = readInputStream(stream);
        
        if (saveLrcFile != null && lyricsInfo != null) {
            if (!saveLrcFile.getParentFile().exists()) {
                saveLrcFile.getParentFile().mkdirs();
            }
            final FileOutputStream out = new FileOutputStream(saveLrcFile);
            out.write(data);
            out.close();
        }
        
        return lyricsInfo;
    }

    /**
     * 读取字节数组的歌词内容.
     *
     * @param base64ByteArray Base64字节数组
     * @param saveLrcFile 保存的歌词文件
     * @return 歌词信息对象
     * @throws Exception 读取异常
     */
    @Override
    public LyricsInfo readLrcText(final byte[] base64ByteArray, final File saveLrcFile) throws Exception {
        if (saveLrcFile != null) {
            // 生成歌词文件
            final FileOutputStream os = new FileOutputStream(saveLrcFile);
            os.write(base64ByteArray);
            os.close();
        }

        return readInputStream(new ByteArrayInputStream(base64ByteArray));
    }

    /**
     * 读取输入流的歌词内容.
     *
     * @param in 输入流
     * @return 歌词信息对象
     * @throws Exception 读取异常
     */
    @Override
    public LyricsInfo readInputStream(final InputStream in) throws Exception {
        final LyricsInfo lyricsInfo = new LyricsInfo();
        lyricsInfo.setLyricsFileExt(getSupportFileExt());
        
        if (in != null) {
            final byte[] buffer = new byte[in.available()];
            in.read(buffer);
            in.close();
            
            final String content = StringCompressUtils.decompress(buffer, getDefaultCharset());
            final String[] lines = content.split(NEWLINE_STRING);
            
            final TreeMap<Integer, LyricsLineInfo> lyricsLineInfosTemp = new TreeMap<Integer, LyricsLineInfo>();
            final TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = new TreeMap<Integer, LyricsLineInfo>();
            final Map<String, Object> lyricsTags = new HashMap<String, Object>();

            for (final String line : lines) {
                try {
                    if (line.startsWith(LEGAL_TITLE_PREFIX)) {
                        final String title = line.substring(LEGAL_TITLE_PREFIX.length(), 
                                line.lastIndexOf(RIGHT_SQUARE_BRACKET));
                        lyricsTags.put(LyricsTag.TAG_TITLE, title);
                    } else if (line.startsWith(LEGAL_ARTIST_PREFIX)) {
                        final String artist = line.substring(LEGAL_ARTIST_PREFIX.length(), 
                                line.lastIndexOf(RIGHT_SQUARE_BRACKET));
                        lyricsTags.put(LyricsTag.TAG_ARTIST, artist);
                    } else if (line.startsWith(LEGAL_OFFSET_PREFIX)) {
                        final String offset = line.substring(LEGAL_OFFSET_PREFIX.length(), 
                                line.lastIndexOf(RIGHT_SQUARE_BRACKET));
                        lyricsTags.put(LyricsTag.TAG_OFFSET, Integer.parseInt(offset));
                    } else if (line.startsWith(LEGAL_BY_PREFIX)) {
                        final String by = line.substring(LEGAL_BY_PREFIX.length(), 
                                line.lastIndexOf(RIGHT_SQUARE_BRACKET));
                        lyricsTags.put(LyricsTag.TAG_BY, by);
                    } else if (line.startsWith(LEGAL_TOTAL_PREFIX)) {
                        final String total = line.substring(LEGAL_TOTAL_PREFIX.length(), 
                                line.lastIndexOf(RIGHT_SQUARE_BRACKET));
                        lyricsTags.put(LyricsTag.TAG_TOTAL, Integer.parseInt(total));
                    } else {
                        processSpecialLines(line, lyricsTags, lyricsInfo, lyricsLineInfosTemp);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }

            // Sort by start time
            sortLyricsLines(lyricsLineInfosTemp, lyricsLineInfos);

            // 设置歌词的标签类
            lyricsInfo.setLyricsTags(lyricsTags);
            lyricsInfo.setLyricsLineInfoTreeMap(lyricsLineInfos);
        }
        return lyricsInfo;
    }
    
    /**
     * 处理特殊的歌词行.
     *
     * @param line 歌词行
     * @param lyricsTags 歌词标签映射
     * @param lyricsInfo 歌词信息对象
     * @param lyricsLineInfosTemp 临时歌词行信息映射
     * @throws Exception 处理异常
     */
    private void processSpecialLines(final String line, final Map<String, Object> lyricsTags, 
            final LyricsInfo lyricsInfo, 
            final TreeMap<Integer, LyricsLineInfo> lyricsLineInfosTemp) throws Exception {
        if (line.startsWith(LEGAL_TAG_PREFIX)) {
            // Parse custom tags
            parseCustomTags(line, lyricsTags);
        } else if (line.startsWith(LEGAL_EXTRA_LYRICS_PREFIX)) {
            // Parse extra lyrics (translations/transliterations)
            parseExtraLyrics(line, lyricsInfo);
        } else if (line.startsWith(LEGAL_LYRICS_LINE_PREFIX)) {
            // Parse lyrics line
            final LyricsLineInfo lyricsLineInfo = parseLyricsLine(line);
            if (lyricsLineInfo != null) {
                lyricsLineInfosTemp.put(lyricsLineInfo.getStartTime(), lyricsLineInfo);
            }
        }
    }
    
    /**
     * 排序歌词行.
     *
     * @param lyricsLineInfosTemp 临时歌词行信息映射
     * @param lyricsLineInfos 最终歌词行信息映射
     */
    private void sortLyricsLines(final TreeMap<Integer, LyricsLineInfo> lyricsLineInfosTemp, 
            final TreeMap<Integer, LyricsLineInfo> lyricsLineInfos) {
        int index = 0;
        final Iterator<Integer> it = lyricsLineInfosTemp.keySet().iterator();
        while (it.hasNext()) {
            lyricsLineInfos.put(index++, lyricsLineInfosTemp.get(it.next()));
        }
    }
    
    /**
     * 解析自定义标签.
     *
     * @param line 歌词行
     * @param lyricsTags 歌词标签映射
     */
    private void parseCustomTags(final String line, final Map<String, Object> lyricsTags) {
        final String tagContent = line.substring(LEGAL_TAG_PREFIX.length(), 
                line.lastIndexOf(RIGHT_SQUARE_BRACKET));
        if (tagContent.contains(COLON_STRING)) {
            final String[] parts = tagContent.split(COLON_STRING, 2);
            lyricsTags.put(parts[0], parts[1]);
        }
    }

    /**
     * 解析额外歌词信息.
     *
     * @param line 歌词行
     * @param lyricsInfo 歌词信息对象
     * @throws Exception 解析异常
     */
    private void parseExtraLyrics(final String line, final LyricsInfo lyricsInfo) throws Exception {
        final String base64Content = line.substring(line.indexOf(LEFT_BRACKET_STRING) + 2, 
                line.lastIndexOf(RIGHT_BRACKET_STRING));
        final String jsonContent = new String(Base64.decodeBase64(base64Content));
        
        try {
            final JSONObject extraObj = JSONObject.fromObject(jsonContent);
            final JSONArray contentArray = extraObj.getJSONArray(CONTENT_FIELD);
            
            for (int i = 0; i < contentArray.size(); i++) {
                final JSONObject lyricsObj = contentArray.getJSONObject(i);
                final int lyricType = lyricsObj.getInt(LYRIC_TYPE_FIELD);
                final JSONArray lyricContentArray = lyricsObj.getJSONArray(LYRIC_CONTENT_FIELD);
                
                if (lyricType == TRANSLATE_LYRICS_TYPE) {
                    // Translation lyrics
                    parseTranslateLyrics(lyricsInfo, lyricContentArray);
                } else if (lyricType == TRANSLITERATION_LYRICS_TYPE) {
                    // Transliteration lyrics
                    parseTransliterationLyrics(lyricsInfo, lyricContentArray);
                }
            }
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析翻译歌词.
     *
     * @param lyricsInfo 歌词信息对象
     * @param lyricContentArray 歌词内容数组
     */
    private void parseTranslateLyrics(final LyricsInfo lyricsInfo, final JSONArray lyricContentArray) {
        final TranslateLyricsInfo translateLyricsInfo = new TranslateLyricsInfo();
        final List<TranslateLrcLineInfo> translateLrcLineInfos = new ArrayList<TranslateLrcLineInfo>();
        
        for (int i = 0; i < lyricContentArray.size(); i++) {
            final JSONArray lyricArray = lyricContentArray.getJSONArray(i);
            if (lyricArray.size() > 0) {
                final TranslateLrcLineInfo translateLrcLineInfo = new TranslateLrcLineInfo();
                translateLrcLineInfo.setLineLyrics(lyricArray.getString(0));
                translateLrcLineInfos.add(translateLrcLineInfo);
            }
        }
        
        if (translateLrcLineInfos.size() > 0) {
            translateLyricsInfo.setTranslateLrcLineInfos(translateLrcLineInfos);
            lyricsInfo.setTranslateLyricsInfo(translateLyricsInfo);
        }
    }

    /**
     * 解析音译歌词.
     *
     * @param lyricsInfo 歌词信息对象
     * @param lyricContentArray 歌词内容数组
     */
    private void parseTransliterationLyrics(final LyricsInfo lyricsInfo, final JSONArray lyricContentArray) {
        final TransliterationLyricsInfo transliterationLyricsInfo = new TransliterationLyricsInfo();
        final List<LyricsLineInfo> transliterationLrcLineInfos = new ArrayList<LyricsLineInfo>();
        
        for (int i = 0; i < lyricContentArray.size(); i++) {
            final JSONArray lyricArray = lyricContentArray.getJSONArray(i);
            if (lyricArray.size() > 0) {
                final LyricsLineInfo lyricsLineInfo = new LyricsLineInfo();
                final String[] lyricsWords = new String[lyricArray.size()];
                String lineLyrics = "";
                
                for (int j = 0; j < lyricArray.size(); j++) {
                    lyricsWords[j] = lyricArray.getString(j);
                    lineLyrics += lyricsWords[j];
                    if (j < lyricArray.size() - 1) {
                        lineLyrics += SPACE_STRING;
                    }
                }
                
                lyricsLineInfo.setLyricsWords(lyricsWords);
                lyricsLineInfo.setLineLyrics(lineLyrics);
                transliterationLrcLineInfos.add(lyricsLineInfo);
            }
        }
        
        if (transliterationLrcLineInfos.size() > 0) {
            transliterationLyricsInfo.setTransliterationLrcLineInfos(transliterationLrcLineInfos);
            lyricsInfo.setTransliterationLyricsInfo(transliterationLyricsInfo);
        }
    }

    /**
     * 解析歌词行.
     *
     * @param line 歌词行文本
     * @return 歌词行信息对象
     */
    private LyricsLineInfo parseLyricsLine(final String line) {
        try {
            // Extract content between parentheses
            final String content = line.substring(line.indexOf(LEFT_BRACKET_STRING) + 2, 
                    line.lastIndexOf(RIGHT_BRACKET_STRING));
            final String[] parts = content.split(SEPARATOR_STRING);
            
            if (parts.length >= MIN_PARSE_PARTS) {
                return buildLyricsLineInfo(parts);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 构建歌词行信息对象.
     *
     * @param parts 解析后的部分数组
     * @return 歌词行信息对象
     */
    private LyricsLineInfo buildLyricsLineInfo(final String[] parts) {
        final String timeText = parts[0];
        final String lyricsText = parts[1];
        final String wordsDisIntervalText = parts[2];
        
        final LyricsLineInfo lyricsLineInfo = new LyricsLineInfo();
        
        // Parse time - for HRCS, time is typically a single start time
        final int startTime = Integer.parseInt(timeText.trim());
        lyricsLineInfo.setStartTime(startTime);
        
        // Set lyrics text
        lyricsLineInfo.setLineLyrics(lyricsText);
        
        // Parse word intervals
        final int[] wordsDisInterval = parseWordsDisInterval(wordsDisIntervalText);
        lyricsLineInfo.setWordsDisInterval(wordsDisInterval);
        
        // Parse words - for HRCS, we split by characters
        final String[] lyricsWords = parseLyricsWords(lyricsText);
        lyricsLineInfo.setLyricsWords(lyricsWords);
        
        // Calculate end time
        final int endTime = calculateEndTime(startTime, wordsDisInterval);
        lyricsLineInfo.setEndTime(endTime);
        
        return lyricsLineInfo;
    }
    
    /**
     * 解析单词间隔时间数组.
     *
     * @param wordsDisIntervalText 单词间隔文本
     * @return 单词间隔时间数组
     */
    private int[] parseWordsDisInterval(final String wordsDisIntervalText) {
        final String[] wordsDisIntervalArray = wordsDisIntervalText.split(RIGHT_ANGLE_STRING);
        final List<Integer> intervals = new ArrayList<Integer>();
        
        for (final String intervalText : wordsDisIntervalArray) {
            if (intervalText.contains(LEFT_ANGLE_STRING)) {
                final String[] intervalParts = intervalText.substring(
                        intervalText.indexOf(LEFT_ANGLE_STRING) + 1).split(COMMA_STRING);
                if (intervalParts.length >= 2) {
                    intervals.add(Integer.parseInt(intervalParts[1].trim()));
                }
            }
        }
        
        final int[] wordsDisInterval = new int[intervals.size()];
        for (int i = 0; i < intervals.size(); i++) {
            wordsDisInterval[i] = intervals.get(i);
        }
        
        return wordsDisInterval;
    }
    
    /**
     * 解析歌词单词数组.
     *
     * @param lyricsText 歌词文本
     * @return 歌词单词数组
     */
    private String[] parseLyricsWords(final String lyricsText) {
        final String[] lyricsWords = new String[lyricsText.length()];
        for (int i = 0; i < lyricsText.length(); i++) {
            lyricsWords[i] = String.valueOf(lyricsText.charAt(i));
        }
        return lyricsWords;
    }
    
    /**
     * 计算结束时间.
     *
     * @param startTime 开始时间
     * @param wordsDisInterval 单词间隔时间数组
     * @return 结束时间
     */
    private int calculateEndTime(final int startTime, final int[] wordsDisInterval) {
        int endTime = startTime;
        for (final int interval : wordsDisInterval) {
            endTime += interval;
        }
        return endTime;
    }

    /**
     * 检查文件扩展名是否被支持.
     *
     * @param ext 文件扩展名
     * @return 是否支持
     */
    @Override
    public boolean isFileSupported(final String ext) {
        return getSupportFileExt().equals(ext);
    }

    /**
     * 获取支持的文件扩展名.
     *
     * @return 支持的文件扩展名
     */
    @Override
    public String getSupportFileExt() {
        return ".hrcs";
    }
}
