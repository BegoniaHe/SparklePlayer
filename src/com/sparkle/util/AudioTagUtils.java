package com.sparkle.util;

import java.io.File;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

/**
 * 音频标签工具类
 * 专门用于处理音频文件的标签信息和专辑封面
 * 
 * @author yuyi2003
 */
public class AudioTagUtils {

    /**
     * 提取MP3文件的专辑封面
     * 
     * @param filePath 文件路径
     * @return 专辑封面的字节数组，如果没有则返回null
     */
    public static byte[] extractAlbumArt(String filePath) {
        try {
            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                return null;
            }

            AudioFile af = AudioFileIO.read(audioFile);
            Tag tag = af.getTag();
            
            if (tag != null) {
                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    return artwork.getBinaryData();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取音频文件的标签信息
     * 
     * @param filePath 文件路径
     * @return 包含标签信息的字符串数组：[title, artist, album]
     */
    public static String[] getAudioTags(String filePath) {
        String[] tags = new String[3]; // [title, artist, album]
        try {
            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                return tags;
            }

            AudioFile af = AudioFileIO.read(audioFile);
            Tag tag = af.getTag();
            
            if (tag != null) {
                tags[0] = tag.getFirst(FieldKey.TITLE);
                tags[1] = tag.getFirst(FieldKey.ARTIST);
                tags[2] = tag.getFirst(FieldKey.ALBUM);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tags;
    }

    /**
     * 检查音频文件是否有专辑封面
     * 
     * @param filePath 文件路径
     * @return 如果有专辑封面返回true，否则返回false
     */
    public static boolean hasAlbumArt(String filePath) {
        try {
            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                return false;
            }

            AudioFile af = AudioFileIO.read(audioFile);
            Tag tag = af.getTag();
            
            if (tag != null) {
                Artwork artwork = tag.getFirstArtwork();
                return artwork != null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
