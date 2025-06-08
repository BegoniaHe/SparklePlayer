package com.sparkle.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
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

/**
 * 主界面
 * 
 * @author yuyi2003
 * 
 */
public class MainFrame extends JFrame implements SparkleObserver {
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
	private int mainFrameWidth;
	/**
	 * 窗口高度
	 */
	private int mainFrameHeight;	/**
	 * 
	 */
	private MainOperatePanel mainOperatePanel;
	private MainCenterPanel mainCenterPanel;	public MainFrame() {
		init();// 初始化
		initComponent();// 初始化控件
		initSkin();// 初始化皮肤
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
		int moPanelY = mainFrameHeight - moPanelHeight;		mainOperatePanel = new MainOperatePanel(moPanelWidth,
				moPanelHeight, this);
		mainOperatePanel.setBounds(moPanelX, moPanelY, moPanelWidth,
				moPanelHeight);
		new PanelMoveFrame(mainOperatePanel, this);

		// 界面中部面板
		int mcPanelWidth = mainFrameWidth;
		int mcPanelHeight = mainFrameHeight - mainMenuPanel.getHeight()
				- mainOperatePanel.getHeight();
		int mcPanelX = 0;
		int mcPanelY = mainMenuPanel.getHeight() - 0;		mainCenterPanel = new MainCenterPanel(mainOperatePanel, this,
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
	 */	public void exitPlayer() {
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
				}			}
		}
	}
}
