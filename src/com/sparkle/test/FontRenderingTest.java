package com.sparkle.test;

import java.awt.*;
import javax.swing.*;
import java.text.AttributedString;

import com.sparkle.util.FontsUtil;

/**
 * 字体回退渲染问题诊断测试
 * 
 * @author yuyi2003
 */
public class FontRenderingTest extends JPanel {
    
    private static final String[] TEST_TEXTS = {
        "蔡健雅",      // 中文
        "행복하길",     // 韩文 
        "ทุกเช้า",     // 泰文
        "Beautiful",   // 英文
        "蔡행ทุ"       // 混合
    };
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int y = 30;
        
        for (String text : TEST_TEXTS) {
            // 1. 使用主字体直接绘制
            Font primaryFont = FontsUtil.getBaseFont(16);
            g2d.setFont(primaryFont);
            g2d.setColor(Color.BLACK);
            g2d.drawString("主字体绘制: ", 10, y);
            g2d.drawString(text, 120, y);
            
            y += 25;
            
            // 2. 使用AttributedString绘制（字体回退）
            g2d.setColor(Color.BLUE);
            g2d.drawString("字体回退绘制: ", 10, y);
            AttributedString attrString = FontsUtil.createFallbackString(text, 16);
            g2d.drawString(attrString.getIterator(), 120, y);
            
            y += 25;
            
            // 3. 显示字符分析
            g2d.setColor(Color.RED);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            StringBuilder analysis = new StringBuilder("字符分析: ");
            for (char c : text.toCharArray()) {
                boolean needsFallback = !primaryFont.canDisplay(c);
                analysis.append(c).append(needsFallback ? "(回退) " : "(主) ");
            }
            g2d.drawString(analysis.toString(), 10, y);
            
            y += 40;
        }
        
        g2d.dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("字体回退渲染测试");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 500);
            frame.setLocationRelativeTo(null);
            
            FontRenderingTest panel = new FontRenderingTest();
            panel.setBackground(Color.WHITE);
            frame.add(panel);
            
            frame.setVisible(true);
            
            // 控制台输出详细信息
            System.out.println("=== 字体回退渲染测试 ===");
            Font primaryFont = FontsUtil.getBaseFont(16);
            System.out.println("主字体: " + primaryFont.getName());
            System.out.println("主字体文件: " + primaryFont.getFontName());
            
            for (String text : TEST_TEXTS) {
                System.out.println("\n测试文本: " + text);
                for (char c : text.toCharArray()) {
                    boolean canDisplay = primaryFont.canDisplay(c);
                    System.out.println("  字符 '" + c + "' (Unicode: U+" + 
                        Integer.toHexString(c).toUpperCase() + 
                        ") - 主字体支持: " + canDisplay);
                }
            }
        });
    }
}
