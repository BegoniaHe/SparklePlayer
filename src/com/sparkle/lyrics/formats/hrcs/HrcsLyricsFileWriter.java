package com.sparkle.lyrics.formats.hrcs;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;

import com.sparkle.lyrics.LyricsFileWriter;
import com.sparkle.lyrics.model.LyricsInfo;
import com.sparkle.lyrics.model.LyricsLineInfo;
import com.sparkle.lyrics.model.LyricsTag;
import com.sparkle.lyrics.model.TranslateLrcLineInfo;
import com.sparkle.lyrics.utils.StringCompressUtils;

/**
 * HRCS歌词写入器.
 * 
 * @author yuyi2003
 */
public class HrcsLyricsFileWriter extends LyricsFileWriter {
    
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
    
    /** 歌词类型常量. */
    private static final String LYRIC_TYPE = "lyricType";
    
    /** 歌词内容常量. */
    private static final String LYRIC_CONTENT = "lyricContent";
    
    /** 单引号前缀常量. */
    private static final String QUOTE_PREFIX = "('";
    
    /** 单引号后缀常量. */
    private static final String QUOTE_SUFFIX = "');\n";
    
    /** 单引号常量. */
    private static final String SINGLE_QUOTE = "'";
    
    /** 逗号单引号常量. */
    private static final String COMMA_QUOTE = ",'";
    
    /** 逗号常量. */
    private static final String COMMA = ",";
    
    /** 逗号零常量. */
    private static final String COMMA_ZERO = ",0";

    /**
     * 默认构造函数.
     */
    public HrcsLyricsFileWriter() {
    }

    /**
     * 写入歌词到文件.
     * 
     * @param lyricsInfo 歌词信息对象
     * @param lyricsFilePath 歌词文件路径
     * @return 写入是否成功
     * @throws Exception 写入过程中的异常
     */
    @Override
    public boolean writer(final LyricsInfo lyricsInfo, final String lyricsFilePath) throws Exception {
        try {
            final File lyricsFile = new File(lyricsFilePath);
            if (lyricsFile != null) {
                if (!lyricsFile.getParentFile().exists()) {
                    lyricsFile.getParentFile().mkdirs();
                }
                
                // 对字符串运行压缩
                final byte[] content = StringCompressUtils.compress(parseLyricsInfo(lyricsInfo), getDefaultCharset());
                
                // 生成歌词文件
                final FileOutputStream os = new FileOutputStream(lyricsFile);
                os.write(content);
                os.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 解析歌词对象类转换为字符串.
     * 
     * @param lyricsInfo 歌词信息对象
     * @return 解析后的歌词字符串
     */
    private String parseLyricsInfo(final LyricsInfo lyricsInfo) {
        String lyricsCom = "";
        
        // 先保存所有的标签数据
        lyricsCom += parseTagsInfo(lyricsInfo);
        
        // 翻译和音译歌词
        lyricsCom += parseExtraLyricsInfo(lyricsInfo);

        // 每行歌词内容
        lyricsCom += parseLyricsLines(lyricsInfo);
        
        return lyricsCom;
    }
    
    /**
     * 解析标签信息.
     * 
     * @param lyricsInfo 歌词信息对象
     * @return 标签信息字符串
     */
    private String parseTagsInfo(final LyricsInfo lyricsInfo) {
        String lyricsCom = "";
        final Map<String, Object> tags = lyricsInfo.getLyricsTags();
        for (Map.Entry<String, Object> entry : tags.entrySet()) {
            final Object val = entry.getValue();
            if (entry.getKey().equals(LyricsTag.TAG_TITLE)) {
                lyricsCom += LEGAL_TITLE_PREFIX;
            } else if (entry.getKey().equals(LyricsTag.TAG_ARTIST)) {
                lyricsCom += LEGAL_ARTIST_PREFIX;
            } else if (entry.getKey().equals(LyricsTag.TAG_OFFSET)) {
                lyricsCom += LEGAL_OFFSET_PREFIX;
            } else if (entry.getKey().equals(LyricsTag.TAG_BY)) {
                lyricsCom += LEGAL_BY_PREFIX;
            } else if (entry.getKey().equals(LyricsTag.TAG_TOTAL)) {
                lyricsCom += LEGAL_TOTAL_PREFIX;
            } else {
                lyricsCom += LEGAL_TAG_PREFIX + entry.getKey() + ":";
            }
            lyricsCom += val + "];\n";
        }
        return lyricsCom;
    }
    
    /**
     * 解析额外歌词信息(翻译和音译).
     * 
     * @param lyricsInfo 歌词信息对象
     * @return 额外歌词信息字符串
     */
    private String parseExtraLyricsInfo(final LyricsInfo lyricsInfo) {
        final JSONObject extraLyricsObj = new JSONObject();
        final JSONArray contentArray = new JSONArray();

        // 判断是否有翻译歌词
        addTranslateLyrics(lyricsInfo, contentArray);

        // 判断是否有音译歌词
        addTransliterationLyrics(lyricsInfo, contentArray);

        extraLyricsObj.put("content", contentArray);
        
        // 添加翻译和音译歌词
        return LEGAL_EXTRA_LYRICS_PREFIX + QUOTE_PREFIX 
            + Base64.encodeBase64String(extraLyricsObj.toString().getBytes()) + QUOTE_SUFFIX;
    }
    
    /**
     * 添加翻译歌词.
     * 
     * @param lyricsInfo 歌词信息对象
     * @param contentArray 内容数组
     */
    private void addTranslateLyrics(final LyricsInfo lyricsInfo, final JSONArray contentArray) {
        if (lyricsInfo.getTranslateLyricsInfo() != null) {
            final List<TranslateLrcLineInfo> translateLrcLineInfos = 
                lyricsInfo.getTranslateLyricsInfo().getTranslateLrcLineInfos();
            if (translateLrcLineInfos != null && translateLrcLineInfos.size() > 0) {
                final JSONObject lyricsObj = new JSONObject();
                final JSONArray lyricContentArray = new JSONArray();
                lyricsObj.put(LYRIC_TYPE, 1);
                
                for (int i = 0; i < translateLrcLineInfos.size(); i++) {
                    final JSONArray lyricArray = new JSONArray();
                    final TranslateLrcLineInfo translateLrcLineInfo = translateLrcLineInfos.get(i);
                    lyricArray.add(translateLrcLineInfo.getLineLyrics());
                    lyricContentArray.add(lyricArray);
                }
                
                if (lyricContentArray.size() > 0) {
                    lyricsObj.put(LYRIC_CONTENT, lyricContentArray);
                    contentArray.add(lyricsObj);
                }
            }
        }
    }
     /**
     * 添加音译歌词.
     * 
     * @param lyricsInfo 歌词信息对象
     * @param contentArray 内容数组
     */
    private void addTransliterationLyrics(final LyricsInfo lyricsInfo, final JSONArray contentArray) {
        if (lyricsInfo.getTransliterationLyricsInfo() != null) {
            final List<LyricsLineInfo> lyricsLineInfos = 
                lyricsInfo.getTransliterationLyricsInfo().getTransliterationLrcLineInfos();
            if (lyricsLineInfos != null && lyricsLineInfos.size() > 0) {
                final JSONObject lyricsObj = new JSONObject();
                final JSONArray lyricContentArray = new JSONArray();
                lyricsObj.put(LYRIC_TYPE, 0);
                
                for (int i = 0; i < lyricsLineInfos.size(); i++) {
                    final LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(i);
                    final String[] lyricsWords = lyricsLineInfo.getLyricsWords();
                    final JSONArray lyricArray = new JSONArray();
                    
                    for (int j = 0; j < lyricsWords.length; j++) {
                        lyricArray.add(lyricsWords[j].trim());
                    }
                    lyricContentArray.add(lyricArray);
                }
                
                if (lyricContentArray.size() > 0) {
                    lyricsObj.put(LYRIC_CONTENT, lyricContentArray);
                    contentArray.add(lyricsObj);
                }
            }
        }
    }
    
    /**
     * 解析歌词行信息.
     * 
     * @param lyricsInfo 歌词信息对象
     * @return 歌词行字符串
     */
    private String parseLyricsLines(final LyricsInfo lyricsInfo) {
        String lyricsCom = "";
        final TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = lyricsInfo.getLyricsLineInfoTreeMap();
        
        for (int i = 0; i < lyricsLineInfos.size(); i++) {
            final LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(i);
            lyricsCom += buildLyricsLine(lyricsLineInfo);
        }
        return lyricsCom;
    }
    
    /**
     * 构建单行歌词字符串.
     * 
     * @param lyricsLineInfo 歌词行信息
     * @return 单行歌词字符串
     */
    private String buildLyricsLine(final LyricsLineInfo lyricsLineInfo) {
        final int startTime = lyricsLineInfo.getStartTime();
        final String lyricsText = lyricsLineInfo.getLineLyrics();
        final int[] wordsDisInterval = lyricsLineInfo.getWordsDisInterval();
        
        String lyricsCom = LEGAL_LYRICS_LINE_PREFIX + QUOTE_PREFIX + startTime + SINGLE_QUOTE;
        lyricsCom += COMMA_QUOTE + lyricsText + SINGLE_QUOTE;
        
        // 构建字符间隔文本
        final String wordsDisIntervalText = buildWordsDisIntervalText(wordsDisInterval);
        
        lyricsCom += COMMA_QUOTE + wordsDisIntervalText + QUOTE_SUFFIX;
        return lyricsCom;
    }
    
    /**
     * 构建字符间隔文本.
     * 
     * @param wordsDisInterval 字符间隔数组
     * @return 字符间隔文本
     */
    private String buildWordsDisIntervalText(final int[] wordsDisInterval) {
        String wordsDisIntervalText = "";
        int lastTime = 0;
        for (int j = 0; j < wordsDisInterval.length; j++) {
            String wordsDisIntervalTextTemp = "";
            if (j == 0) {
                wordsDisIntervalTextTemp = lastTime + COMMA + wordsDisInterval[j] + COMMA_ZERO;
                lastTime = wordsDisInterval[j];
            } else {
                wordsDisIntervalTextTemp = lastTime + COMMA + wordsDisInterval[j] + COMMA_ZERO;
                lastTime += wordsDisInterval[j];
            }
            wordsDisIntervalText += "<" + wordsDisIntervalTextTemp + ">";
        }
        return wordsDisIntervalText;
    }

    /**
     * 检查文件扩展名是否被支持.
     * 
     * @param ext 文件扩展名
     * @return 是否支持该文件扩展名
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
