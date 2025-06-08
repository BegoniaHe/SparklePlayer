package com.sparkle.lyrics.model;

import java.util.List;

/**
 * 翻译歌词信息
 * 
 * @author yuyi2003
 */
public class TranslateLyricsInfo {
    /**
     * 翻译行歌词
     */
    private List<TranslateLrcLineInfo> translateLrcLineInfos;

    public List<TranslateLrcLineInfo> getTranslateLrcLineInfos() {
        return translateLrcLineInfos;
    }

    public void setTranslateLrcLineInfos(List<TranslateLrcLineInfo> translateLrcLineInfos) {
        this.translateLrcLineInfos = translateLrcLineInfos;
    }
}
