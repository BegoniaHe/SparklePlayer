package com.sparkle.widget.label;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;
import javax.swing.JLabel;
import com.sparkle.util.FontFallbackUtil;

/**
 * 支持字体回退的自定义JLabel
 * 确保多语言文本能够正确显示
 * 
 * @author yuyi2003
 */
public class FontFallbackLabel extends JLabel {
    
    private static final long serialVersionUID = 1L;
    
    public FontFallbackLabel() {
        super();
    }
    
    public FontFallbackLabel(String text) {
        super(text);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        
        try {
            // 启用抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            String text = getText();
            if (text == null || text.isEmpty()) {
                super.paintComponent(g);
                return;
            }
            
            Font baseFont = getFont();
            if (baseFont == null) {
                super.paintComponent(g);
                return;
            }
            
            // 设置颜色
            g2d.setColor(getForeground());
            
            // 创建带字体回退的AttributedString
            AttributedString attributedString = FontFallbackUtil.createFallbackString(text, baseFont.getSize());
            
            // 计算文本位置 - 使用简单的FontMetrics计算
            FontMetrics fm = g2d.getFontMetrics(baseFont);
            Rectangle2D bounds = fm.getStringBounds(text, g2d);
                
            int x = 0;
            int y = 0;
            
            // 处理水平对齐
            int horizontalAlignment = getHorizontalAlignment();
            if (horizontalAlignment == CENTER) {
                x = (getWidth() - (int)bounds.getWidth()) / 2;
            } else if (horizontalAlignment == RIGHT) {
                x = getWidth() - (int)bounds.getWidth() - getInsets().right;
            } else {
                x = getInsets().left;
            }
            
            // 处理垂直对齐
            int verticalAlignment = getVerticalAlignment();
            if (verticalAlignment == CENTER) {
                y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            } else if (verticalAlignment == BOTTOM) {
                y = getHeight() - fm.getDescent() - getInsets().bottom;
            } else {
                y = fm.getAscent() + getInsets().top;
            }
            
            // 绘制带字体回退的文本
            g2d.drawString(attributedString.getIterator(), x, y);
            
        } finally {
            g2d.dispose();
        }
    }
}
