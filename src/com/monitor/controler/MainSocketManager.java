package com.monitor.controler;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import com.monitor.core.MainSocket;

import javafx.util.Pair;

public class MainSocketManager {
	ServerSocket mainServer;
	public final int audioPort;
	ArrayList<MainSocket> sockets;
	SocketManager mainManager;

	public MainSocketManager(ServerSocket mainServer, int audioPort, SocketManager mainManager) {
		this.mainServer = mainServer;
		this.audioPort = audioPort;
		this.mainManager = mainManager;
		sockets = new ArrayList<>(5);
		new Thread(new MainManagerThread()).start();
	}

	class MainManagerThread implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					sockets.add(new MainSocket(mainServer.accept(), MainSocketManager.this));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Pair<Integer, Integer> createNewUser(MainSocket socket) {
		return mainManager.createNewUser(socket);
	}

	public Pair<Integer, Pair<Integer, MainSocket>> removeUser(int id) {
		return mainManager.removeUser(id);
	}

	public Pair<Pair<Integer, Integer>, Integer> createControlTask(int from, int to) {
		return mainManager.createControlTask(from, to);
	}

	public Pair<Pair<Integer, Integer>, Integer> removeControlTask(int from, int to) {
		return mainManager.removeControlTask(from, to);
	}

	public Pair<Pair<Integer, Integer>, Integer> removeControlTask(Pair<Integer, Integer> key) {
		return mainManager.removeControlTask(key);
	}

	public void removeSocket(MainSocket socket) {
		sockets.remove(socket);
	}

	public boolean hasUser(int id) {
		return mainManager.hasUser(id);
	}

	public MainSocket bindMainSocket(int id, int password) {
		return mainManager.findUser(id, password);
	}

}
