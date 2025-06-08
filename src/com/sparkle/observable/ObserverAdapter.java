package com.sparkle.observable;

/**
 * ObserverAdapter, 用于将新的 SparkleObserver 接口适配为旧的 java.util.Observer 接口。
 * 
 * @author yuyi2003
 */
@SuppressWarnings("deprecation")
public class ObserverAdapter implements SparkleObserver {
    private final java.util.Observer legacyObserver;
    
    public ObserverAdapter(java.util.Observer legacyObserver) {
        this.legacyObserver = legacyObserver;
    }
    
    @Override
    public void update(Object source, Object data) {
        // 将新的观察者接口调用转换为旧的接口调用
        // 注意：这里我们传递 null 作为 Observable，因为新的模式不再使用 Observable
        legacyObserver.update(null, data);
    }
    
    /**
     * 获取包装的原始 Observer
     * 
     * @return 原始的 Observer 对象
     */
    public java.util.Observer getLegacyObserver() {
        return legacyObserver;
    }
}
