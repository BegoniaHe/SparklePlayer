package com.sparkle.model;

/**
 * 数据库表版本信息
 * 
 * @author yuyi2003
 * 
 */
public final class TabVersion {
    public static final String key = "TV-";
    private String id;
    private String tabName;
    private int version;//默认版本号

    private TabVersion() {
    }

    public static TabVersion createTabVersion() {
        return new TabVersion();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTabName() {
        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

}
