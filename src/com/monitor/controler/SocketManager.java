package com.monitor.controler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;

import com.monitor.core.CheckThread;
import com.monitor.core.MainSocket;

import javafx.util.Pair;

public class SocketManager {
	public final MainSocketManager mainManger;
	public final AudioSocketManager audioManager;
	public final int port;
	public final int audioPort;
	ConcurrentHashMap<Pair<Integer, Integer>, Integer> controlTask;
	ConcurrentHashMap<Integer, Pair<Integer, MainSocket>> users;

	public static CheckThread checkThread;

	static {
		checkThread = new CheckThread();
		checkThread.start();
	}

	public SocketManager(int port) throws IOException {
		this.port = port;
		ServerSocket mainServer = new ServerSocket(port);
		ServerSocket audioServer = new ServerSocket(0);
		// ServerSocket mainServer = new ServerSocket();
		// mainServer.setReuseAddress(true);
		// mainServer.bind(new
		// InetSocketAddress(InetAddress.getLocalHost().getHostAddress(),
		// port));
		//
		// ServerSocket audioServer = new ServerSocket();
		// audioServer.setReuseAddress(true);
		// audioServer.bind(new
		// InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), 0));
		audioPort = audioServer.getLocalPort();
		mainManger = new MainSocketManager(mainServer, audioPort, this);
		audioManager = new AudioSocketManager(audioServer, this);
		controlTask = new ConcurrentHashMap<>();
		users = new ConcurrentHashMap<>();
	}

	public static void main(String[] args) {
		try {
			System.out.println(new Integer(1000) == 1000);
			System.out.println((int) ((int) (Math.random() * 9 + 1) * 100000 + Math.random() * 10000));
			new SocketManager(8888);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Pair<Integer, Integer> createNewUser(MainSocket socket) {
		int temp;
		while (true) {
			temp = (int) ((int) (Math.random() * 9 + 1) * 100000 + Math.random() * 10000);
			if (!users.containsKey(temp))
				break;
		}

		Pair<Integer, Integer> u = new Pair<Integer, Integer>(temp,
				(int) ((int) (Math.random() * 9 + 1) * 100000 + Math.random() * 10000));
		users.put(u.getKey(), new Pair<Integer, MainSocket>(u.getValue(), socket));
		return u;
	}

	public Pair<Integer, Pair<Integer, MainSocket>> removeUser(int id) {
		try {
			return new Pair<Integer, Pair<Integer, MainSocket>>(id, users.remove(id));
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Pair<Pair<Integer, Integer>, Integer> createControlTask(int from, int to) {
		int temp = (int) ((int) (Math.random() * 9 + 1) * 100000 + Math.random() * 10000);
		controlTask.put(new Pair<>(from, to), (temp));
		return new Pair<Pair<Integer, Integer>, Integer>(new Pair<>(from, to), temp);

	}

	public Pair<Pair<Integer, Integer>, Integer> removeControlTask(Pair<Integer, Integer> key) {
		try {
			return new Pair<Pair<Integer, Integer>, Integer>(key, controlTask.remove(key));
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Pair<Pair<Integer, Integer>, Integer> removeControlTask(int from, int to) {
		try {
			Pair<Integer, Integer> temp = new Pair<>(from, to);
			return new Pair<Pair<Integer, Integer>, Integer>(temp, controlTask.remove(temp));
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		}
	}

	public int getSecurityID(int from, int to) {
		Integer temp = controlTask.get(new Pair<>(from, to));
		if (temp == null)
			return -1;
		return temp;
	}

	public MainSocket findUser(int id) {
		Pair<Integer, MainSocket> result = users.get(id);
		if (result != null)
			return result.getValue();
		return null;
	}

	public MainSocket findUser(int id, int password) {
		Pair<Integer, MainSocket> result = users.get(id);
		if (result != null && result.getKey() == password)
			return result.getValue();
		return null;
	}

	public boolean hasUser(int id) {
		return users.containsKey(id);
	}

	public int getTaskNumber(){
		return controlTask.size();
	}

	public int getUserNumber() {
		return users.size();
	}
}
