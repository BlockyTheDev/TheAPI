package me.devtec.shared.sockets;

import me.devtec.shared.dataholder.Config;
import me.devtec.shared.events.EventManager;
import me.devtec.shared.events.api.ClientResponde;
import me.devtec.shared.events.api.ServerClientRespondeEvent;
import me.devtec.shared.sockets.implementation.SocketClientHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

public interface SocketClient {
	public String serverName();

	public String ip();

	public int port();

	public boolean isConnected();

	public boolean canReconnect();

	public int ping();

	public void lock();

	public void unlock();

	public boolean isLocked();

	public default void write(String fileName, File file) {
		writeWithData(null, fileName, file);
	}

	public default void writeWithData(Config data, String fileName, File file) {
		if(fileName==null || file == null)return;
		DataOutputStream out = getOutputStream();
		try {
			lock();
			if(data!=null) {
				out.writeInt(ClientResponde.RECEIVE_DATA_AND_FILE.getResponde());
				//data
				byte[] path = data.toByteArray();
				out.writeInt(path.length);
				out.write(path);
			} else
				out.writeInt(ClientResponde.RECEIVE_FILE.getResponde());
			//file
			byte[] bytesData = fileName.getBytes();
			out.writeInt(bytesData.length);
			out.write(bytesData);

			ClientResponde responde = ClientResponde.fromResponde(getInputStream().readInt());
			ServerClientRespondeEvent crespondeEvent = new ServerClientRespondeEvent(this, responde.getResponde());
			EventManager.call(crespondeEvent);

			if(responde==ClientResponde.ACCEPTED_FILE) {
				out.writeLong(file.length());
				FileInputStream fileInputStream = new FileInputStream(file);
				int bytes = 0;
				byte[] buffer = new byte[2*1024];
				while ((bytes=fileInputStream.read(buffer))!=-1)
					out.write(buffer,0,bytes);
				out.flush();
				fileInputStream.close();
				Thread.sleep(200);
				responde = ClientResponde.fromResponde(getInputStream().readInt());
				crespondeEvent = new ServerClientRespondeEvent(this, responde.getResponde());
				EventManager.call(crespondeEvent);
				unlock();
				if(responde==ClientResponde.FAILED_DOWNLOAD_FILE)
					writeWithData(data, fileName, file);
			}else {
				out.flush();
				unlock();
			}
		}catch(Exception e) {
			e.printStackTrace();
			unlock();
			stop();
			if(canReconnect())
				start();
		}
	}

	public default void write(Config data) {
		if(data == null)return;
		DataOutputStream out = getOutputStream();
		try {
			byte[] path = data.toByteArray();
			out.writeInt(ClientResponde.RECEIVE_DATA.getResponde());
			out.writeInt(path.length);
			out.write(path);
			out.flush();
		}catch(Exception e) {
			stop();
			if(canReconnect())
				start();
		}
	}

	public default void write(File file) {
		if(file == null)return;
		write(file.getName(), file);
	}

	public default void writeWithData(Config data, File file) {
		if(data == null || file == null)return;
		writeWithData(data, file.getName(), file);
	}

	public void start();

	public void stop();

	public DataInputStream getInputStream();

	public DataOutputStream getOutputStream();

	public Socket getSocket();

	public static void setServerName(String serverName) {
		SocketClientHandler.serverName=serverName.getBytes();
	}

	public static SocketClientHandler openConnection(String ip, int port, String password) {
		SocketClientHandler client = new SocketClientHandler(ip, port, password);
		client.start();
		return client;
	}
}