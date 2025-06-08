package com.sparkle.observable;

/**
 * 观察者，用来观察一些操作，如主题颜色的改变，何时弹出更新的窗口等等。
 * 现已更新为使用现代观察者模式，替代已过时的 java.util.Observable
 * 
 * @author yuyi2003
 */
@SuppressWarnings("deprecation")
public class ObserverManage extends SparkleObservable {

	private static ObserverManage myobserver = null;

	public static ObserverManage getObserver() {
		if (myobserver == null) {
			myobserver = new ObserverManage();
		}
		return myobserver;
	}

	public void setMessage(Object data) {
		myobserver.setChanged();
		myobserver.notifyObservers(data);
	}		/**
	 * 兼容性方法：添加旧式 Observer
	 * @param observer 旧式 Observer
	 */
	public void addLegacyObserver(java.util.Observer observer) {
		if (observer != null) {
			super.addObserver(new ObserverAdapter(observer));
		}
	}		/**
	 * 兼容性方法：删除旧式 Observer
	 * @param observer 旧式 Observer
	 */
	public void deleteLegacyObserver(java.util.Observer observer) {
		if (observer != null) {
			// 找到对应的适配器并删除
			SparkleObserver[] observerArray = getObservers();
			for (SparkleObserver sparkleObserver : observerArray) {
				if (sparkleObserver instanceof ObserverAdapter) {
					ObserverAdapter adapter = (ObserverAdapter) sparkleObserver;
					if (adapter.getLegacyObserver() == observer) {
						super.deleteObserver(adapter);
						break;
					}
				}
			}
		}
	}
}
