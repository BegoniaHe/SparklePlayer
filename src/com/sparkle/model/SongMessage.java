package com.sparkle.model;

/**
 * 
 * 歌曲信息类
 * 
 */
public class SongMessage {
    private static int i = 1000;

    public static final int INITMUSIC = (i++); // 初始化歌曲操作
    public static final int REINITMUSIC = (i++); // 重新播放歌曲
    public static final int PREMUSIC = (i++); // 上一首歌曲操作
    public static final int PLAYMUSIC = (i++); // 播放歌曲操作
    public static final int PLAYINFOMUSIC = (i++); // 播放歌曲
    public static final int PAUSEMUSIC = (i++); // 暂停歌曲操作
    public static final int STOPMUSIC = (i++); // 结束歌曲操作
    public static final int NEXTMUSIC = (i++); // 下一首歌曲操作
    public static final int ERRORMUSIC = (i++); // 歌曲错误操作
    public static final int SEEKTOMUSIC = (i++); // 歌曲快进操作

    public static final int SERVICEPLAYMUSIC = (i++); // 服务播放歌曲操作
    public static final int SERVICEPLAYINIT = (i++); // 服务播放歌曲操作
    public static final int SERVICEPLAYINGMUSIC = (i++); // 服务正在播放歌曲操作
    public static final int SERVICEPAUSEMUSIC = (i++); // 服务暂停歌曲操作
    public static final int SERVICEPAUSEEDMUSIC = (i++); // 服务已经暂停歌曲操作
    public static final int SERVICESTOPMUSIC = (i++); // 服务结束歌曲操作
    public static final int SERVICESTOPEDMUSIC = (i++); // 服务已经结束歌曲操作
    public static final int SERVICESEEKTOMUSIC = (i++); // 服务快进歌曲
    public static final int SERVICEERRORMUSIC = (i++); // 服务播放歌曲错误操作

    public static final int KSCTYPELRC = (i++); // lrcview ksc歌词
    public static final int LRCKSCDOWNLOADED = (i++); // lrcview ksc歌词下载完成
    public static final int LRCKSCLOADED = (i++); // lrcview ksc歌词下载完成

    public static final int KSCTYPEDES = (i++); // 桌面 ksc歌词
    public static final int DESKSCDOWNLOADED = (i++); // 桌面 ksc歌词下载完成
    public static final int DESKSCLOADED = (i++); // 桌面 ksc歌词下载完成

    public static final int KSCTYPELOCK = (i++); // 锁屏ksc歌词
    public static final int LOCKKSCLOADED = (i++); // 锁屏 ksc歌词下载完成

    /**
     * 歌曲为空报错信息
     */
    public static final String ERRORMESSAGESONGNULL = "请选择播放歌曲";
    /**
     * 播放歌曲为空报错信息
     */
    public static final String ERRORMESSAGEPLAYSONGNULL = "播放歌曲为空报错";

    private int type;
    private SongInfo songInfo;// 歌曲数据
    private String errorMessage;// 错误信息

    private int progress = 0;// 进度
    private String lrcFilePath; // lrc歌词路径
    private String sid;// ksc歌词所属的sid

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public SongInfo getSongInfo() {
        return songInfo;
    }

    public void setSongInfo(SongInfo songInfo) {
        this.songInfo = songInfo;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getLrcFilePath() {
        return lrcFilePath;
    }

    public void setLrcFilePath(String lrcFilePath) {
        this.lrcFilePath = lrcFilePath;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

}
