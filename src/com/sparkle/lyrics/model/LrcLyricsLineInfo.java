package com.sparkle.lyrics.model;

/**
 * LRC歌词行信息实体类
 * 
 * @author yuyi2003
 */
public class LrcLyricsLineInfo {
    /**
     * 歌词开始时间
     */
    private int startTime;
    
    /**
     * 歌词结束时间
     */
    private int endTime;
    
    /**
     * 该行歌词
     */
    private String lineLyrics;

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public String getLineLyrics() {
        return lineLyrics;
    }

    public void setLineLyrics(String lineLyrics) {
        if (lineLyrics != null) {
            this.lineLyrics = lineLyrics.replaceAll("\r|\n", "");
        } else {
            this.lineLyrics = null;
        }
    }
}
