package com.sparkle.widget.panel.songlist;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * 歌曲列表Item面板，该面板包含（ListViewHeadPanel，ListViewComPanel）
 * 
 * @author yuyi2003
 * 
 */
public class ListViewItemPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ListViewItemPanel() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setOpaque(false);
	}
}
