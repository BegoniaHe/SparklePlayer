package com.sparkle.common;

import java.io.File;
import java.util.Calendar;

public class Constants {
    /**
     * ==================================
     * <p>
     * 目录数据
     * </p>
     * ==================================
     */

    /**
     * 临时目录
     */
    public final static String PATH_TEMP = "sparkleplayer";

    /**
     * Logcat日志目录
     */
    public final static String PATH_LOGCAT = PATH_TEMP + File.separator + "logcat";

    /**
     * 歌曲目录
     */
    public final static String PATH_AUDIO = PATH_TEMP + File.separator + "audio";

    /**
     * 歌词目录
     */
    public final static String PATH_LYRICS = PATH_TEMP + File.separator + "lyrics";
    /**
     * 歌手写真目录
     */
    public final static String PATH_ARTIST = PATH_TEMP + File.separator + "artist";
    /**
     * 专辑图
     */
    public final static String PATH_ALBUM = PATH_TEMP + File.separator + "album";
    /**
     * 皮肤
     */
    public final static String PATH_SKIN = PATH_TEMP + File.separator + "skin";

    /**
     * 字体
     */
    public final static String PATH_FONTS = PATH_TEMP + File.separator + "fonts";
    /**
     * 图标
     */
    public final static String PATH_ICON = PATH_TEMP + File.separator + "icon";
    /**
     * db数据库保存到C盘里面
     */
    public final static String PATH_DB = PATH_TEMP + File.separator + "db";
    /**
     * ==================================
     * <p>
     * 基本数据
     * </p>
     * ==================================
     */    /**
     * app应用名
     */
    public final static String APPNAME = "Sparkle";
    /**
     * app标题
     */
    public final static String APPTITLE = "Sparkle";
    /**
     * app提示语
     */
    // public final static String APPTIPTITLE = "Sparkle";
    // public final static String APPTITLEANDYEAR = "Sparkle" + getYear();

    /**
     * App default font size
     */
    public final static String APP_FONTSIZE = "12";

    /**
     * App name for display
     */
    public final static String APP_NAME = APPTITLE;

    /**
     * 获取年份
     * 
     * @return
     */
    public static String getYear() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        return year + "";
    }

}
