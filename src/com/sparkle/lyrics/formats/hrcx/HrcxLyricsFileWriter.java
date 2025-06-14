package com.sparkle.lyrics.formats.hrcx;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
 * HRCX歌词写入器
 * 
 * @author yuyi2003
 */
public class HrcxLyricsFileWriter extends LyricsFileWriter {
    /**
     * 歌曲名 字符串
     */
    private final static String LEGAL_TITLE_PREFIX = "[ti:";
    /**
     * 歌手名 字符串
     */
    private final static String LEGAL_ARTIST_PREFIX = "[ar:";
    /**
     * 时间补偿值 字符串
     */
    private final static String LEGAL_OFFSET_PREFIX = "[offset:";
    /**
     * 歌曲长度
     */
    private final static String LEGAL_TOTAL_PREFIX = "[total:";
    /**
     * 上传者
     */
    private final static String LEGAL_BY_PREFIX = "[by:";
    /**
     * Tag标签
     */
    private final static String LEGAL_TAG_PREFIX = "haplayer.tag[";

    /**
     * 歌词 字符串
     */
    public final static String LEGAL_LYRICS_LINE_PREFIX = "haplayer.lrc";

    /**
     * 额外歌词
     */
    private final static String LEGAL_EXTRA_LYRICS_PREFIX = "haplayer.extra.lrc";

    public HrcxLyricsFileWriter() {
    }

    @Override
    public boolean writer(LyricsInfo lyricsInfo, String lyricsFilePath) throws Exception {
        try {
            File lyricsFile = new File(lyricsFilePath);
            if (lyricsFile != null) {
                if (!lyricsFile.getParentFile().exists()) {
                    lyricsFile.getParentFile().mkdirs();
                }
                
                // 对字符串运行压缩
                byte[] content = StringCompressUtils.compress(parseLyricsInfo(lyricsInfo), getDefaultCharset());
                
                // 生成歌词文件
                FileOutputStream os = new FileOutputStream(lyricsFile);
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
     * 解析歌词对象类转换为字符串
     */
    private String parseLyricsInfo(LyricsInfo lyricsInfo) {
        String lyricsCom = "";
        
        // 先保存所有的标签数据
        Map<String, Object> tags = lyricsInfo.getLyricsTags();
        for (Map.Entry<String, Object> entry : tags.entrySet()) {
            Object val = entry.getValue();
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

        // 翻译和音译歌词
        JSONObject extraLyricsObj = new JSONObject();
        JSONArray contentArray = new JSONArray();

        // 判断是否有翻译歌词
        if (lyricsInfo.getTranslateLyricsInfo() != null) {
            List<TranslateLrcLineInfo> translateLrcLineInfos = lyricsInfo.getTranslateLyricsInfo().getTranslateLrcLineInfos();
            if (translateLrcLineInfos != null && translateLrcLineInfos.size() > 0) {
                JSONObject lyricsObj = new JSONObject();
                JSONArray lyricContentArray = new JSONArray();
                lyricsObj.put("lyricType", 1);
                
                for (int i = 0; i < translateLrcLineInfos.size(); i++) {
                    JSONArray lyricArray = new JSONArray();
                    TranslateLrcLineInfo translateLrcLineInfo = translateLrcLineInfos.get(i);
                    lyricArray.add(translateLrcLineInfo.getLineLyrics());
                    lyricContentArray.add(lyricArray);
                }
                
                if (lyricContentArray.size() > 0) {
                    lyricsObj.put("lyricContent", lyricContentArray);
                    contentArray.add(lyricsObj);
                }
            }
        }

        // 判断是否有音译歌词
        if (lyricsInfo.getTransliterationLyricsInfo() != null) {
            List<LyricsLineInfo> lyricsLineInfos = lyricsInfo.getTransliterationLyricsInfo().getTransliterationLrcLineInfos();
            if (lyricsLineInfos != null && lyricsLineInfos.size() > 0) {
                JSONObject lyricsObj = new JSONObject();
                JSONArray lyricContentArray = new JSONArray();
                lyricsObj.put("lyricType", 0);
                
                for (int i = 0; i < lyricsLineInfos.size(); i++) {
                    LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(i);
                    String[] lyricsWords = lyricsLineInfo.getLyricsWords();
                    JSONArray lyricArray = new JSONArray();
                    
                    for (int j = 0; j < lyricsWords.length; j++) {
                        lyricArray.add(lyricsWords[j].trim());
                    }
                    lyricContentArray.add(lyricArray);
                }
                
                if (lyricContentArray.size() > 0) {
                    lyricsObj.put("lyricContent", lyricContentArray);
                    contentArray.add(lyricsObj);
                }
            }
        }

        extraLyricsObj.put("content", contentArray);
        
        // 添加翻译和音译歌词
        lyricsCom += LEGAL_EXTRA_LYRICS_PREFIX + "('" + Base64.encodeBase64String(extraLyricsObj.toString().getBytes()) + "');\n";

        // 每行歌词内容
        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = lyricsInfo.getLyricsLineInfoTreeMap();
        
        // 将每行歌词，放到有序的map，判断已重复的歌词
        LinkedHashMap<String, List<Integer>> lyricsLineInfoMapResult = new LinkedHashMap<String, List<Integer>>();
        
        for (int i = 0; i < lyricsLineInfos.size(); i++) {
            LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(i);
            String lyricsText = lyricsLineInfo.getLineLyrics();
            List<Integer> indexs;
            
            if (lyricsLineInfoMapResult.containsKey(lyricsText)) {
                indexs = lyricsLineInfoMapResult.get(lyricsText);
            } else {
                indexs = new ArrayList<Integer>();
            }
            indexs.add(i);
            lyricsLineInfoMapResult.put(lyricsText, indexs);
        }

        // 遍历
        for (Map.Entry<String, List<Integer>> entry : lyricsLineInfoMapResult.entrySet()) {
            lyricsCom += LEGAL_LYRICS_LINE_PREFIX + "('";
            List<Integer> indexs = entry.getValue();
            
            // 当前行歌词文本
            String lyricsText = entry.getKey();
            
            // 时间列表
            String timeText = "";
            for (int i = 0; i < indexs.size(); i++) {
                int index = indexs.get(i);
                LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(index);
                int startTime = lyricsLineInfo.getStartTime();
                
                if (i == 0) {
                    timeText += startTime;
                } else {
                    timeText += "," + startTime;
                }
            }

            // 获取第一个歌词行的详细信息
            int firstIndex = indexs.get(0);
            LyricsLineInfo firstLyricsLineInfo = lyricsLineInfos.get(firstIndex);
            int[] wordsDisInterval = firstLyricsLineInfo.getWordsDisInterval();
            
            String wordsDisIntervalText = "";
            int lastTime = 0;
            for (int j = 0; j < wordsDisInterval.length; j++) {
                String wordsDisIntervalTextTemp = "";
                if (j == 0) {
                    wordsDisIntervalTextTemp = lastTime + "," + wordsDisInterval[j] + ",0";
                    lastTime = wordsDisInterval[j];
                } else {
                    wordsDisIntervalTextTemp = lastTime + "," + wordsDisInterval[j] + ",0";
                    lastTime += wordsDisInterval[j];
                }
                wordsDisIntervalText += "<" + wordsDisIntervalTextTemp + ">";
            }
            
            lyricsCom += timeText + "'";
            lyricsCom += ",'" + lyricsText + "'";
            lyricsCom += ",'" + wordsDisIntervalText + "');\n";
        }
        return lyricsCom;
    }

    @Override
    public boolean isFileSupported(String ext) {
        return getSupportFileExt().equals(ext);
    }

    @Override
    public String getSupportFileExt() {
        return ".hrcx";
    }
}
