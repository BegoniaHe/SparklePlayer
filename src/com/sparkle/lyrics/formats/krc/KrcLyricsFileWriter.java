package com.sparkle.lyrics.formats.krc;

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
 * KRC歌词生成器
 * 
 * @author yuyi2003
 */
public class KrcLyricsFileWriter extends LyricsFileWriter {
    /**
     * 歌曲名 字符串
     */
    private final static String LEGAL_SONGNAME_PREFIX = "[ti:";
    /**
     * 歌手名 字符串
     */
    private final static String LEGAL_SINGERNAME_PREFIX = "[ar:";
    /**
     * 时间补偿值 字符串
     */
    private final static String LEGAL_OFFSET_PREFIX = "[offset:";
    /**
     * 额外歌词字符串
     */
    private final static String LEGAL_LANGUAGE_PREFIX = "[language:";
    /**
     * 解码参数
     */
    private static final char[] key = { '@', 'G', 'a', 'w', '^', '2', 't', 'G',
            'Q', '6', '1', '-', 'Î', 'Ò', 'n', 'i' };

    public KrcLyricsFileWriter() {
    }

    private String parseLyricsInfo(LyricsInfo lyricsIfno) throws Exception {
        String lyricsCom = "";
        // 先保存所有的标签数据
        Map<String, Object> tags = lyricsIfno.getLyricsTags();
        for (Map.Entry<String, Object> entry : tags.entrySet()) {
            Object val = entry.getValue();
            if (entry.getKey().equals(LyricsTag.TAG_TITLE)) {
                lyricsCom += LEGAL_SONGNAME_PREFIX;
            } else if (entry.getKey().equals(LyricsTag.TAG_ARTIST)) {
                lyricsCom += LEGAL_SINGERNAME_PREFIX;
            } else if (entry.getKey().equals(LyricsTag.TAG_OFFSET)) {
                lyricsCom += LEGAL_OFFSET_PREFIX;
            } else {
                val = "[" + entry.getKey() + ":" + val;
            }
            lyricsCom += val + "]\n";
        }

        JSONObject extraLyricsObj = new JSONObject();
        JSONArray contentArray = new JSONArray();
        // 判断是否有翻译歌词
        if (lyricsIfno.getTranslateLyricsInfo() != null) {
            List<TranslateLrcLineInfo> translateLrcLineInfos = lyricsIfno
                    .getTranslateLyricsInfo().getTranslateLrcLineInfos();
            if (translateLrcLineInfos != null
                    && translateLrcLineInfos.size() > 0) {
                JSONObject lyricsObj = new JSONObject();
                JSONArray lyricContentArray = new JSONArray();
                lyricsObj.put("language", 0);
                lyricsObj.put("type", 1);
                for (int i = 0; i < translateLrcLineInfos.size(); i++) {
                    JSONArray lyricArray = new JSONArray();
                    TranslateLrcLineInfo translateLrcLineInfo = translateLrcLineInfos
                            .get(i);
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
        if (lyricsIfno.getTransliterationLyricsInfo() != null) {
            List<LyricsLineInfo> lyricsLineInfos = lyricsIfno
                    .getTransliterationLyricsInfo()
                    .getTransliterationLrcLineInfos();
            if (lyricsLineInfos != null && lyricsLineInfos.size() > 0) {
                JSONObject lyricsObj = new JSONObject();
                JSONArray lyricContentArray = new JSONArray();
                lyricsObj.put("language", 0);
                lyricsObj.put("type", 0);
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
        //
        extraLyricsObj.put("content", contentArray);
        // 添加翻译和音译歌词
        lyricsCom += LEGAL_LANGUAGE_PREFIX
                + Base64.encodeBase64String(extraLyricsObj.toString()
                        .getBytes()) + "]\n";

        // [1679,1550]<0,399,0>作<399,200,0>词<599,250,0>：<849,301,0>李<1150,400,0>健
        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = lyricsIfno
                .getLyricsLineInfoTreeMap();
        // 每行歌词内容
        for (int i = 0; i < lyricsLineInfos.size(); i++) {
            LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(i);
            //
            int startTime = lyricsLineInfo.getStartTime();
            int endTime = lyricsLineInfo.getEndTime();
            lyricsCom += "[" + startTime + "," + (endTime - startTime) + "]";
            //
            String[] lyricsWords = lyricsLineInfo.getLyricsWords();
            int wordsDisInterval[] = lyricsLineInfo.getWordsDisInterval();
            int lastTime = 0;
            for (int j = 0; j < wordsDisInterval.length; j++) {
                if (j == 0) {
                    lyricsCom += "<0," + wordsDisInterval[j] + ",0>"
                            + lyricsWords[j];
                    lastTime = wordsDisInterval[j];
                } else {
                    lyricsCom += "<" + lastTime + "," + wordsDisInterval[j]
                            + ",0>" + lyricsWords[j];
                    lastTime += wordsDisInterval[j];
                }
            }
            lyricsCom += "\n";
        }

        return lyricsCom;
    }

    @Override
    public boolean writer(LyricsInfo lyricsIfno, String lyricsFilePath)
            throws Exception {
        try {
            File lyricsFile = new File(lyricsFilePath);
            if (lyricsFile != null) {
                //
                if (!lyricsFile.getParentFile().exists()) {
                    lyricsFile.getParentFile().mkdirs();
                }

                //
                // 对字符串运行压缩
                byte[] content = StringCompressUtils.compress(
                        parseLyricsInfo(lyricsIfno), getDefaultCharset());

                int j = content.length;
                for (int k = 0; k < j; k++) {
                    int l = k % 16;
                    int tmp67_65 = k;
                    byte[] tmp67_64 = content;
                    tmp67_64[tmp67_65] = (byte) (tmp67_64[tmp67_65] ^ key[l]);
                }
                String topText = "krc1";
                byte[] top = new byte[4];
                for (int i = 0; i < topText.length(); i++) {
                    top[i] = (byte) topText.charAt(i);
                }

                // 生成歌词文件
                FileOutputStream os = new FileOutputStream(lyricsFile);
                os.write(top);
                os.write(content);
                os.close();

            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("krc");
    }

    @Override
    public String getSupportFileExt() {
        return "krc";
    }
}
