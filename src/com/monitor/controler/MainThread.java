package com.monitor.controler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainThread {
	SocketManager manager;

	public MainThread() throws IOException {
		manager = new SocketManager(8889);
	}

	public void run() {
	}

	public void start() {
		run();
	}

	public static void main(String[] args) {
		try {
			MainThread main = new MainThread();
			main.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				try {
					String command = reader.readLine();
					if (command.equals("exit") | command.equals("quit"))
						break;
					switch (command) {
					case "ls users":
						System.out.println("当前使用用户" + main.manager.getUserNumber());
						break;
					case "ls tasks":
						System.out.println("当前任务数" + main.manager.getTaskNumber());
						break;
					default:
						System.out.println("ls users 显示在线用户数, ls tasks 显示当前任务数");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
