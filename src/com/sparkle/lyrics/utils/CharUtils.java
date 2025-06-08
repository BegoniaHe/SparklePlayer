package com.sparkle.lyrics.utils;

/**
 * 字符工具类
 * 
 * @author yuyi2003
 */
public class CharUtils {
    
    /**
     * 判断字符是不是中文，中文字符标点都可以判断
     * 
     * @param c 字符
     * @return true如果是中文字符
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * 是否是日语平假名
     * 
     * @param c 字符
     * @return true如果是平假名
     */
    public static boolean isHiragana(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.HIRAGANA;
    }

    /**
     * 是否是韩语字符
     * 
     * @param c 字符
     * @return true如果是韩语字符
     */
    public static boolean isHangulSyllables(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.HANGUL_SYLLABLES;
    }

    /**
     * 是否是日语片假名
     * 
     * @param c 字符
     * @return true如果是片假名
     */
    public static boolean isKatakana(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.KATAKANA;
    }

    /**
     * 判断是否为数字
     * 
     * @param c 字符
     * @return true如果是数字
     */
    public static boolean isDigit(char c) {
        return Character.isDigit(c);
    }

    /**
     * 判断是否为字母
     * 
     * @param c 字符
     * @return true如果是字母
     */
    public static boolean isLetter(char c) {
        return Character.isLetter(c);
    }

    /**
     * 判断该字符是不是字母
     * 
     * @param c 字符
     * @return true如果是字母(a-z, A-Z)
     */
    public static boolean isWord(char c) {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
    }
}
