package com.sparkle.enterProgram;

import java.awt.Font;
import java.util.Enumeration;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;

import com.sparkle.common.BaseData;
import com.sparkle.logger.LoggerManage;
import com.sparkle.service.MediaPlayerService;
import com.sparkle.ui.MainFrame;
import com.sparkle.util.DataUtil;
import com.sparkle.util.FontsUtil;

/**
 * 
 * @author yuyi2003
 * 
 */
public class EnterProgram {
    private static LoggerManage logger = LoggerManage.getYuyiLogger();

    /**
     * 程序入口
     * 
     * @throws UnsupportedLookAndFeelException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     * 
     */
    public static void main(String[] args) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException,
            UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                init();
            }
        });

    }

    /**
     * 初始化
     */
    protected static void init() {
        // 初始化数据
        DataUtil.init();
        // 初始化播放器服务
        MediaPlayerService.getMediaPlayerService().init();
    
        initGlobalFont(FontsUtil.getBaseFont(BaseData.appFontSize));
        // 启动主界面
        new MainFrame();
        logger.info("启动界面完成");
    }

    /**
     * 统一设置字体，父界面设置之后，所有由父界面进入的子界面都不需要再次设置字体
     */
    private static void initGlobalFont(Font font) {
        FontUIResource fontRes = new FontUIResource(font);
        for (Enumeration<Object> keys = UIManager.getDefaults().keys(); keys
                .hasMoreElements();) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontRes);
            }
        }
    }
}
