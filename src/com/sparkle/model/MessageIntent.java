package com.sparkle.model;

/**
 * 消息传递类
 * 
 * @author yuyi2003
 * 
 */
public class MessageIntent {
    /**
     * 默认
     */
    public static final String FRAME_NORMAL = "com.sparkle.frame.normal";

    /**
     * 多行歌词字体大小
     */
    public static final String MANYLINEFONTSIZE = "com.sparkle.lyrics.fontsize";
    /**
     * 多行歌词歌词颜色
     */
    public static final String MANYLINELRCCOLOR = "com.sparkle.lyrics.color";
    /**
     * 桌面歌词窗口
     */
    public static final String OPENORCLOSEDESLRC = "com.sparkle.frame.openorclosedeslrc";
    /**
     * 桌面歌词桌面关闭
     */
    public static final String CLOSEDESLRC = "com.sparkle.frame.closedeslrc";

    /**
     * 多行桌面歌词字体大小
     */
    public static final String DESMANYLINEFONTSIZE = "com.sparkle.lyrics.des.fontsize";
    /**
     * 多行桌面歌词歌词颜色
     */
    public static final String DESMANYLINELRCCOLOR = "com.sparkle.lyrics.des.color";
    /**
     * 播放器音量
     */
    public static final String PLAYERVOLUME = "com.sparkle.player.Volume";
    /**
     * 桌面歌词锁
     */
    public static final String LOCKDESLRC = "com.sparkle.frame.locklyrics";
    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

}
