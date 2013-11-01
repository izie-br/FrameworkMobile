package com.quantium.mobile.framework;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Observable {

	// Favor NAO usar referencia forte, para nao ocorrer vazamento de memoria
	private ArrayList<Observer> observers = null;
	private Lock observersLock = new ReentrantLock();

	public void registerObserver(Observer observer) {
		observersLock.lock();
		try {
			if (this.observers == null) {
				this.observers = new ArrayList<Observer>();
			}
			this.observers.add(observer);
		} finally {
			observersLock.unlock();
		}
	}

	public void unregisterObserver(Observer observer) {
		if (observers == null) {
			return;
		}
		observersLock.lock();
		try {
			if (this.observers != null) {
				Iterator<Observer> refIterator = observers.iterator();
				while (refIterator.hasNext()) {
					Observer obj = refIterator.next();
					if (obj == null) {
						refIterator.remove();
					} else if (obj == observer) {
						refIterator.remove();
					}
				}
				if (observers.size() == 0) {
					this.observers = null;
				}
			}
		} finally {
			observersLock.unlock();
		}
	}

	public void triggerObserver(String column) {
		if (observers == null || observers.size() == 0) {
			return;
		}
		new Thread(new OnChangeRunnable(this, column)).start();
	}

	public class OnChangeRunnable implements Runnable {

		protected String column;
		protected Observable target;

		public OnChangeRunnable(Observable target, String column) {
			this.column = column;
			this.target = target;
		}

		@Override
		public synchronized void run() {
			if (observers == null) {
				return;
			}
			observersLock.lock();
			try {
				Iterator<Observer> refIterator = observers.iterator();
				while (refIterator.hasNext()) {
					Observer observer = refIterator.next();
					if (observer == null) {
						refIterator.remove();
					} else {
						observer.update(target, column);
					}
				}
			} finally {
				observersLock.unlock();
			}
		}
	}

}
