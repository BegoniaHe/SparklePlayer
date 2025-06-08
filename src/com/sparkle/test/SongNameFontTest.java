package com.sparkle.test;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.sparkle.widget.label.FontFallbackLabel;

/**
 * 测试歌名标签的字体回退功能
 * 
 * @author yuyi2003
 */
public class SongNameFontTest {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }
    
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("歌名字体回退测试");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.BLACK);
        
        // 测试不同语言的歌名
        String[] testSongs = {
            "蔡健雅 - Beautiful Love",
            "茶太 - 志在千里~恋姫喚作百花王~", 
            "林亨柱 - 행복하길 바래",
            "Celine Dion - My Heart Will Go on",
            "Lyn - My Destiny",
            "ทุกเช้า ทุกคืน ทุกวัน - โอ๊ค สมิทธิ์"
        };
        
        Font testFont = new Font("SansSerif", Font.PLAIN, 14);
        
        for (int i = 0; i < testSongs.length; i++) {
            FontFallbackLabel label = new FontFallbackLabel(testSongs[i]);
            label.setFont(testFont);
            label.setForeground(Color.WHITE);
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setBounds(20, 30 + i * 50, 550, 30);
            panel.add(label);
        }
        
        frame.add(panel);
        frame.setVisible(true);
        
        System.out.println("歌名字体回退测试启动完成。");
        System.out.println("测试的歌名包括：中文、日文、韩文、英文、泰文");
        System.out.println("如果所有文字都能正确显示，说明字体回退功能正常工作。");
    }
}
