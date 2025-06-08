package com.sparkle.lyrics.formats.lrc;

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
 * LRC歌词写入器
 * 
 * @author yuyi2003
 */
public class LrcLyricsFileWriter extends LyricsFileWriter {

    public LrcLyricsFileWriter() {
    }

    @Override
    public boolean writer(LyricsInfo lyricsInfo, String lyricsFilePath) throws Exception {
        if (lyricsInfo == null || lyricsFilePath == null) {
            return false;
        }
        
        try {
            File lyricsFile = new File(lyricsFilePath);
            if (!lyricsFile.getParentFile().exists()) {
                lyricsFile.getParentFile().mkdirs();
            }
            
            FileOutputStream fos = new FileOutputStream(lyricsFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos, getDefaultCharset());
            PrintWriter writer = new PrintWriter(osw);
            
            // 写入LRC内容
            String lrcContent = parseLyricsInfo(lyricsInfo);
            writer.print(lrcContent);
            
            writer.close();
            osw.close();
            fos.close();
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 解析歌词信息为LRC格式字符串
     * 
     * @param lyricsInfo 歌词信息
     * @return LRC格式字符串
     */
    private String parseLyricsInfo(LyricsInfo lyricsInfo) {
        StringBuilder lrcContent = new StringBuilder();
        
        // 写入标签信息
        Map<String, Object> tags = lyricsInfo.getLyricsTags();
        if (tags != null) {
            for (Map.Entry<String, Object> entry : tags.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (value == null) {
                    continue;
                }
                
                String valueStr = value.toString();
                
                if (LyricsTag.TAG_TITLE.equals(key)) {
                    lrcContent.append("[ti:").append(valueStr).append("]\n");
                } else if (LyricsTag.TAG_ARTIST.equals(key)) {
                    lrcContent.append("[ar:").append(valueStr).append("]\n");
                } else if ("lyrics.tag.album".equals(key)) {
                    lrcContent.append("[al:").append(valueStr).append("]\n");
                } else if (LyricsTag.TAG_OFFSET.equals(key)) {
                    lrcContent.append("[offset:").append(valueStr).append("]\n");
                } else if (LyricsTag.TAG_BY.equals(key)) {
                    lrcContent.append("[by:").append(valueStr).append("]\n");
                }
            }
        }
        
        // 写入歌词行
        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = lyricsInfo.getLyricsLineInfoTreeMap();
        if (lyricsLineInfos != null) {
            for (LyricsLineInfo lyricsLineInfo : lyricsLineInfos.values()) {
                if (lyricsLineInfo != null) {
                    // 转换时间为 [mm:ss.xx] 格式
                    String timeTag = formatTime(lyricsLineInfo.getStartTime());
                    String lyricsText = lyricsLineInfo.getLineLyrics();
                    
                    if (lyricsText == null) {
                        lyricsText = "";
                    }
                    
                    lrcContent.append(timeTag).append(lyricsText).append("\n");
                }
            }
        }
        
        return lrcContent.toString();
    }
    
    /**
     * 格式化时间为LRC格式 [mm:ss.xx]
     * 
     * @param timeInMillis 时间（毫秒）
     * @return 格式化的时间字符串
     */
    private String formatTime(int timeInMillis) {
        int totalSeconds = timeInMillis / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        int milliseconds = timeInMillis % 1000;
        
        // 将毫秒转换为百分之一秒
        int centiseconds = milliseconds / 10;
        
        return String.format("[%02d:%02d.%02d]", minutes, seconds, centiseconds);
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
