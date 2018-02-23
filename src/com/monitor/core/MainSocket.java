package com.monitor.core;

import java.awt.Dimension;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

import com.monitor.controler.MainSocketManager;
import com.monitor.controler.SocketManager;
import com.monitor.pack.ControlerPack;
import com.monitor.pack.Packet;
import com.net.PacketArrivedListener;
import com.security.AES.Default;
import com.security.net.SafeSocket;

import javafx.util.Pair;

public class MainSocket {
	boolean fromer = false;
	SafeSocket socket;
	int id;
	int password;
	boolean bound = false;
	MainSocket another;
	MainSocketManager manager;
	volatile ImageServerSocket imageSocket = null;
	String key;
	ArrayBlockingQueue<Pair<Pair<Integer, Integer>, Integer>> taskList;

	public MainSocket(Socket socket, MainSocketManager manager) {
		this.socket = new SafeSocket(socket, true, true);
		this.manager = manager;
		this.socket.addPacketArrivedListener(new MainTask());
		taskList = new ArrayBlockingQueue<>(2);
		id = -1;
		password = -1;
	}

	private void sendStartMonitorPack(int from, int to) {
		Packet pack = ControlerPack.createRequireSuccessfullyPack(from, to, taskList.peek().getValue(), key,
				manager.audioPort);
		try {
			socket.sendPacket(pack);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendStartMonitorPack(int from, int to, Dimension rawDimension, String hostName) {
		Packet pack = ControlerPack.createRequireSuccessfullyPack(from, to, taskList.peek().getValue(), key,
				manager.audioPort, rawDimension, hostName);
		try {
			socket.sendPacket(pack);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void errorOccured(String message) {
		try {
			socket.sendPacket(ControlerPack.createErrorPack(message));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class MainTask implements PacketArrivedListener {

		@Override
		public void onPacketArrived(Packet packet, long time) {
			ControlerPack pack = (ControlerPack) packet;
			switch (pack.type) {
			case ControlerPack.TYPE_LOGIN:
				if (id == -1) {
					Pair<Integer, Integer> user = manager.createNewUser(MainSocket.this);
					id = user.getKey();
					password = user.getValue();
					try {
						socket.sendPacket(ControlerPack.createNewUserPack(id, password));
						System.out.println("create");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					try {
						socket.sendPacket(ControlerPack.createNewUserPack(id, password));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			case ControlerPack.TYPE_REQUIRE:
				MainSocket temp = manager.bindMainSocket(pack.to, pack.getPassword());
				if (temp != null) {
					another = temp;
					another.another = MainSocket.this;
					another.bound = bound = true;
					Pair<Pair<Integer, Integer>, Integer> task = manager.createControlTask(pack.from, pack.to);
					taskList.add(task);
					another.taskList.add(task);
					try {
						key = Default.getKey();
						another.key = key;
						another.fromer = true;
						sendStartMonitorPack(pack.from, pack.to);
						another.sendStartMonitorPack(pack.from, pack.to, pack.getRawDimension(), pack.getHostName());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					errorOccured("用户密码错误");
				}
				break;
			case ControlerPack.TYPE_EXIT:
				try {
					exit(pack);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case ControlerPack.TYPE_QUERY_ID:

				try {
					if (manager.hasUser(pack.getQueryID()))
						socket.sendPacket(ControlerPack.createIDQueryResultPack(true));
					else
						socket.sendPacket(ControlerPack.createIDQueryResultPack(false));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case ControlerPack.TYPE_EVENT:
			case ControlerPack.TYPE_PORT:
			case ControlerPack.TYPE_PORT_RESULT:
			case ControlerPack.TYPE_FLOW:
			default:
				try {
					another.socket.sendPacket(pack);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			}
		}

	}

	private void exit(ControlerPack pack) throws Exception {
		another.socket.sendPacket(pack);
		another.bound = bound = false;
		another = null;
		key = null;

		// TODO 回收ImageSocket
		if (imageSocket != null) {
			imageSocket.socket.close();
			another.imageSocket.sendExitPack();
		}

		socket.sendPacket(ControlerPack.createExitPack(ControlerPack.FROM_SERVER));
		socket.close();
		manager.removeControlTask(taskList.poll().getKey());
		manager.removeUser(id);
		manager.removeSocket(MainSocket.this);

		manager = null;
	}

	public void setImageSocket(ImageServerSocket imageSocket) {
		this.imageSocket = imageSocket;
		SocketManager.checkThread.add(checkImageSocketPrepared);
	}

	Checkable checkImageSocketPrepared = new Checkable() {

		@Override
		public boolean check() {
			boolean result = imageSocket != null && another.imageSocket != null;
			if (result) {
				int from, to;
				if (fromer) {
					from = id;
					to = another.id;
				} else {
					from = another.id;
					to = id;
				}
				imageSocket.another = another.imageSocket;
				another.imageSocket.another = imageSocket;
				if (fromer) {
					another.imageSocket.bind();
				} else {
					imageSocket.bind();
				}
				try {
					socket.sendPacket(ControlerPack.createImagePreparedPack(from, to));
					another.socket.sendPacket(ControlerPack.createImagePreparedPack(from, to));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return result;
		}
	};

}
