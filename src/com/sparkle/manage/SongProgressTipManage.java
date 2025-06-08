package com.sparkle.manage;

import com.sparkle.widget.dialog.ProgressTipDialog;

/**
 * 歌曲进度提示管理.
 * 
 * @author yuyi2003
 * 
 */
public class SongProgressTipManage {
    /**
     * 是否显示.
     */
    private static boolean isShow;

    /**
     * 单例实例.
     */
    private static SongProgressTipManage songInfoTipManage;

    /**
     * 歌曲进度提示窗口.
     */
    private static ProgressTipDialog songProgressTipDialog;

    /**
     * 获取单例实例.
     * 
     * @return 单例实例
     */
    public static SongProgressTipManage getSongInfoTipManage() {
        if (songInfoTipManage == null) {
            songInfoTipManage = new SongProgressTipManage();
        }
        return songInfoTipManage;
    }

    /**
     * 构造函数.
     */
    public SongProgressTipManage() {
        songProgressTipDialog = new ProgressTipDialog();
        songProgressTipDialog.setVisible(false);
    }    /**
     * 获取歌曲进度提示对话框.
     * 
     * @return 歌曲进度提示对话框
     */
    public ProgressTipDialog getSongProgressTipDialog() {
        return songProgressTipDialog;
    }

    /**
     * 显示窗口.
     */
    public static void showSongProgressTipDialog() {
        if (songProgressTipDialog != null) {
            isShow = true;
            if (!songProgressTipDialog.isShowing()) {
                songProgressTipDialog.setVisible(true);
            }
        }
    }

    /**
     * 隐藏歌曲信息窗口.
     */
    public static void hideSongProgressTipDialog() {
        if (songProgressTipDialog != null) {
            isShow = false;
            new Thread() {

                private static final int SLEEP_DURATION = 100;

                @Override
                public void run() {
                    try {
                        Thread.sleep(SLEEP_DURATION);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!isShow) {
                        songProgressTipDialog.setVisible(false);
                    }
                }

            }.start();
        }
    }
}
