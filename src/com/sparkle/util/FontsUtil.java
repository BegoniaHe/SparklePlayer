package com.sparkle.util;

import java.awt.Font;
import java.io.File;
import java.text.AttributedString;

import com.sparkle.common.Constants;

/**
 * 字体处理类
 * 支持字体回退功能：默认使用HYRunYuan-65S.ttf，如果无法渲染则回退到Arial-Unicode-Regular.ttf
 * 
 * @author yuyi2003
 * 
 */
public class FontsUtil {
	// 保持向后兼容
	public static String fontFilePath = Constants.PATH_FONTS + File.separator
			+ "Arial-Unicode-Regular.ttf";

	/**
	 * 根据字体文件获取字体
	 * 
	 * @param filePath
	 * @param fontSize
	 *            字体大小
	 * @return
	 */
	public static Font getFontByFile(String filePath, int fontStyle,
			int fontSize) {
		Font font = null;
		try {

			// 推荐用这种，原因请详细看http://www.cnblogs.com/zcy_soft/p/3503656.html
			font = Font.createFont(Font.TRUETYPE_FONT, new File(filePath));
			font = font.deriveFont(fontStyle, fontSize);

		} catch (Exception e) {
			e.printStackTrace();
		}
		if (font == null) {
			font = getBaseFont(fontSize);
		}

		return font;
	}

	/**
	 * 获取默认字体（使用字体回退功能）
	 * 
	 * @param fontSize
	 * @return
	 */
	public static Font getBaseFont(int fontSize) {
		// 使用字体回退功能，优先使用HYRunYuan-65S.ttf
		return FontFallbackUtil.getPrimaryFont(fontSize);
	}
	
	/**
	 * 获取回退字体
	 * 
	 * @param fontSize
	 * @return
	 */
	public static Font getFallbackFont(int fontSize) {
		return FontFallbackUtil.getFallbackFont(fontSize);
	}
	
	/**
	 * 创建带有字体回退功能的AttributedString
	 * 
	 * @param text 要绘制的文本
	 * @param fontSize 字体大小
	 * @return 配置了字体回退的AttributedString
	 */
	public static AttributedString createFallbackString(String text, int fontSize) {
		return FontFallbackUtil.createFallbackString(text, fontSize);
	}
		/**
	 * 检查字符是否需要使用回退字体
	 * 
	 * @param c 字符
	 * @param fontSize 字体大小
	 * @return true表示需要回退字体
	 */
	public static boolean needsFallback(char c, int fontSize) {
		Font primaryFont = FontFallbackUtil.getPrimaryFont(fontSize);
		return FontFallbackUtil.needsFallback(c, primaryFont);
	}
	
	/**
	 * 获取支持指定字符的字体
	 * 
	 * @param c 字符
	 * @param fontSize 字体大小
	 * @return 支持该字符的字体
	 */
	public static Font getFontForCharacter(char c, int fontSize) {
		return FontFallbackUtil.getFontForCharacter(c, fontSize);
	}
}
