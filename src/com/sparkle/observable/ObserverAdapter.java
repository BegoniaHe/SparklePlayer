package com.sparkle.observable;

/**
 * ObserverAdapter, 用于将新的 SparkleObserver 接口适配为旧的 java.util.Observer 接口.
 * 
 * @author yuyi2003
 */
@SuppressWarnings("deprecation")
public class ObserverAdapter implements SparkleObserver {
    /**
     * 包装的旧式观察者.
     */
    private final java.util.Observer legacyObserver;
    
    /**
     * 构造函数.
     * 
     * @param legacyObserver 旧式观察者
     */
    public ObserverAdapter(final java.util.Observer legacyObserver) {
        this.legacyObserver = legacyObserver;
    }
    
    /**
     * 更新方法，将新接口调用转换为旧接口调用.
     * 
     * @param source 事件源
     * @param data 数据
     */
    @Override
    public void update(final Object source, final Object data) {
        // 将新的观察者接口调用转换为旧的接口调用
        // 注意：这里我们传递 null 作为 Observable，因为新的模式不再使用 Observable
        legacyObserver.update(null, data);
    }
    
    /**
     * 获取包装的原始 Observer.
     * 
     * @return 原始的 Observer 对象
     */
    public java.util.Observer getLegacyObserver() {
        return legacyObserver;
    }
}
