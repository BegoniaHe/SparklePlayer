package com.sparkle.test;

import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

import com.sparkle.util.FontsUtil;
import com.sparkle.util.FontFallbackUtil;

/**
 * 字体回退功能测试类
 * 用于验证中文歌名是否能够正确显示
 * 
 * @author yuyi2003
 */
public class FontFallbackTest {
    
    public static void main(String[] args) {
        // 创建测试窗口
        JFrame frame = new JFrame("字体回退功能测试");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        
        JPanel panel = new JPanel(new GridLayout(8, 1, 10, 10));
        
        // 测试不同语言的歌名
        String[] testSongs = {
            "Beautiful Love - 蔡健雅",  // 中英混合
            "志在千里~恋姫喚作百花王~ - 茶太",  // 中日混合
            "행복하길 바래 - 林亨柱",  // 韩文+中文
            "My Heart Will Go on - Celine Dion",  // 英文
            "My Destiny - Lyn",  // 英文
            "ทุกเช้า ทุกคืน ทุกวัน - โอ๊ค สมิทธิ์",  // 泰文
            "普通测试歌名",  // 纯中文
            "Test Song Name"  // 纯英文
        };
        
        // 测试每个歌名
        for (String songName : testSongs) {
            JLabel label = new JLabel(songName);
            
            // 使用我们的字体回退功能
            Font fallbackFont = FontsUtil.getBaseFont(14);
            label.setFont(fallbackFont);
            
            // 添加边框以便查看
            label.setBorder(javax.swing.BorderFactory.createTitledBorder("使用字体回退: " + fallbackFont.getName()));
            
            panel.add(label);
        }
        
        frame.add(panel, BorderLayout.CENTER);
        
        // 添加说明标签
        JLabel infoLabel = new JLabel("<html>" +
            "<h3>字体回退功能测试</h3>" +
            "<p>如果您能看到所有文字正确显示（包括中文、韩文、泰文等），</p>" +
            "<p>则说明字体回退功能工作正常。</p>" +
            "</html>");
        frame.add(infoLabel, BorderLayout.NORTH);
        
        frame.setVisible(true);
        
        // 输出字体信息到控制台
        System.out.println("=== 字体回退功能测试 ===");
        Font primaryFont = FontFallbackUtil.getPrimaryFont(14);
        Font fallbackFont = FontFallbackUtil.getFallbackFont(14);
        
        System.out.println("主字体: " + primaryFont.getName() + " (大小: " + primaryFont.getSize() + ")");
        System.out.println("回退字体: " + fallbackFont.getName() + " (大小: " + fallbackFont.getSize() + ")");
        
        // 测试特定字符的字体回退
        System.out.println("\n=== 字符字体回退测试 ===");
        String testChars = "蔡健雅행복Lyn普通ทุกเช้าTest";
        for (char c : testChars.toCharArray()) {
            boolean needsFallback = FontsUtil.needsFallback(c, 14);
            Font fontForChar = FontFallbackUtil.getFontForCharacter(c, 14);
            System.out.println("字符 '" + c + "' - 需要回退: " + needsFallback + 
                             ", 使用字体: " + fontForChar.getName());
        }
    }
}
