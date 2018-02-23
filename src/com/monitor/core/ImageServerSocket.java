package com.monitor.core;

import java.net.Socket;

import com.monitor.controler.AudioSocketManager;
import com.monitor.pack.ImagePack;
import com.monitor.pack.Packet;
import com.net.PacketArrivedListener;
import com.security.net.SafeSocket;

public class ImageServerSocket {
	SafeSocket socket;
	AudioSocketManager manager;
	ImageServerSocket another;
	MainSocket main;
	String Key;

	public ImageServerSocket(Socket socket, AudioSocketManager manager) {
		this.socket = new SafeSocket(socket, false, false);
		this.manager = manager;
		this.socket.addPacketArrivedListener(new ImageTask());
	}

	public void sendExitPack() throws Exception {
		int from, to;
		if (main.fromer) {
			from = main.id;
			to = main.another.id;
		} else {
			from = main.another.id;
			to = main.id;
		}
		socket.sendPacket(new ImagePack(from, to));
	}

	class ImageTask implements PacketArrivedListener {

		@Override
		public void onPacketArrived(Packet packet, long time) {
			ImagePack pack = (ImagePack) packet;
			// System.out.println(PackHelper.toString(pack));
			switch (pack.type) {
			case ImagePack.TYPE_LOGIN:
				MainSocket main = manager.findUser(pack.getID());
				if (manager.getSecurityID(pack.from, pack.to) == pack.getSecurity(main.key)) {
					ImageServerSocket.this.main = main;
					main.setImageSocket(ImageServerSocket.this);
					System.out.println("³É¹¦°ó¶¨");
					// if()
					// if(fromSocket.)
				}
				break;
			case ImagePack.TYPE_IMAGE:
			default:
				try {
					another.socket.sendPacket(pack, 1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			}
		}

	}

	public void bind() {
		socket.bindToAnotherSafeSocket(another.socket);
	}
}
