package com.sparkle.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.sparkle.logger.LoggerManage;
import com.sparkle.model.TabVersion;

/**
 * 数据库表版本信息表.
 * 
 * @author yuyi2003
 */
public final class TabVersionDB {
    
    /**
     * 日志管理器实例.
     */
    private static final LoggerManage LOGGER = LoggerManage.getZhangLogger();

    /**
     * 表名常量.
     */
    public static final String TBL_NAME = "tabVersionTbl";
    
    /**
     * 建表SQL语句常量，不支持long型等.
     */
    public static final String CREATE_TBL = "CREATE TABLE " + TBL_NAME + " ("
            + "id VARCHAR(256),tabName VARCHAR(256),version int)";

    /**
     * 数据操作失败错误消息.
     */
    private static final String DATA_OPERATION_FAILED = "数据失败!";
    
    /**
     * SQL查询前缀常量.
     */
    private static final String SELECT_FROM_PREFIX = "select * from ";

    /**
     * 单例实例.
     */
    private static TabVersionDB tabVersionDB;

    /**
     * 私有构造函数，防止外部实例化.
     */
    private TabVersionDB() {
        // 私有构造函数
    }    /**
     * 获取 TabVersionDB 单例实例.
     * 
     * @return TabVersionDB 实例
     */
    public static synchronized TabVersionDB getTabVersionDB() {
        if (tabVersionDB == null) {
            tabVersionDB = new TabVersionDB();
            tabVersionDB.initializeTable();
        }
        return tabVersionDB;
    }

    /**
     * 初始化数据库表.
     */
    private void initializeTable() {
        try {
            final boolean flag = DBUtils.isTableExist(TBL_NAME);
            if (!flag) {
                final Connection connection = DBUtils.getConnection();
                connection.setAutoCommit(true);
                final Statement stmt = connection.createStatement();
                stmt.executeUpdate(CREATE_TBL);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        } finally {
            DBUtils.close();
        }
    }

    /**
     * 添加版本数据.
     * 
     * @param tabVersion 表版本信息
     */
    public void add(final TabVersion tabVersion) {
        try {
            final Connection connection = DBUtils.getConnection();
            final String sql = "insert into " + TBL_NAME + " values(?,?,?)";
            final PreparedStatement ps = connection.prepareStatement(sql);

            ps.setString(1, tabVersion.getId());
            ps.setString(2, tabVersion.getTabName());
            ps.setInt(3, tabVersion.getVersion());

            final int result = ps.executeUpdate();
            if (result <= 0) {
                LOGGER.error("插入" + TBL_NAME + DATA_OPERATION_FAILED);
            }

        } catch (final Exception e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        } finally {
            DBUtils.close();
        }
    }

    /**
     * 获取版本数据.
     * 
     * @param tabName 表名
     * @return 表版本信息
     */
    public TabVersion getTabVersion(final String tabName) {
        try {
            final TabVersion tabVersion = new TabVersion();
            final Connection connection = DBUtils.getConnection();
            final String sql = SELECT_FROM_PREFIX + TBL_NAME + " where tabName=?";

            final PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, tabName);

            final ResultSet result = ps.executeQuery();
            if (result.next()) {
                tabVersion.setId(result.getString("id"));
                tabVersion.setTabName(result.getString("tabName"));
                tabVersion.setVersion(result.getInt("version"));
            }
            return tabVersion;
        } catch (final Exception e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        } finally {
            DBUtils.close();
        }
        return null;
    }

    /**
     * 更新版本数据.
     * 
     * @param tabVersion 表版本信息
     */
    public void update(final TabVersion tabVersion) {
        try {
            final Connection connection = DBUtils.getConnection();
            final String sql = "update " + TBL_NAME + " set version=? where id=?";
            final PreparedStatement ps = connection.prepareStatement(sql);

            ps.setInt(1, tabVersion.getVersion());
            ps.setString(2, tabVersion.getId());

            final int result = ps.executeUpdate(); // 返回行数或者0
            if (result <= 0) {
                LOGGER.error("更新" + TBL_NAME + DATA_OPERATION_FAILED);
            }

        } catch (final Exception e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        } finally {
            DBUtils.close();
        }
    }

    /**
     * 检查该表数据是否已经存在.
     * 
     * @param id 记录ID
     * @return 如果数据存在返回 true
     */
    public boolean isExist(final String id) {
        try {
            final Connection connection = DBUtils.getConnection();
            final String sql = SELECT_FROM_PREFIX + TBL_NAME + " where id=?";
            final PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, id);
            final ResultSet result = ps.executeQuery();
            if (result.next()) {
                return true;
            }
        } catch (final Exception e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        } finally {
            DBUtils.close();
        }
        return false;
    }
}
