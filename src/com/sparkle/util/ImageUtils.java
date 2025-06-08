package com.sparkle.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.sparkle.common.Constants;

/**
 * 图片处理工具类
 * 
 * @author yuyi2003
 */
public class ImageUtils {

    /**
     * 创建圆形图片
     * 
     * @param originalImage 原始图片
     * @param size 圆形图片的大小
     * @return 圆形图片
     */
    public static BufferedImage createCircularImage(BufferedImage originalImage, int size) {
        // 创建透明背景的缓冲图像
        BufferedImage circularImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = circularImage.createGraphics();

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // 创建圆形裁剪区域
        Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, size, size);
        g2d.setClip(circle);

        // 缩放并绘制原始图片
        Image scaledImage = originalImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        g2d.drawImage(scaledImage, 0, 0, null);

        g2d.dispose();
        return circularImage;
    }

    /**
     * 从字节数组创建圆形图片ImageIcon
     * 
     * @param imageData 图片数据字节数组
     * @param size 圆形图片的大小
     * @return 圆形图片ImageIcon
     */
    public static ImageIcon createCircularImageIcon(byte[] imageData, int size) {
        try {
            if (imageData == null || imageData.length == 0) {
                return getDefaultIcon(size);
            }

            // 从字节数组创建BufferedImage
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            BufferedImage originalImage = ImageIO.read(bais);
            
            if (originalImage == null) {
                return getDefaultIcon(size);
            }

            // 创建圆形图片
            BufferedImage circularImage = createCircularImage(originalImage, size);
            return new ImageIcon(circularImage);

        } catch (IOException e) {
            e.printStackTrace();
            return getDefaultIcon(size);
        }
    }

    /**
     * 获取默认的圆形图标
     * 
     * @param size 图标大小
     * @return 默认的圆形图标
     */
    public static ImageIcon getDefaultIcon(int size) {
        String defaultIconPath = Constants.PATH_ICON + File.separator + "ic_launcher.png";
        try {
            BufferedImage defaultImage = ImageIO.read(new File(defaultIconPath));
            if (defaultImage != null) {
                BufferedImage circularDefault = createCircularImage(defaultImage, size);
                return new ImageIcon(circularDefault);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // 如果默认图片也加载失败，创建一个简单的圆形图标
        BufferedImage fallbackImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = fallbackImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(java.awt.Color.LIGHT_GRAY);
        g2d.fillOval(0, 0, size, size);
        g2d.dispose();
        return new ImageIcon(fallbackImage);
    }

    /**
     * 保存专辑封面到本地
     * 
     * @param imageData 图片数据
     * @param fileName 文件名
     * @return 保存的文件路径，失败返回null
     */
    public static String saveAlbumCover(byte[] imageData, String fileName) {
        if (imageData == null || imageData.length == 0) {
            return null;
        }

        try {
            // 确保专辑目录存在
            File albumDir = new File(Constants.PATH_ALBUM);
            if (!albumDir.exists()) {
                albumDir.mkdirs();
            }

            // 从字节数组创建图片
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            BufferedImage image = ImageIO.read(bais);
            
            if (image == null) {
                return null;
            }

            // 保存图片
            String filePath = Constants.PATH_ALBUM + File.separator + fileName + ".jpg";
            File outputFile = new File(filePath);
            ImageIO.write(image, "jpg", outputFile);
            
            return filePath;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
