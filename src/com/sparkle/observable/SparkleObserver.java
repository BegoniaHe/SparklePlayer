package com.sparkle.observable;

/**
 * 现代化的观察者接口，替代已过时的 java.util.Observer.
 * 
 * @author yuyi2003
 */
@FunctionalInterface
public interface SparkleObserver {
    /**
     * 当被观察对象发生变化时调用此方法.
     * 
     * @param source 发生变化的对象
     * @param data 传递的数据
     */
    void update(Object source, Object data);
}
