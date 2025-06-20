package com.sparkle.observable;

/**
 * 观察者管理器，用来观察一些操作，如主题颜色的改变，何时弹出更新的窗口等等.
 * 现已更新为使用现代观察者模式，替代已过时的 java.util.Observable.
 * 
 * @author yuyi2003
 */
public final class ObserverManage extends SparkleObservable {

    /**
     * 单例实例.
     */
    private static ObserverManage instance;

    /**
     * 私有构造函数，防止外部实例化.
     */
    private ObserverManage() {
        // 私有构造函数
    }

    /**
     * 获取观察者管理器实例.
     *
     * @return 观察者管理器实例
     */
    public static synchronized ObserverManage getObserver() {
        if (instance == null) {
            instance = new ObserverManage();
        }
        return instance;
    }

    /**
     * 设置消息并通知观察者.
     *
     * @param data 消息数据
     */
    public void setMessage(final Object data) {
        setChanged();
        notifyObservers(data);
    }
}
