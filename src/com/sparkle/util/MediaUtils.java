package com.sparkle.util;

import java.io.File;
import java.util.Date;

import com.sparkle.lyrics.utils.FileUtils;
import com.sparkle.lyrics.utils.TimeUtils;
import com.sparkle.model.SongInfo;
import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.model.Track;
import com.tulskiy.musique.system.TrackIO;
import com.tulskiy.musique.util.AudioMath;

public class MediaUtils {
    /**
     * 通过文件获取mp3的相关数据信息
     * 
     * @param filePath
     * @return
     */

    public static SongInfo getSongInfoByFile(String filePath) {
        File sourceFile = new File(filePath);
        if (!sourceFile.exists())
            return null;
        SongInfo songInfo = null;
        try {

            AudioFileReader audioFileReader = TrackIO
                    .getAudioFileReader(sourceFile.getName());
            Track track = audioFileReader.read(sourceFile);

            double totalMS = AudioMath.samplesToMillis(track.getTrackData()
                    .getTotalSamples(), track.getTrackData().getSampleRate());
            long duration = Math.round(totalMS);

            String durationStr = TimeUtils.parseString((int) duration);

            songInfo = new SongInfo();
            // 文件名
            String displayName = sourceFile.getName();

            int index = displayName.lastIndexOf(".");
            displayName = displayName.substring(0, index);

            String artist = "";
            String title = "";
            if (displayName.contains("-")) {
                String[] titleArr = displayName.split("-");
                artist = titleArr[0].trim();
                title = titleArr[1].trim();
            } else {
                title = displayName;
            }

            if (sourceFile.length() < 1024 * 1024) {
                return null;
            }

            songInfo.setSid(IDGenerate.getId("SI-"));
            songInfo.setDisplayName(displayName);
            songInfo.setSinger(artist);
            songInfo.setTitle(title);
            songInfo.setDuration(duration);
            songInfo.setDurationStr(durationStr);
            songInfo.setSize(sourceFile.length());
            songInfo.setSizeStr(FileUtils.getFileSize(sourceFile.length()));            songInfo.setFilePath(filePath);
            songInfo.setType(SongInfo.LOCALSONG);
            // songInfo.setIslike(SongInfo.UNLIKE);
            // songInfo.setDownloadStatus(SongInfo.DOWNLOADED);
            songInfo.setCreateTime(DateUtil.dateToString(new Date()));

            // 提取并保存专辑封面
            try {
                byte[] albumArtData = extractAlbumArt(filePath);
                if (albumArtData != null) {
                    // 使用歌曲ID作为文件名保存专辑封面
                    String albumCoverPath = ImageUtils.saveAlbumCover(albumArtData, songInfo.getSid());
                    if (albumCoverPath != null) {
                        songInfo.setAlbumUrl(albumCoverPath);
                    }
                }

                // 尝试从标签获取更准确的歌曲信息
                SongInfo tagInfo = getAlbumInfo(filePath);
                if (tagInfo.getSinger() != null && !tagInfo.getSinger().trim().isEmpty()) {
                    songInfo.setSinger(tagInfo.getSinger());
                }
                if (tagInfo.getTitle() != null && !tagInfo.getTitle().trim().isEmpty()) {
                    songInfo.setTitle(tagInfo.getTitle());
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 如果专辑封面提取失败，继续执行，不影响歌曲信息的获取
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return songInfo;

    }    /**
     * 提取MP3文件的专辑封面
     * 
     * @param filePath 文件路径
     * @return 专辑封面的字节数组，如果没有则返回null
     */
    public static byte[] extractAlbumArt(String filePath) {
        return AudioTagUtils.extractAlbumArt(filePath);
    }

    /**
     * 获取专辑标题和艺术家信息
     * 
     * @param filePath 文件路径
     * @return 包含专辑信息的SongInfo对象（仅填充album和artist字段）
     */
    public static SongInfo getAlbumInfo(String filePath) {
        SongInfo albumInfo = new SongInfo();
        String[] tags = AudioTagUtils.getAudioTags(filePath);
        
        if (tags[0] != null && !tags[0].trim().isEmpty()) {
            albumInfo.setTitle(tags[0]);
        }
        if (tags[1] != null && !tags[1].trim().isEmpty()) {
            albumInfo.setSinger(tags[1]);
        }
        if (tags[2] != null && !tags[2].trim().isEmpty()) {
            albumInfo.setAlbumUrl(tags[2]); // 暂时将专辑名存储在albumUrl字段
        }
        
        return albumInfo;
    }
}
