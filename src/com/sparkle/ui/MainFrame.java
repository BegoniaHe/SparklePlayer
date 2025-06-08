package com.sparkle.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.sparkle.common.BaseData;
import com.sparkle.common.Constants;
import com.sparkle.event.PanelMoveFrame;
import com.sparkle.logger.LoggerManage;

import com.sparkle.manage.MediaManage;
import com.sparkle.model.MessageIntent;
import com.sparkle.model.SongInfo;
import com.sparkle.model.SongMessage;
import com.sparkle.observable.ObserverManage;
import com.sparkle.observable.SparkleObserver;
import com.sparkle.util.DataUtil;

import com.sparkle.widget.panel.MainCenterPanel;
import com.sparkle.widget.panel.MainMenuPanel;
import com.sparkle.widget.panel.MainOperatePanel;
import com.sparkle.widget.panel.lrc.ManyLineLyricsView;
import com.sparkle.lyrics.model.LyricsLineInfo;
import com.sparkle.util.LyricsUtil;

import java.util.TreeMap;

/**
 * 主界面
 * 
 * @author yuyi2003
 * 
 */
public class MainFrame extends JFrame implements SparkleObserver, KeyListener {
    private static LoggerManage logger = LoggerManage.getZhangLogger();

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /**
     * 背景图片
     */
    private JLabel bgJLabel;
    /**
     * 窗口宽度
     */
    private int mainFrameWidth;    /**
     * 窗口高度
     */
    private int mainFrameHeight;
    
    /**
     * 双击检测相关变量
     */
    private long lastLeftArrowPress = 0;
    private long lastRightArrowPress = 0;
    private static final long DOUBLE_PRESS_INTERVAL = 500; // 双击间隔时间（毫秒）
    
    /**
     * 
     */
    private MainOperatePanel mainOperatePanel;
    private MainCenterPanel mainCenterPanel;public MainFrame() {
        init();// 初始化
        initComponent();// 初始化控件
        initSkin();// 初始化皮肤
        initKeyboardControl(); // 初始化键盘控制
        //
        setVisible(true);
        ObserverManage.getObserver().addObserver(this);
    }

    /**
     * 初始化
     */
    private void init() {
        // 定义窗口关闭事件
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitPlayer();
            }
        });

        this.setTitle(Constants.APPTITLE);
        this.setUndecorated(true);
        // 状态栏图标
        String iconFilePath = Constants.PATH_ICON + File.separator
                + BaseData.iconFileName;
        this.setIconImage(new ImageIcon(iconFilePath).getImage());

        // 设置基本的窗口数据
        Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();

        // 默认窗口宽度
        // mainFrameWidth = screenDimension.width / 5 * 3 + 100;
        // 默认窗口高度
        // mainFrameHeight = screenDimension.height / 4 * 3;
        
        // 默认窗口720p
        mainFrameWidth = 1280;
        mainFrameHeight = 720;

        // 计算最优窗口大小，保持16:9比例，默认720p
        double aspectRatio = 16.0 / 9.0;
        
        // 获取可用屏幕区域（排除任务栏等）
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(
                getGraphicsConfiguration());
        int availableWidth = screenDimension.width - screenInsets.left - screenInsets.right;
        int availableHeight = screenDimension.height - screenInsets.top - screenInsets.bottom;
        
        // 默认使用720p，但确保不超过屏幕的80%
        int maxWidth = (int) (availableWidth * 0.8);
        int maxHeight = (int) (availableHeight * 0.8);
        
        // 根据可用空间计算最佳尺寸
        if (mainFrameWidth > maxWidth || mainFrameHeight > maxHeight) {
            // 根据宽度和高度限制计算缩放比例
            double scaleByWidth = (double) maxWidth / mainFrameWidth;
            double scaleByHeight = (double) maxHeight / mainFrameHeight;
            double scale = Math.min(scaleByWidth, scaleByHeight);
            
            mainFrameWidth = (int) (mainFrameWidth * scale);
            mainFrameHeight = (int) (mainFrameHeight * scale);
        }
        
        // 确保保持16:9比例
        int heightByWidth = (int) (mainFrameWidth / aspectRatio);
        int widthByHeight = (int) (mainFrameHeight * aspectRatio);
        
        if (heightByWidth <= maxHeight) {
            mainFrameHeight = heightByWidth;
        } else {
            mainFrameWidth = widthByHeight;
        }
        
        // 设置最小尺寸限制为 360p
        int minWidth = 640;
        int minHeight = (int) (minWidth / aspectRatio);
        
        if (mainFrameWidth < minWidth) {
            mainFrameWidth = minWidth;
            mainFrameHeight = minHeight;
        }

        this.setSize(mainFrameWidth, mainFrameHeight);
        this.setLocationRelativeTo(null);
    }

    /**
     * 初始化组件
     */
    private void initComponent() {
        this.getContentPane().setLayout(null);

        // 主界面标题栏菜单面板
        int mmPanelWidth = mainFrameWidth;
        int mmPanelHeight = 55;
        int mmPanelX = 0;
        int mmPanelY = 0;
        MainMenuPanel mainMenuPanel = new MainMenuPanel(mmPanelWidth,
                mmPanelHeight, this);
        new PanelMoveFrame(mainMenuPanel, this);
        mainMenuPanel
                .setBounds(mmPanelX, mmPanelY, mmPanelWidth, mmPanelHeight);// 位置
        // 底部操作面板
        int moPanelWidth = mainFrameWidth;
        int moPanelHeight = 75;
        int moPanelX = 0;
        int moPanelY = mainFrameHeight - moPanelHeight;        mainOperatePanel = new MainOperatePanel(moPanelWidth,
                moPanelHeight, this);
        mainOperatePanel.setBounds(moPanelX, moPanelY, moPanelWidth,
                moPanelHeight);
        new PanelMoveFrame(mainOperatePanel, this);

        // 界面中部面板
        int mcPanelWidth = mainFrameWidth;
        int mcPanelHeight = mainFrameHeight - mainMenuPanel.getHeight()
                - mainOperatePanel.getHeight();
        int mcPanelX = 0;
        int mcPanelY = mainMenuPanel.getHeight() - 0;        mainCenterPanel = new MainCenterPanel(mainOperatePanel, this,
                mcPanelWidth, mcPanelHeight);
        mainCenterPanel.setBounds(mcPanelX, mcPanelY, mcPanelWidth,
                mcPanelHeight);

        //
        this.getContentPane().add(mainCenterPanel);
        this.getContentPane().add(mainOperatePanel);
        this.getContentPane().add(mainMenuPanel);
    }

    /**
     * 初始化皮肤
     */
    private void initSkin() {

        bgJLabel = new JLabel(getSkinImageIcon());// 把背景图片显示在一个标签里面
        // 把标签的大小位置设置为图片刚好填充整个面板
        bgJLabel.setBounds(0, 0, this.getWidth(), this.getHeight());
        this.getContentPane().add(bgJLabel);
    }

    /**
     * 获取皮肤图片
     * 
     * @return
     */
    private ImageIcon getSkinImageIcon() {
        String backgroundFilePath = Constants.PATH_SKIN + File.separator
                + BaseData.bGroundFileName;
        ImageIcon background = new ImageIcon(backgroundFilePath);// 背景图片
        background.setImage(background.getImage().getScaledInstance(
                this.getWidth(), this.getHeight(), Image.SCALE_SMOOTH));
        return background;
    }

    /**
     * 中间面板
     * @return
     */
    public MainCenterPanel getMainCenterPanel() {
        return mainCenterPanel;
    }

    /**
     * 操作面板 
     * @return
     */
    public MainOperatePanel getMainOperatePanel() {
        return mainOperatePanel;
    }

    /**
     * 退出播放器
     */    public void exitPlayer() {
        this.setVisible(false);
        MediaManage.getMediaManage().stopToPlay();
        logger.info("准备退出播放器");
        DataUtil.saveData();
        logger.info("退出成功");
        System.exit(0);
    }
    @Override
    public void update(Object source, Object data) {
        updateUI(data);
    }

    private void updateUI(Object data) {
        if (data instanceof MessageIntent) {
            MessageIntent messageIntent = (MessageIntent) data;
            if (messageIntent.getAction().equals(MessageIntent.FRAME_NORMAL)) {

                setAlwaysOnTop(true);
                setExtendedState(Frame.NORMAL);
                setAlwaysOnTop(false);
                setVisible(true);

            }
        } else if (data instanceof SongMessage) {
            SongMessage songMessage = (SongMessage) data;
            if (songMessage.getType() == SongMessage.INITMUSIC) {
                SongInfo mSongInfo = songMessage.getSongInfo();
                if (mSongInfo != null) {
                    setTitle(mSongInfo.getDisplayName());
                }            }
        }
    }    /**
     * 初始化键盘控制
     */
    private void initKeyboardControl() {
        // 设置窗口可获得焦点
        this.setFocusable(true);
        this.addKeyListener(this);
        
        // 确保窗口获得焦点时能响应键盘事件
        this.requestFocus();
        
        // 添加窗口焦点监听器，确保窗口激活时能响应键盘
        this.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                requestFocus();
            }
            
            @Override
            public void windowLostFocus(java.awt.event.WindowEvent e) {
                // 窗口失去焦点时不需要特殊处理
            }
        });
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }    @Override    public void keyPressed(KeyEvent e) {
        // 处理按键事件
        int keyCode = e.getKeyCode();
        switch (keyCode) {
        case KeyEvent.VK_SPACE:
            // 空格键控制播放/暂停
            togglePlayPause();
            break;        case KeyEvent.VK_LEFT:
            // 左箭头键：检测双击
            handleLeftArrow();
            break;
        case KeyEvent.VK_RIGHT:
            // 右箭头键：检测双击
            handleRightArrow();
            break;
        case KeyEvent.VK_UP:
            // 上箭头键增加音量
            adjustVolume(5);
            break;
        case KeyEvent.VK_DOWN:
            // 下箭头键减少音量
            adjustVolume(-5);
            break;        case KeyEvent.VK_M:
            // M键切换静音
            toggleMute();
            break;
        case KeyEvent.VK_T:
            // T键切换歌词翻译
            toggleLyricsTranslation();
            break;
        case KeyEvent.VK_ESCAPE:
            // ESC键退出播放器
            exitPlayer();
            break;
        }
    }@Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    /**
     * 切换播放/暂停状态
     */
    private void togglePlayPause() {
        if (MediaManage.getMediaManage().getPlayStatus() == MediaManage.PLAYING) {
            // 当前正在播放，发送暂停消息
            SongMessage songMessage = new SongMessage();
            songMessage.setType(SongMessage.PAUSEMUSIC);
            ObserverManage.getObserver().setMessage(songMessage);
        } else {
            // 当前暂停或停止，发送播放消息
            SongMessage songMessage = new SongMessage();
            songMessage.setType(SongMessage.PLAYMUSIC);
            ObserverManage.getObserver().setMessage(songMessage);
        }
    }

    /**
     * 播放上一首
     */
    private void playPrevious() {
        SongMessage songMessage = new SongMessage();
        songMessage.setType(SongMessage.PREMUSIC);
        ObserverManage.getObserver().setMessage(songMessage);
    }

    /**
     * 播放下一首
     */
    private void playNext() {
        SongMessage songMessage = new SongMessage();
        songMessage.setType(SongMessage.NEXTMUSIC);
        ObserverManage.getObserver().setMessage(songMessage);
    }    /**
     * 调整音量
     * @param delta 音量变化量（正数增加，负数减少）
     */
    private void adjustVolume(int delta) {
        int currentVolume = BaseData.volumeSize;
        int newVolume = Math.max(0, Math.min(100, currentVolume + delta));
        
        if (newVolume != currentVolume) {
            BaseData.volumeSize = newVolume;
            
            // 同步更新界面上的音量滑块
            if (mainOperatePanel != null) {
                mainOperatePanel.getVolumeSlider().setValue(newVolume);
            }
            
            // 发送音量变化消息
            MessageIntent messageIntent = new MessageIntent();
            messageIntent.setAction(MessageIntent.PLAYERVOLUME);
            ObserverManage.getObserver().setMessage(messageIntent);
        }
    }

    /**
     * 切换静音状态
     */    private void toggleMute() {
        if (BaseData.volumeSize == 0) {
            // 当前静音，恢复到50%音量
            BaseData.volumeSize = 50;
        } else {
            // 当前有声音，设为静音
            BaseData.volumeSize = 0;
        }
        
        // 同步更新界面上的音量滑块
        if (mainOperatePanel != null) {
            mainOperatePanel.getVolumeSlider().setValue(BaseData.volumeSize);
        }
        
        // 发送音量变化消息
        MessageIntent messageIntent = new MessageIntent();
        messageIntent.setAction(MessageIntent.PLAYERVOLUME);
        ObserverManage.getObserver().setMessage(messageIntent);
    }
      /**
     * 跳转到上一句歌词
     */
    private void navigateToPreviousLyrics() {
        try {
            if (mainCenterPanel != null && mainCenterPanel.getLyricsPanel() != null) {
                ManyLineLyricsView lyricsView = mainCenterPanel.getLyricsPanel().getManyLineLyricsView();
                if (lyricsView != null && lyricsView.getLyricsLineTreeMap() != null) {
                    TreeMap<Integer, LyricsLineInfo> lyricsTreeMap = lyricsView.getLyricsLineTreeMap();
                    if (!lyricsTreeMap.isEmpty()) {
                        int currentLine = 0;
                        // 获取当前歌词行号（从当前播放进度计算）
                        if (lyricsView.getLyricsUtil() != null) {
                            currentLine = lyricsView.getLyricsUtil().getLineNumber(lyricsTreeMap, 
                                (int) MediaManage.getMediaManage().getSongInfo().getPlayProgress());
                        }
                        
                        // 跳转到上一行
                        int previousLine = Math.max(0, currentLine - 1);
                        if (previousLine < lyricsTreeMap.size()) {
                            // 获取上一行歌词的开始时间
                            int startTime = lyricsTreeMap.get(previousLine).getStartTime();
                            
                            // 发送跳转消息
                            SongMessage songMessage = new SongMessage();
                            songMessage.setType(SongMessage.SEEKTOMUSIC);
                            songMessage.setProgress(startTime);
                            ObserverManage.getObserver().setMessage(songMessage);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 如果歌词导航失败，回退到原有的上一曲功能
            playPrevious();
        }
    }
      /**
     * 跳转到下一句歌词
     */
    private void navigateToNextLyrics() {
        try {
            if (mainCenterPanel != null && mainCenterPanel.getLyricsPanel() != null) {
                ManyLineLyricsView lyricsView = mainCenterPanel.getLyricsPanel().getManyLineLyricsView();
                if (lyricsView != null && lyricsView.getLyricsLineTreeMap() != null) {
                    TreeMap<Integer, LyricsLineInfo> lyricsTreeMap = lyricsView.getLyricsLineTreeMap();
                    if (!lyricsTreeMap.isEmpty()) {
                        int currentLine = 0;
                        // 获取当前歌词行号（从当前播放进度计算）
                        if (lyricsView.getLyricsUtil() != null) {
                            currentLine = lyricsView.getLyricsUtil().getLineNumber(lyricsTreeMap, 
                                (int) MediaManage.getMediaManage().getSongInfo().getPlayProgress());
                        }
                        
                        // 跳转到下一行
                        int nextLine = Math.min(lyricsTreeMap.size() - 1, currentLine + 1);
                        if (nextLine >= 0 && nextLine < lyricsTreeMap.size()) {
                            // 获取下一行歌词的开始时间
                            int startTime = lyricsTreeMap.get(nextLine).getStartTime();
                            
                            // 发送跳转消息
                            SongMessage songMessage = new SongMessage();
                            songMessage.setType(SongMessage.SEEKTOMUSIC);
                            songMessage.setProgress(startTime);
                            ObserverManage.getObserver().setMessage(songMessage);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 如果歌词导航失败，回退到原有的下一曲功能
            playNext();
        }
    }
    
    /**
     * 处理左箭头键按下事件（检测双击）
     */
    private void handleLeftArrow() {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastLeftArrowPress < DOUBLE_PRESS_INTERVAL) {
            // 双击检测到，播放上一首
            playPrevious();
            lastLeftArrowPress = 0; // 重置时间
        } else {
            // 单击，跳转到上一句歌词
            navigateToPreviousLyrics();
            lastLeftArrowPress = currentTime;
        }
    }
    
    /**
     * 处理右箭头键按下事件（检测双击）
     */
    private void handleRightArrow() {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastRightArrowPress < DOUBLE_PRESS_INTERVAL) {
            // 双击检测到，播放下一首
            playNext();
            lastRightArrowPress = 0; // 重置时间
        } else {
            // 单击，跳转到下一句歌词
            navigateToNextLyrics();
            lastRightArrowPress = currentTime;
        }
    }    /**
     * 获取当前歌词翻译显示状态
     */
    private boolean isTranslationCurrentlyShowing() {
        try {
            if (mainCenterPanel != null && mainCenterPanel.getLyricsPanel() != null) {
                ManyLineLyricsView lyricsView = mainCenterPanel.getLyricsPanel().getManyLineLyricsView();
                if (lyricsView != null) {
                    // 通过反射或者其他方式获取当前状态，由于没有getter方法，我们通过检查翻译按钮状态
                    if (mainOperatePanel != null) {
                        // 检查控制栏的翻译按钮状态来判断当前是否显示翻译
                        // 这里我们假设如果显示翻译按钮是可见的，则当前显示翻译
                        return mainOperatePanel.isTranslationShowing();
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to check translation status: " + e.getMessage());
        }
        return false;
    }

    /**
     * 切换歌词翻译显示状态
     */
    private void toggleLyricsTranslation() {
        try {
            if (mainCenterPanel != null && mainCenterPanel.getLyricsPanel() != null) {
                ManyLineLyricsView lyricsView = mainCenterPanel.getLyricsPanel().getManyLineLyricsView();
                if (lyricsView != null && lyricsView.getLyricsUtil() != null) {
                    // 检查是否有翻译歌词
                    int extraLrcType = lyricsView.getLyricsUtil().getExtraLrcType();
                    
                    if (extraLrcType == LyricsUtil.TRANSLATE_LRC || 
                        extraLrcType == LyricsUtil.TRANSLATE_AND_TRANSLITERATION_LRC) {
                        
                        // 获取当前翻译显示状态
                        boolean currentlyShowing = isTranslationCurrentlyShowing();
                        
                        // 切换状态：如果当前显示翻译，则隐藏；如果当前隐藏，则显示
                        if (currentlyShowing) {
                            // 当前显示翻译，切换为隐藏
                            lyricsView.setExtraLrcStatus(ManyLineLyricsView.NOSHOWEXTRALRC);
                            // 更新控制栏按钮状态为隐藏翻译
                            updateTranslationButtons(false);
                        } else {
                            // 当前隐藏翻译，切换为显示
                            lyricsView.setExtraLrcStatus(ManyLineLyricsView.SHOWTRANSLATELRC);
                            // 更新控制栏按钮状态为显示翻译
                            updateTranslationButtons(true);
                        }
                        
                        // 强制刷新歌词显示
                        lyricsView.repaint();
                    }
                }
            }
        } catch (Exception e) {
            // 如果切换失败，记录错误但不中断程序
            logger.debug("Failed to toggle lyrics translation: " + e.getMessage());
        }
    }
    
    /**
     * 更新翻译按钮状态
     * @param showingTranslation 是否正在显示翻译
     */
    private void updateTranslationButtons(boolean showingTranslation) {
        try {
            if (mainOperatePanel != null) {
                mainOperatePanel.updateTranslationButtonState(showingTranslation);
            }
        } catch (Exception e) {
            logger.debug("Failed to update translation buttons: " + e.getMessage());
        }
    }

}
