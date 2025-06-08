package com.sparkle.test;

import java.awt.*;
import javax.swing.*;
import java.text.AttributedString;

import com.sparkle.util.FontsUtil;
import com.sparkle.util.DrawingUtil;

/**
 * 综合字体回退功能测试
 * 验证歌名标签和歌词显示的字体回退与居中功能
 * 
 * @author yuyi2003
 */
public class ComprehensiveFontTest extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    private static final String[] TEST_SONGS = {
        "蔡健雅 - Beautiful Love",    // 中英混合
        "행복하길 바래 - 林亨柱",       // 韩文+中文
        "ทุกเช้า ทุกคืน ทุกวัน",      // 纯泰文
        "志在千里~恋姫喚作百花王~ - 茶太", // 中日混合
        "My Heart Will Go on - Celine Dion", // 纯英文
        "普通的中文歌曲测试"            // 纯中文
    };
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int panelWidth = getWidth();
        int y = 40;
        
        // 标题
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g2d.drawString("SparklePlayer 字体回退功能综合测试", 10, 25);
        
        for (String song : TEST_SONGS) {
            // 1. 模拟歌名标签显示（如ListViewComItemPanel中的歌名）
            g2d.setColor(Color.BLUE);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            g2d.drawString("歌名标签:", 10, y);
            
            // 使用字体回退的字体
            Font songFont = FontsUtil.getBaseFont(12);
            g2d.setFont(songFont);
            g2d.setColor(Color.BLACK);
            
            // 计算宽度并居中（模拟MainOperatePanel中的歌名显示）
            int oldWidth = g2d.getFontMetrics().stringWidth(song); // 旧方法
            int newWidth = DrawingUtil.getTextWidthWithFallback(g2d, song, 12); // 新方法
            
            int centerX = (panelWidth - newWidth) / 2;
            
            // 使用字体回退绘制歌名
            AttributedString attrString = FontsUtil.createFallbackString(song, 12);
            g2d.drawString(attrString.getIterator(), centerX, y + 15);
            
            y += 20;
            
            // 2. 显示宽度计算对比
            g2d.setColor(Color.RED);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
            g2d.drawString(String.format("旧方法宽度: %dpx, 新方法宽度: %dpx, 差异: %dpx", 
                         oldWidth, newWidth, Math.abs(newWidth - oldWidth)), 10, y);
            
            // 3. 绘制居中参考线
            g2d.setColor(Color.GREEN);
            g2d.drawLine(panelWidth / 2, y - 25, panelWidth / 2, y - 15); // 中心线
            g2d.setColor(Color.BLUE);
            g2d.drawLine(centerX, y - 20, centerX + newWidth, y - 20); // 文本边界
            
            y += 30;
        }
        
        // 显示说明
        y += 20;
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g2d.drawString("绿色线: 面板中心线", 10, y);
        g2d.drawString("蓝色线: 文本边界（使用字体回退计算）", 10, y + 15);
        g2d.drawString("如果文本正确居中且所有字符都能显示，说明字体回退功能工作正常", 10, y + 30);
        
        g2d.dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("SparklePlayer 字体回退功能综合测试");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 500);
            frame.setLocationRelativeTo(null);
            
            ComprehensiveFontTest panel = new ComprehensiveFontTest();
            panel.setBackground(Color.WHITE);
            frame.add(panel);
            
            frame.setVisible(true);
            
            // 控制台输出测试结果
            System.out.println("=== SparklePlayer 字体回退功能综合测试 ===");
            System.out.println("测试项目:");
            System.out.println("1. 歌名标签字体回退 (ListViewComItemPanel)");
            System.out.println("2. 播放面板歌名字体回退 (MainOperatePanel)");
            System.out.println("3. 歌曲信息对话框字体回退 (SongInfoDialog)");
            System.out.println("4. 歌词显示字体回退 (FloatLyricsView)");
            System.out.println("5. 多行歌词字体回退 (ManyLineLyricsView)");
            System.out.println("6. 文本宽度计算修复 (DrawingUtil)");
            System.out.println();
            
            Font primaryFont = FontsUtil.getBaseFont(12);
            System.out.println("当前字体配置:");
            System.out.println("主字体: " + primaryFont.getName());
            System.out.println("回退字体: " + FontsUtil.getFallbackFont(12).getName());
            System.out.println();
            
            // 测试字符支持情况
            System.out.println("字符支持测试:");
            String testText = "蔡健雅행복ทุกTest";
            for (char c : testText.toCharArray()) {
                boolean needsFallback = FontsUtil.needsFallback(c, 12);
                Font charFont = FontsUtil.getFontForCharacter(c, 12);
                System.out.printf("'%c' - 需要回退: %s, 使用字体: %s%n", 
                                c, needsFallback ? "是" : "否", charFont.getName());
            }
            
            System.out.println("\n=== 测试完成 ===");
            System.out.println("如果界面中所有歌名都能正确显示且居中对齐，说明字体回退功能修复成功！");
        });
    }
}
