package com.sparkle.manage;

import java.io.File;

import com.sparkle.util.LyricsUtil;

/**
 * 歌词处理类.
 * 
 * @author yuyi2003
 * 
 */
public final class LyricsManage {

    /**
     * 当前歌词的歌曲sid.
     */
    private static String mSid = "";

    /**
     * 当前歌词解析器.
     */
    private static LyricsUtil mLyricsUtil;

    /**
     * 私有构造函数防止实例化.
     */
    private LyricsManage() {
        // 工具类不允许实例化
    }    /**
     * 通过歌曲的sid和歌词路径获取歌词解析器.
     * 
     * @param sid 歌曲ID
     * @param lrcFile 歌词文件
     * @return 歌词解析器
     */
    public static LyricsUtil getLyricsParser(final String sid, final File lrcFile) {
        if (sid.equals(mSid)) {
            if (mLyricsUtil == null) {
                mLyricsUtil = new LyricsUtil(lrcFile);
            }
        } else {
            mSid = sid;
            mLyricsUtil = new LyricsUtil(lrcFile);
        }
        return mLyricsUtil;
    }

    /**
     * 获取歌词解析.
     * 
     * @param sid 歌曲ID
     * @return 歌词解析器
     */
    public static LyricsUtil getLyricsParser(final String sid) {
        if (sid.equals(mSid)) {
            return mLyricsUtil;
        }
        return null;
    }

    /**
     * 通过歌曲ID获取KSC歌词解析器.
     * 
     * @param sid 歌曲ID
     * @return 歌词解析器
     */
    public static LyricsUtil getKscLyricsParserByInputStream(final String sid) {
        if (sid.equals(mSid)) {
            if (mLyricsUtil == null) {
                mLyricsUtil = new LyricsUtil();
            }
        } else {
            mSid = sid;
            mLyricsUtil = new LyricsUtil();

        }
        return mLyricsUtil;
    }

    /**
     * 清空数据.
     */
    public static void clean() {
        mLyricsUtil = null;
    }
}
