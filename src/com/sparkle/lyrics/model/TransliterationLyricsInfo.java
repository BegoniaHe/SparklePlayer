package com.sparkle.lyrics.model;

import java.util.List;

/**
 * 音译歌词信息
 * 
 * @author yuyi2003
 */
public class TransliterationLyricsInfo {

    /**
     * 音译歌词行
     */
    private List<LyricsLineInfo> transliterationLrcLineInfos;

    public List<LyricsLineInfo> getTransliterationLrcLineInfos() {
        return transliterationLrcLineInfos;
    }

    public void setTransliterationLrcLineInfos(List<LyricsLineInfo> transliterationLrcLineInfos) {
        this.transliterationLrcLineInfos = transliterationLrcLineInfos;
    }
}
