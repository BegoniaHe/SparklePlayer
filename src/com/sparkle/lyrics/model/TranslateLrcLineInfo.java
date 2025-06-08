package com.sparkle.lyrics.model;

import java.util.List;

/**
 * 翻译行歌词信息
 * 
 * @author yuyi2003
 */
public class TranslateLrcLineInfo {

    /**
     * 该行歌词
     */
    private String lineLyrics;

    /**
     * 分割翻译行歌词
     */
    private List<TranslateLrcLineInfo> splitTranslateLrcLineInfos;

    public List<TranslateLrcLineInfo> getSplitTranslateLrcLineInfos() {
        return splitTranslateLrcLineInfos;
    }

    public void setSplitTranslateLrcLineInfos(List<TranslateLrcLineInfo> splitTranslateLrcLineInfos) {
        this.splitTranslateLrcLineInfos = splitTranslateLrcLineInfos;
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

    /**
     * 复制翻译歌词行信息
     * 
     * @param dist 目标实体类
     * @param orig 原始实体类
     */
    public void copy(TranslateLrcLineInfo dist, TranslateLrcLineInfo orig) {
        if (dist != null && orig != null) {
            dist.setLineLyrics(orig.getLineLyrics());
        }
    }
}
