package com.sparkle.widget.dialog;

import java.awt.Cursor;
import java.awt.Color;
import java.awt.Dimension;
// import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import com.sparkle.common.BaseData;
import com.sparkle.model.MessageIntent;
import com.sparkle.observable.ObserverManage;
import com.sparkle.observable.SparkleObserver;
import com.sparkle.widget.panel.des.DesOperatePanel;
import com.sparkle.widget.panel.lrc.FloatLyricsView;

/**
 * 桌面歌词窗口
 * 
 * @author yuyi2003
 * 
 */
public class DesLrcDialog extends JDialog implements SparkleObserver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 高度
	 */
	private int mHeight = 0;
	/**
	 * 宽度
	 */
	private int mWidth = 0;

	private int mY = 0;

	// private int maxY = 0;

	// private int g_mouseX = 0, g_mouseY = 0; /* 鼠标的坐标 */
	/**
	 * 操作面板
	 */
	private DesOperatePanel desOperatePanel;

	private DesLrcDialogMouseListener desLrcDialogMouseListener = new DesLrcDialogMouseListener();

	/**
	 * 歌词面板
	 */
	private FloatLyricsView floatLyricsView;

	public DesLrcDialog() {

		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		mHeight = screenDimension.height / 6 - 10;
		mWidth = screenDimension.width;
		mY = screenDimension.height;

		init();
		initComponent();
		this.setSize(mWidth, mHeight);
		this.setMinimumSize(new Dimension(mWidth / 3 * 2, mHeight));
		this.setMaximumSize(new Dimension(mWidth, mHeight));
		this.setUndecorated(true);
		this.setAlwaysOnTop(true);
		this.setBackground(new Color(0, 0, 0, 0));// 关键代码！ 设置窗体透明

		this.addMouseListener(desLrcDialogMouseListener);
		this.addMouseMotionListener(desLrcDialogMouseListener);
		ObserverManage.getObserver().addObserver(this);
	}

	private void init() {

		// 获取屏幕边界
		// Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
		// 取得底部边界高度，即任务栏高度
		// int taskHeight = screenInsets.bottom;
		// maxY = mY - mHeight - taskHeight;

	}

	private void initComponent() {
		this.getContentPane().setLayout(null);

		int width = mHeight / 5 * 17 + 10 * 17;

		desOperatePanel = new DesOperatePanel(width, mHeight / 5,
				desLrcDialogMouseListener, this);
		desOperatePanel.setBounds((mWidth - width) / 2, 0, width, mHeight / 5);
		desOperatePanel.setVisible(false);

		// 设置字体
		BaseData.desktopLrcFontMinSize = (mHeight - mHeight / 5) / 4;
		BaseData.desktopLrcFontMaxSize = (mHeight - mHeight / 5) / 3;
		if (BaseData.desktopLrcFontSize == 0) {
			BaseData.desktopLrcFontSize = BaseData.desktopLrcFontMinSize;
		}

		floatLyricsView = new FloatLyricsView(mWidth, mHeight - mHeight / 5,
				desLrcDialogMouseListener);
		floatLyricsView.setExtraLyricsListener(desOperatePanel
				.getExtraLyricsListener());
		floatLyricsView.setBounds(0,
				desOperatePanel.getY() + desOperatePanel.getHeight(), mWidth,
				mHeight - mHeight / 5);

		this.getContentPane().add(desOperatePanel);
		this.getContentPane().add(floatLyricsView);
	}

	public int getmHeight() {
		return mHeight;
	}

	public int getmWidth() {
		return mWidth;
	}

	public int getmY() {
		return mY;
	}

	public DesOperatePanel getDesOperatePanel() {
		return desOperatePanel;
	}

	public FloatLyricsView getFloatLyricsView() {
		return floatLyricsView;
	}

	private boolean isDragged = false;

	/**
	 * 初始化桌面歌词锁事件
	 */
	private void initLock() {
		if (!BaseData.desLrcIsLock) {
		} else {
			desOperatePanel.setVisible(false);
			floatLyricsView.setShow(false);
			desOperatePanel.repaint();
		}

	}

	/**
	 * 窗口鼠标事件
	 * 
	 * @author yuyi2003
	 * 
	 */
	private class DesLrcDialogMouseListener implements MouseInputListener {

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			/* 获取鼠标的位置 */
			// g_mouseX = e.getX();
			// g_mouseY = e.getY();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			setCursor(null);
			// if (!floatLyricsView.getEnter() && !desOperatePanel.getEnter()) {
			// desOperatePanel.setVisible(false);
			// floatLyricsView.setEnter(false);
			// desOperatePanel.setEnter(false);
			// }
			isDragged = false;
			mouseExited(e);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// desOperatePanel.setVisible(true);
			// floatLyricsView.setEnter(true);
			if (floatLyricsView.getEnter()) {
				desOperatePanel.setVisible(true);
			} else if (desOperatePanel.getEnter()) {
				desOperatePanel.setVisible(true);
				floatLyricsView.setShow(true);
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if (!isDragged) {
				// desOperatePanel.setVisible(false);
				// floatLyricsView.setEnter(false);
				// desOperatePanel.setEnter(false);
				if (floatLyricsView.getEnter() == false) {
					if (desOperatePanel.getEnter()) {
						desOperatePanel.setVisible(true);
						floatLyricsView.setShow(true);
					} else {
						desOperatePanel.setVisible(false);
						floatLyricsView.setShow(false);
					}
				} else if (desOperatePanel.getEnter() == false) {
					if (floatLyricsView.getEnter()) {
						desOperatePanel.setVisible(true);
						floatLyricsView.setShow(true);
					} else {
						desOperatePanel.setVisible(false);
						floatLyricsView.setShow(false);
					}
				} else {
					desOperatePanel.setVisible(false);
					floatLyricsView.setShow(false);
				}
			} else {
				desOperatePanel.setVisible(true);
				floatLyricsView.setShow(true);
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {

			/* 鼠标左键托动事件 */
			// if (e.getModifiers() == MouseEvent.BUTTON1_MASK) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				// /* 重新定义窗口的位置 */
				/*
				if (getY() + e.getY() - g_mouseY < 0)
					setLocation(getX(), 0);
				else if (getY() + e.getY() - g_mouseY > maxY)

					setLocation(getX(), maxY);
				else
					setLocation(getX(), getY() + e.getY() - g_mouseY);
				*/
			}

			isDragged = true;

		}

		@Override
		public void mouseMoved(MouseEvent e) {

		}

	}
	@Override
	public void update(Object source, final Object data) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (data instanceof MessageIntent) {
					MessageIntent messageIntent = (MessageIntent) data;
					if (messageIntent.getAction().equals(
							MessageIntent.LOCKDESLRC)) {
						initLock();
					}
				}
			}
		});
	}
}