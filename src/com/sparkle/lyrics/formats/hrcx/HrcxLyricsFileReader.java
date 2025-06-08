package com.sparkle.lyrics.formats.hrcx;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.sparkle.lyrics.utils.CharUtils;
import com.sparkle.lyrics.utils.StringCompressUtils;

/**
 * HRCX歌词解析器
 * 
 * @author yuyi2003
 */
public class HrcxLyricsFileReader extends LyricsFileReader {
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

    public HrcxLyricsFileReader() {
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
            byte[] buffer = new byte[in.available()];
            in.read(buffer);
            in.close();
            
            String content = StringCompressUtils.decompress(buffer, getDefaultCharset());
            String[] lines = content.split("\n");
            
            TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = new TreeMap<Integer, LyricsLineInfo>();
            Map<String, Object> lyricsTags = new HashMap<String, Object>();
            int index = 0;

            for (String line : lines) {
                try {
                    if (line.startsWith(LEGAL_TITLE_PREFIX)) {
                        String title = line.substring(LEGAL_TITLE_PREFIX.length(), line.lastIndexOf("]"));
                        lyricsTags.put(LyricsTag.TAG_TITLE, title);
                    } else if (line.startsWith(LEGAL_ARTIST_PREFIX)) {
                        String artist = line.substring(LEGAL_ARTIST_PREFIX.length(), line.lastIndexOf("]"));
                        lyricsTags.put(LyricsTag.TAG_ARTIST, artist);
                    } else if (line.startsWith(LEGAL_OFFSET_PREFIX)) {
                        String offset = line.substring(LEGAL_OFFSET_PREFIX.length(), line.lastIndexOf("]"));
                        lyricsTags.put(LyricsTag.TAG_OFFSET, Integer.parseInt(offset));
                    } else if (line.startsWith(LEGAL_BY_PREFIX)) {
                        String by = line.substring(LEGAL_BY_PREFIX.length(), line.lastIndexOf("]"));
                        lyricsTags.put(LyricsTag.TAG_BY, by);
                    } else if (line.startsWith(LEGAL_TOTAL_PREFIX)) {
                        String total = line.substring(LEGAL_TOTAL_PREFIX.length(), line.lastIndexOf("]"));
                        lyricsTags.put(LyricsTag.TAG_TOTAL, Integer.parseInt(total));
                    } else if (line.startsWith(LEGAL_TAG_PREFIX)) {
                        // Parse custom tags
                        String tagContent = line.substring(LEGAL_TAG_PREFIX.length(), line.lastIndexOf("]"));
                        if (tagContent.contains(":")) {
                            String[] parts = tagContent.split(":", 2);
                            lyricsTags.put(parts[0], parts[1]);
                        }
                    } else if (line.startsWith(LEGAL_EXTRA_LYRICS_PREFIX)) {
                        // Parse extra lyrics (translations/transliterations)
                        parseExtraLyrics(line, lyricsInfo);
                    } else if (line.startsWith(LEGAL_LYRICS_LINE_PREFIX)) {
                        // Parse lyrics line
                        LyricsLineInfo lyricsLineInfo = parseLyricsLine(line);
                        if (lyricsLineInfo != null) {
                            lyricsLineInfos.put(index++, lyricsLineInfo);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 设置歌词的标签类
            lyricsInfo.setLyricsTags(lyricsTags);
            lyricsInfo.setLyricsLineInfoTreeMap(lyricsLineInfos);
        }
        return lyricsInfo;
    }

    private void parseExtraLyrics(String line, LyricsInfo lyricsInfo) throws Exception {
        String base64Content = line.substring(line.indexOf("('") + 2, line.lastIndexOf("')"));
        String jsonContent = new String(Base64.decodeBase64(base64Content));
        
        try {
            JSONObject extraObj = JSONObject.fromObject(jsonContent);
            JSONArray contentArray = extraObj.getJSONArray("content");
            
            for (int i = 0; i < contentArray.size(); i++) {
                JSONObject lyricsObj = contentArray.getJSONObject(i);
                int lyricType = lyricsObj.getInt("lyricType");
                JSONArray lyricContentArray = lyricsObj.getJSONArray("lyricContent");
                
                if (lyricType == 1) {
                    // Translation lyrics
                    parseTranslateLyrics(lyricsInfo, lyricContentArray);
                } else if (lyricType == 0) {
                    // Transliteration lyrics
                    parseTransliterationLyrics(lyricsInfo, lyricContentArray);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseTranslateLyrics(LyricsInfo lyricsInfo, JSONArray lyricContentArray) {
        TranslateLyricsInfo translateLyricsInfo = new TranslateLyricsInfo();
        List<TranslateLrcLineInfo> translateLrcLineInfos = new ArrayList<TranslateLrcLineInfo>();
        
        for (int i = 0; i < lyricContentArray.size(); i++) {
            JSONArray lyricArray = lyricContentArray.getJSONArray(i);
            if (lyricArray.size() > 0) {
                TranslateLrcLineInfo translateLrcLineInfo = new TranslateLrcLineInfo();
                translateLrcLineInfo.setLineLyrics(lyricArray.getString(0));
                translateLrcLineInfos.add(translateLrcLineInfo);
            }
        }
        
        if (translateLrcLineInfos.size() > 0) {
            translateLyricsInfo.setTranslateLrcLineInfos(translateLrcLineInfos);
            lyricsInfo.setTranslateLyricsInfo(translateLyricsInfo);
        }
    }

    private void parseTransliterationLyrics(LyricsInfo lyricsInfo, JSONArray lyricContentArray) {
        TransliterationLyricsInfo transliterationLyricsInfo = new TransliterationLyricsInfo();
        List<LyricsLineInfo> transliterationLrcLineInfos = new ArrayList<LyricsLineInfo>();
        
        for (int i = 0; i < lyricContentArray.size(); i++) {
            JSONArray lyricArray = lyricContentArray.getJSONArray(i);
            if (lyricArray.size() > 0) {
                LyricsLineInfo lyricsLineInfo = new LyricsLineInfo();
                String[] lyricsWords = new String[lyricArray.size()];
                String lineLyrics = "";
                
                for (int j = 0; j < lyricArray.size(); j++) {
                    lyricsWords[j] = lyricArray.getString(j);
                    lineLyrics += lyricsWords[j];
                    if (j < lyricArray.size() - 1) {
                        lineLyrics += " ";
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

    private LyricsLineInfo parseLyricsLine(String line) {
        try {
            // Extract content between parentheses
            String content = line.substring(line.indexOf("('") + 2, line.lastIndexOf("')"));
            String[] parts = content.split("','");
            
            if (parts.length >= 3) {
                String timeText = parts[0];
                String lyricsText = parts[1];
                String wordsDisIntervalText = parts[2];
                
                LyricsLineInfo lyricsLineInfo = new LyricsLineInfo();
                
                // Parse time intervals
                String[] timeArray = timeText.split(",");
                List<Integer> startTimes = new ArrayList<Integer>();
                for (String time : timeArray) {
                    startTimes.add(Integer.parseInt(time.trim()));
                }
                
                if (startTimes.size() > 0) {
                    lyricsLineInfo.setStartTime(startTimes.get(0));
                }
                
                // Set lyrics text
                lyricsLineInfo.setLineLyrics(lyricsText);
                
                // Parse word intervals
                String[] wordsDisIntervalArray = wordsDisIntervalText.split(">");
                List<Integer> intervals = new ArrayList<Integer>();
                for (String intervalText : wordsDisIntervalArray) {
                    if (intervalText.contains("<")) {
                        String[] intervalParts = intervalText.substring(intervalText.indexOf("<") + 1).split(",");
                        if (intervalParts.length >= 2) {
                            intervals.add(Integer.parseInt(intervalParts[1].trim()));
                        }
                    }
                }
                
                int[] wordsDisInterval = new int[intervals.size()];
                for (int i = 0; i < intervals.size(); i++) {
                    wordsDisInterval[i] = intervals.get(i);
                }
                lyricsLineInfo.setWordsDisInterval(wordsDisInterval);
                
                // Parse words
                List<String> lineLyricsList = getLyricsWords(lyricsText);
                String[] lyricsWords = lineLyricsList.toArray(new String[lineLyricsList.size()]);
                lyricsLineInfo.setLyricsWords(lyricsWords);
                
                // Calculate end time
                int endTime = lyricsLineInfo.getStartTime();
                for (int interval : wordsDisInterval) {
                    endTime += interval;
                }
                lyricsLineInfo.setEndTime(endTime);
                
                return lyricsLineInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        return ".hrcx";
    }
}
