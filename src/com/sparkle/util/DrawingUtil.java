package com.sparkle.util;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.TextAttribute;
import java.text.AttributedString;

/**
 * 绘图工具类，支持字体回退功能
 * 
 * @author yuyi2003
 * 
 */
public class DrawingUtil {
	
	/**
	 * 绘制带有字体回退功能的文本
	 * 
	 * @param g2d Graphics2D对象
	 * @param text 要绘制的文本
	 * @param x X坐标
	 * @param y Y坐标
	 * @param fontSize 字体大小
	 */
	public static void drawStringWithFallback(Graphics2D g2d, String text, float x, float y, int fontSize) {
		if (text == null || text.length() == 0) {
			return;
		}
		
		AttributedString attributedString = FontsUtil.createFallbackString(text, fontSize);
		g2d.drawString(attributedString.getIterator(), x, y);
	}
	
	/**
	 * 绘制带有字体回退功能的文本（带颜色）
	 * 
	 * @param g2d Graphics2D对象
	 * @param text 要绘制的文本
	 * @param x X坐标
	 * @param y Y坐标
	 * @param fontSize 字体大小
	 * @param paint 画笔（颜色或渐变）
	 */
	public static void drawStringWithFallback(Graphics2D g2d, String text, float x, float y, int fontSize, Paint paint) {
		if (text == null || text.length() == 0) {
			return;
		}
		
		// 保存原始画笔
		Paint originalPaint = g2d.getPaint();
		
		// 设置新画笔
		g2d.setPaint(paint);
		
		// 绘制文本
		AttributedString attributedString = FontsUtil.createFallbackString(text, fontSize);
		g2d.drawString(attributedString.getIterator(), x, y);
		
		// 恢复原始画笔
		g2d.setPaint(originalPaint);
	}
	
	/**
	 * 创建带有颜色属性的AttributedString
	 * 
	 * @param text 要绘制的文本
	 * @param fontSize 字体大小
	 * @param paint 画笔（颜色或渐变）
	 * @return 配置了字体回退和颜色的AttributedString
	 */
	public static AttributedString createColoredFallbackString(String text, int fontSize, Paint paint) {
		if (text == null || text.length() == 0) {
			return new AttributedString("");
		}
		
		AttributedString attributedString = FontsUtil.createFallbackString(text, fontSize);
		
		// 添加颜色属性
		attributedString.addAttribute(TextAttribute.FOREGROUND, paint, 0, text.length());
		
		return attributedString;
	}
	
	/**
	 * 获取文本的宽度（考虑字体回退）
	 * 
	 * @param g2d Graphics2D对象
	 * @param text 文本
	 * @param fontSize 字体大小
	 * @return 文本宽度
	 */
	public static int getTextWidthWithFallback(Graphics2D g2d, String text, int fontSize) {
		if (text == null || text.length() == 0) {
			return 0;
		}
		
		// 更准确的方法：逐字符计算宽度
		double totalWidth = 0;
		
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			java.awt.Font font = FontsUtil.needsFallback(c, fontSize) ? 
				FontsUtil.getFallbackFont(fontSize) : FontsUtil.getBaseFont(fontSize);
			
			// 使用FontMetrics来计算字符宽度
			java.awt.FontMetrics fm = g2d.getFontMetrics(font);
			totalWidth += fm.charWidth(c);
		}
		
		return (int) Math.ceil(totalWidth);
	}
}
