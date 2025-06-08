package com.sparkle.lyrics.formats.ksc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import com.sparkle.lyrics.LyricsFileWriter;
import com.sparkle.lyrics.model.LyricsInfo;
import com.sparkle.lyrics.model.LyricsLineInfo;
import com.sparkle.lyrics.model.LyricsTag;

/**
 * KSC歌词保存器
 * 
 * @author yuyi2003
 */
public class KscLyricsFileWriter extends LyricsFileWriter {
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
    public final static String LEGAL_TAG_PREFIX = "karaoke.tag";

    public KscLyricsFileWriter() {
    }

    private String parseLyricsInfo(LyricsInfo lyricsInfo) throws Exception {
        String lyricsCom = "";
        
        // 先保存所有的标签数据
        Map<String, Object> tags = lyricsInfo.getLyricsTags();
        for (Map.Entry<String, Object> entry : tags.entrySet()) {
            Object val = entry.getValue();
            if (entry.getKey().equals(LyricsTag.TAG_TITLE)) {
                lyricsCom += LEGAL_SONGNAME_PREFIX;
            } else if (entry.getKey().equals(LyricsTag.TAG_ARTIST)) {
                lyricsCom += LEGAL_SINGERNAME_PREFIX;
            } else if (entry.getKey().equals(LyricsTag.TAG_OFFSET)) {
                lyricsCom += LEGAL_OFFSET_PREFIX;
            } else {
                lyricsCom += LEGAL_TAG_PREFIX;
                val = entry.getKey() + ":" + val;
            }
            lyricsCom += "('" + val + "');\n";
        }

        // 每行歌词内容
        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = lyricsInfo.getLyricsLineInfoTreeMap();
        for (int i = 0; i < lyricsLineInfos.size(); i++) {
            LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(i);
            
            int startTime = lyricsLineInfo.getStartTime();
            String lyricsText = lyricsLineInfo.getLineLyrics();
            int[] wordsDisInterval = lyricsLineInfo.getWordsDisInterval();
            
            lyricsCom += LEGAL_LYRICS_LINE_PREFIX + "('" + startTime + "','" + lyricsText + "','";
            
            String wordsDisIntervalText = "";
            for (int j = 0; j < wordsDisInterval.length; j++) {
                if (j == 0) {
                    wordsDisIntervalText += wordsDisInterval[j] + "";
                } else {
                    wordsDisIntervalText += "," + wordsDisInterval[j] + "";
                }
            }
            lyricsCom += wordsDisIntervalText + "');\n";
        }
        return lyricsCom;
    }

    @Override
    public boolean writer(LyricsInfo lyricsInfo, String lyricsFilePath) throws Exception {
        try {
            File lyricsFile = new File(lyricsFilePath);
            if (lyricsFile != null) {
                if (!lyricsFile.getParentFile().exists()) {
                    lyricsFile.getParentFile().mkdirs();
                }
                
                FileOutputStream fos = new FileOutputStream(lyricsFile);
                OutputStreamWriter osw = new OutputStreamWriter(fos, getDefaultCharset());
                PrintWriter writer = new PrintWriter(osw);
                
                writer.print(parseLyricsInfo(lyricsInfo));
                writer.close();
                osw.close();
                fos.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
