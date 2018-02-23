package com.monitor.controler;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import com.monitor.core.ImageServerSocket;
import com.monitor.core.MainSocket;

import javafx.util.Pair;

public class AudioSocketManager {

	ServerSocket audioServer;
	ArrayList<ImageServerSocket> sockets;
	SocketManager mainManager;

	public AudioSocketManager(ServerSocket audioServer, SocketManager mainManager) {
		this.audioServer = audioServer;
		this.mainManager = mainManager;
		sockets = new ArrayList<>(5);
		new Thread(new MainManagerThread()).start();
	}

	class MainManagerThread implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					sockets.add(new ImageServerSocket(audioServer.accept(), AudioSocketManager.this));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public int getSecurityID(int from, int to) {
		return mainManager.getSecurityID(from, to);
	}

	public MainSocket findUser(int id) {
		return mainManager.findUser(id);
	}
}
