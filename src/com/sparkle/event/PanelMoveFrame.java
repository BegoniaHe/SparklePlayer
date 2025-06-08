package com.sparkle.event;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

/**
 * 窗口移动事件处理类.
 * 
 * @author yuyi2003
 */
public class PanelMoveFrame {
    /**
     * 操作面板.
     */
    private final JPanel linePanel;
    
    /**
     * 要移动的窗口.
     */
    private final JFrame moveFrame;

    /**
     * 构造函数.
     * 
     * @param panel 操作面板
     * @param moveframe 要移动的窗口
     */
    public PanelMoveFrame(final JPanel panel, final JFrame moveframe) {
        this.moveFrame = moveframe;
        this.linePanel = panel;
        // 鼠标事件处理类
        final MouseEventListener mouseListener = new MouseEventListener(moveFrame);
        linePanel.addMouseListener(mouseListener);
        linePanel.addMouseMotionListener(mouseListener);
    }    /**
     * 鼠标事件处理内部类.
     */
    class MouseEventListener implements MouseInputListener {

        /**
         * 鼠标原始位置.
         */
        private final Point origin;
        
        /**
         * 鼠标拖拽想要移动的目标组件.
         */
        private final JFrame frame;

        /**
         * 构造函数.
         * 
         * @param frame 要移动的窗口
         */
        MouseEventListener(final JFrame frame) {
            this.frame = frame;
            origin = new Point();
        }

        /**
         * 鼠标点击事件.
         * 
         * @param e 鼠标事件
         */
        public void mouseClicked(final MouseEvent e) {
            // 空实现
        }

        /**
         * 记录鼠标按下时的点.
         * 
         * @param e 鼠标事件
         */
        public void mousePressed(final MouseEvent e) {
            origin.x = e.getX();
            origin.y = e.getY();
        }

        /**
         * 鼠标释放事件.
         * 
         * @param e 鼠标事件
         */
        public void mouseReleased(final MouseEvent e) {
            moveFrame.setCursor(null);
        }

        /**
         * 鼠标移进标题栏时，设置鼠标图标为移动图标.
         * 
         * @param e 鼠标事件
         */
        public void mouseEntered(final MouseEvent e) {
            // this.frame
            // .setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }

        /**
         * 鼠标移出标题栏时，设置鼠标图标为默认指针.
         * 
         * @param e 鼠标事件
         */
        public void mouseExited(final MouseEvent e) {
            // this.frame.setCursor(Cursor
            // .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        /**
         * 鼠标在标题栏拖拽时，设置窗口的坐标位置.
         * 窗口新的坐标位置 = 移动前坐标位置+（鼠标指针当前坐标-鼠标按下时指针的位置）
         * 
         * @param e 鼠标事件
         */
        public void mouseDragged(final MouseEvent e) {
            moveFrame.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            final Point p = this.frame.getLocation();
            this.frame.setLocation(p.x + (e.getX() - origin.x), p.y
                    + (e.getY() - origin.y));
        }

        /**
         * 鼠标移动事件.
         * 
         * @param e 鼠标事件
         */
        public void mouseMoved(final MouseEvent e) {
            // 空实现
        }

    }

}
