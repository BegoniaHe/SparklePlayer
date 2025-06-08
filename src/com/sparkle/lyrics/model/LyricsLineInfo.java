package com.sparkle.lyrics.model;

import java.util.List;

/**
 * 动感歌词行信息实体类
 * 
 * @author yuyi2003
 */
public class LyricsLineInfo extends LrcLyricsLineInfo {

    /**
     * 歌词数组，用来分隔每个歌词
     */
    public String[] lyricsWords;
    
    /**
     * 数组，用来存放每个歌词的时间
     */
    private int[] wordsDisInterval;

    /**
     * 分割歌词行歌词
     */
    private List<LyricsLineInfo> splitLyricsLineInfos;

    public List<LyricsLineInfo> getSplitLyricsLineInfos() {
        return splitLyricsLineInfos;
    }

    public void setSplitLyricsLineInfos(List<LyricsLineInfo> splitLyricsLineInfos) {
        this.splitLyricsLineInfos = splitLyricsLineInfos;
    }

    public String[] getLyricsWords() {
        return lyricsWords;
    }

    public void setLyricsWords(String[] lyricsWords) {
        if (lyricsWords != null) {
            for (int i = 0; i < lyricsWords.length; i++) {
                if (lyricsWords[i] != null) {
                    lyricsWords[i] = lyricsWords[i].replaceAll("\r|\n", "");
                }
            }
        }
        this.lyricsWords = lyricsWords;
    }

    public int[] getWordsDisInterval() {
        return wordsDisInterval;
    }

    public void setWordsDisInterval(int[] wordsDisInterval) {
        this.wordsDisInterval = wordsDisInterval;
    }

    /**
     * 复制歌词行信息
     * 
     * @param dist 目标实体类
     * @param orig 原始实体类
     */
    public void copy(LyricsLineInfo dist, LyricsLineInfo orig) {
        if (dist != null && orig != null) {
            dist.setWordsDisInterval(orig.getWordsDisInterval());
            dist.setStartTime(orig.getStartTime());
            dist.setEndTime(orig.getEndTime());
            dist.setLyricsWords(orig.getLyricsWords());
            dist.setLineLyrics(orig.getLineLyrics());
        }
    }
}
