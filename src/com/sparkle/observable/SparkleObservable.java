package com.sparkle.observable;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

/**
 * 现代化的被观察者类，替代已过时的 java.util.Observable
 * 使用线程安全的 CopyOnWriteArrayList 来管理观察者
 * 
 * @author yuyi2003
 */
public class SparkleObservable {
    private final List<SparkleObserver> observers = new CopyOnWriteArrayList<>();
    private boolean changed = false;
    
    /**
     * 添加观察者
     * 
     * @param observer 要添加的观察者
     */
    public synchronized void addObserver(SparkleObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    /**
     * 删除观察者
     * 
     * @param observer 要删除的观察者
     */
    public synchronized void deleteObserver(SparkleObserver observer) {
        observers.remove(observer);
    }
    
    /**
     * 删除所有观察者
     */
    public synchronized void deleteObservers() {
        observers.clear();
    }
    
    /**
     * 标记对象已改变
     */
    protected synchronized void setChanged() {
        changed = true;
    }
    
    /**
     * 清除改变标记
     */
    protected synchronized void clearChanged() {
        changed = false;
    }
    
    /**
     * 检查对象是否已改变
     * 
     * @return 如果对象已改变返回 true
     */
    public synchronized boolean hasChanged() {
        return changed;
    }
    
    /**
     * 通知所有观察者（如果对象已改变）
     * 
     * @param arg 传递给观察者的参数
     */
    public void notifyObservers(Object arg) {
        SparkleObserver[] arrLocal;
        
        synchronized (this) {
            if (!changed) {
                return;
            }
            arrLocal = observers.toArray(new SparkleObserver[0]);
            clearChanged();
        }
        
        for (SparkleObserver observer : arrLocal) {
            try {
                observer.update(this, arg);
            } catch (Exception e) {
                // 记录错误但不影响其他观察者
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 通知所有观察者（如果对象已改变）
     */
    public void notifyObservers() {
        notifyObservers(null);
    }
    
    /**
     * 获取观察者数量
     * 
     * @return 观察者数量
     */
    public synchronized int countObservers() {
        return observers.size();
    }
    
    /**
     * 获取所有观察者的副本（用于遍历）
     * 
     * @return 观察者数组的副本
     */
    protected synchronized SparkleObserver[] getObservers() {
        return observers.toArray(new SparkleObserver[0]);
    }
}
