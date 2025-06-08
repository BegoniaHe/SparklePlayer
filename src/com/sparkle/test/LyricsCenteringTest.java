package com.sparkle.test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.sparkle.util.DrawingUtil;
import com.sparkle.util.FontsUtil;

/**
 * 歌词居中显示测试类
 * 验证使用字体回退后的文本宽度计算是否准确
 * 
 * @author yuyi2003
 */
public class LyricsCenteringTest {
    
    public static void main(String[] args) {
        // 测试各种语言的歌曲名称
        String[] testSongs = {
            "蔡健雅 - Beautiful Love",
            "행복하길 바래 - 林亨柱", 
            "ทุกเช้า ทุกคืน ทุกวัน - โอ๊ค สมิทธิ์",
            "My Heart Will Go on - Celine Dion",
            "普通话歌曲测试",
            "한글 가사 테스트"
        };
        
        int fontSize = 14;
        int panelWidth = 400; // 假设面板宽度为400像素
        
        // 创建一个Graphics2D对象用于测试
        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = testImage.createGraphics();
        g2d.setFont(FontsUtil.getBaseFont(fontSize));
        
        System.out.println("=== 歌词居中显示测试 ===");
        System.out.println("面板宽度: " + panelWidth + "px");
        System.out.println("字体大小: " + fontSize + "px");
        System.out.println();
        
        for (String song : testSongs) {
            // 使用字体回退计算文本宽度
            int textWidth = DrawingUtil.getTextWidthWithFallback(g2d, song, fontSize);
            
            // 计算居中位置
            int centerX = (panelWidth - textWidth) / 2;
            
            System.out.printf("歌曲: %-35s | 文本宽度: %3dpx | 居中X坐标: %3dpx%n", 
                            song, textWidth, centerX);
        }
        
        g2d.dispose();
        
        System.out.println();
        System.out.println("如果居中X坐标为正数且合理，说明文本宽度计算正确，居中显示功能正常。");
    }
}
