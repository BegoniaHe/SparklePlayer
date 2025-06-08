package com.sparkle.manage;

import com.sparkle.widget.dialog.SongInfoDialog;

/**
 * 歌曲提示管理.
 * 
 * @author yuyi2003
 * 
 */
public class SongInfoTipManage {
    /**
     * 是否显示.
     */
    private static boolean isShow;

    /**
     * 单例实例.
     */
    private static SongInfoTipManage songInfoTipManage;

    /**
     * 歌曲提示窗口.
     */
    private static SongInfoDialog songInfoDialog;

    /**
     * 获取单例实例.
     * 
     * @param width 宽度
     * @param height 高度
     * @return 单例实例
     */
    public static SongInfoTipManage getSongInfoTipManage(final int width, final int height) {
        if (songInfoTipManage == null) {
            songInfoTipManage = new SongInfoTipManage(width, height);
        }
        return songInfoTipManage;
    }

    /**
     * 构造函数.
     * 
     * @param width 宽度
     * @param height 高度
     */
    public SongInfoTipManage(final int width, final int height) {
        songInfoDialog = new SongInfoDialog(width, height);
    }    /**
     * 获取歌曲信息对话框.
     * 
     * @return 歌曲信息对话框
     */
    public SongInfoDialog getSongInfoDialog() {
        return songInfoDialog;
    }

    /**
     * 显示窗口.
     */
    public static void showSongInfoTipDialog() {
        if (songInfoDialog != null) {
            isShow = true;
            if (!songInfoDialog.isShowing()) {
                songInfoDialog.setVisible(true);
            }
        }
    }

    /**
     * 隐藏歌曲信息窗口.
     */
    public static void hideSongInfoTipDialog() {
        if (songInfoDialog != null) {
            isShow = false;
            // 延迟关闭窗口，当鼠标移进下一个列表时，不用隐藏，直接修改位置显示。
            new Thread() {

                private static final int SLEEP_DURATION = 200;

                @Override
                public void run() {
                    try {
                        Thread.sleep(SLEEP_DURATION);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!isShow) {
                        songInfoDialog.setVisible(false);
                    }
                }

            }.start();
        }
    }
}
