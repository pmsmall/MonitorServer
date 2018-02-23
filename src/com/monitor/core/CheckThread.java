package com.monitor.core;

import java.util.ArrayList;

public class CheckThread extends Thread {

	ArrayList<Checkable> checkTasks = new ArrayList<>();

	int mode;
	public static final int MODE_TIME = 0;
	public static final int MODE_NOTIFY = 1;

	public CheckThread(int mode) {
		this.mode = mode;
	}

	public CheckThread() {
		this(MODE_NOTIFY);
	}

	public void add(Checkable checkable) {
		synchronized (this) {
			checkTasks.add(checkable);
		}

		synchronized (checkTasks) {
			checkTasks.notify();
		}

	}

	@Override
	public void run() {
		while (true) {
			boolean result;
			for (int i = 0; i < checkTasks.size(); i++) {
				result = checkTasks.get(i).check();
				if (result) {
					synchronized (this) {
						checkTasks.remove(i);
					}
					i--;
				}
			}
			if (checkTasks.size() > 0)
				try {
					Thread.sleep(20);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			else
				try {
					synchronized (checkTasks) {
						checkTasks.wait();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

	}

}
