package com.sparkle.util;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.io.File;
import java.text.AttributedString;

import com.sparkle.common.Constants;

/**
 * 字体回退工具类
 * 实现字体回退功能：默认使用HYRunYuan-65S.ttf，如果无法渲染则回退到Arial-Unicode-Regular.ttf
 * 
 * @author yuyi2003
 * 
 */
public class FontFallbackUtil {
    
    // 主字体文件路径
    public static final String PRIMARY_FONT_PATH = Constants.PATH_FONTS + File.separator
            + "HYRunYuan-65S.ttf";
    
    // 回退字体文件路径
    public static final String FALLBACK_FONT_PATH = Constants.PATH_FONTS + File.separator
            + "Arial-Unicode-Regular.ttf";
    
    // 缓存的主字体和回退字体
    private static Font primaryFont;
    private static Font fallbackFont;
    
    /**
     * 获取主字体
     * 
     * @param fontSize 字体大小
     * @return Font对象
     */
    public static Font getPrimaryFont(int fontSize) {
        if (primaryFont == null || primaryFont.getSize() != fontSize) {
            primaryFont = loadFontFromFile(PRIMARY_FONT_PATH, fontSize);
            if (primaryFont == null) {
                // 如果主字体加载失败，使用回退字体
                primaryFont = getFallbackFont(fontSize);
            }
        }
        return primaryFont;
    }
    
    /**
     * 获取回退字体
     * 
     * @param fontSize 字体大小
     * @return Font对象
     */
    public static Font getFallbackFont(int fontSize) {
        if (fallbackFont == null || fallbackFont.getSize() != fontSize) {
            fallbackFont = loadFontFromFile(FALLBACK_FONT_PATH, fontSize);
            if (fallbackFont == null) {
                // 如果回退字体也加载失败，使用系统默认字体
                fallbackFont = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize);
            }
        }
        return fallbackFont;
    }
    
    /**
     * 从文件加载字体
     * 
     * @param fontPath 字体文件路径
     * @param fontSize 字体大小
     * @return Font对象，加载失败返回null
     */
    private static Font loadFontFromFile(String fontPath, int fontSize) {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath));
            return font.deriveFont(Font.PLAIN, fontSize);
        } catch (Exception e) {
            System.err.println("无法加载字体文件: " + fontPath + ", 错误: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 创建带有字体回退功能的AttributedString
     * 
     * @param text 要绘制的文本
     * @param fontSize 字体大小
     * @return 配置了字体回退的AttributedString
     */
    public static AttributedString createFallbackString(String text, int fontSize) {
        if (text == null || text.length() == 0) {
            return new AttributedString("");
        }
        
        Font mainFont = getPrimaryFont(fontSize);
        Font fallbackFont = getFallbackFont(fontSize);
        
        AttributedString result = new AttributedString(text);
        int textLength = text.length();
        
        // 首先设置主字体作为默认字体
        result.addAttribute(TextAttribute.FONT, mainFont, 0, textLength);
        
        // 检查每个字符，如果主字体无法显示，则使用回退字体
        boolean inFallback = false;
        int fallbackBegin = 0;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean needFallback = !mainFont.canDisplay(c);
            
            if (needFallback != inFallback) {
                if (inFallback) {
                    // 结束回退字体区域
                    result.addAttribute(TextAttribute.FONT, fallbackFont, fallbackBegin, i);
                    inFallback = false;
                } else {
                    // 开始回退字体区域
                    fallbackBegin = i;
                    inFallback = true;
                }
            }
        }
        
        // 如果文本结尾还在回退字体区域，需要处理最后一段
        if (inFallback) {
            result.addAttribute(TextAttribute.FONT, fallbackFont, fallbackBegin, textLength);
        }
        
        return result;
    }
    
    /**
     * 检查字符是否需要使用回退字体
     * 
     * @param c 字符
     * @param primaryFont 主字体
     * @return true表示需要回退字体
     */
    public static boolean needsFallback(char c, Font primaryFont) {
        return !primaryFont.canDisplay(c);
    }
    
    /**
     * 获取支持指定字符的字体
     * 
     * @param c 字符
     * @param fontSize 字体大小
     * @return 支持该字符的字体
     */
    public static Font getFontForCharacter(char c, int fontSize) {
        Font primaryFont = getPrimaryFont(fontSize);
        if (primaryFont.canDisplay(c)) {
            return primaryFont;
        } else {
            return getFallbackFont(fontSize);
        }
    }
}
