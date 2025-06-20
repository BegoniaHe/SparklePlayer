package com.sparkle.observable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 现代化的被观察者类，替代已过时的 java.util.Observable.
 * 使用线程安全的 CopyOnWriteArrayList 来管理观察者.
 * 
 * @author yuyi2003
 */
public class SparkleObservable {
    /**
     * 观察者列表.
     */
    private final List<SparkleObserver> observers = new CopyOnWriteArrayList<>();

    /**
     * 变化标志.
     */
    private boolean changed;

    /**
     * 添加观察者.
     *
     * @param observer 要添加的观察者
     */
    public synchronized void addObserver(final SparkleObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * 标记对象已改变.
     */
    protected synchronized void setChanged() {
        changed = true;
    }

    /**
     * 清除改变标记.
     */
    protected synchronized void clearChanged() {
        changed = false;
    }


    /**
     * 通知所有观察者（如果对象已改变）.
     *
     * @param arg 传递给观察者的参数
     */
    public void notifyObservers(final Object arg) {
        final SparkleObserver[] arrLocal;

        synchronized (this) {
            if (!changed) {
                return;
            }
            arrLocal = observers.toArray(new SparkleObserver[0]);
            clearChanged();
        }

        for (final SparkleObserver observer : arrLocal) {
            try {
                observer.update(this, arg);
            } catch (final Exception e) {
                // 记录错误但不影响其他观察者
                // TODO: 对 'printStackTrace()' 的调用可能应当替换为更可靠的日志
                e.printStackTrace();
            }
        }
    }
}
