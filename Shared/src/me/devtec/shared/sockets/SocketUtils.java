package me.devtec.shared.sockets;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import me.devtec.shared.dataholder.Config;
import me.devtec.shared.dataholder.loaders.ByteLoader;
import me.devtec.shared.events.EventManager;
import me.devtec.shared.events.api.ServerPreReceiveFileEvent;
import me.devtec.shared.events.api.ServerReceiveDataEvent;
import me.devtec.shared.events.api.ServerReceiveFileEvent;

public class SocketUtils {
	public static Config readConfig(DataInputStream in) throws IOException {
		byte[] path = new byte[in.readInt()];
		in.read(path);
		return new Config(ByteLoader.fromBytes(path));
	}

	public static String readText(DataInputStream in) throws IOException {
		byte[] path = new byte[in.readInt()];
		in.read(path);
		return new String(path);
	}

	public static void readFile(DataInputStream in, FileOutputStream out) throws IOException {
		int bytes;
		long size = in.readLong();
		byte[] buffer = new byte[2*1024];
		while (size > 0 && (bytes = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
			out.write(buffer,0,bytes);
			size -= bytes;
		}
		out.close();
	}

	public static void skipFileBytes(DataInputStream in) throws IOException {
		int bytes;
		long size = in.readLong();
		byte[] buffer = new byte[2*1024];
		while (size > 0 && (bytes = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1)
			size -= bytes;
	}

	public static void process(SocketClient client, int taskId) throws IOException {
		DataInputStream in = client.getInputStream();
		switch(taskId) {
		case SocketServer.RECEIVE_DATA:{
			byte[] path = new byte[in.readInt()];
			in.read(path);
			ServerReceiveDataEvent event = new ServerReceiveDataEvent(client, SocketUtils.readConfig(in));
			EventManager.call(event);
			break;
		}
		case SocketServer.RECEIVE_FILE:{
			ServerPreReceiveFileEvent event = new ServerPreReceiveFileEvent(client, null, SocketUtils.readText(in));
			EventManager.call(event);
			if(event.isCancelled()) {
				SocketUtils.skipFileBytes(in);
				break;
			}
			String folder = event.getFileDirectory();
			if(!folder.isEmpty() && !folder.endsWith("/"))folder+="/";
			File createdFile = new File(folder+event.getFileName());
			if(createdFile.exists())createdFile.delete();
			else {
				if(createdFile.getParentFile()!=null)
					createdFile.getParentFile().mkdirs();
				createdFile.createNewFile();
			}
			FileOutputStream out = new FileOutputStream(createdFile);
			SocketUtils.readFile(in, out);
			ServerReceiveFileEvent fileEvent = new ServerReceiveFileEvent(client, null, createdFile);
			EventManager.call(fileEvent);
			break;
		}
		case SocketServer.RECEIVE_DATA_AND_FILE:{
			Config data = SocketUtils.readConfig(in);
			ServerPreReceiveFileEvent event = new ServerPreReceiveFileEvent(client, data, SocketUtils.readText(in));
			EventManager.call(event);
			if(event.isCancelled()) {
				SocketUtils.skipFileBytes(in);
				break;
			}
			String folder = event.getFileDirectory();
			if(!folder.isEmpty() && !folder.endsWith("/"))folder+="/";
			File createdFile = new File(folder+event.getFileName());
			if(createdFile.exists())createdFile.delete();
			else {
				if(createdFile.getParentFile()!=null)
					createdFile.getParentFile().mkdirs();
				createdFile.createNewFile();
			}
			FileOutputStream out = new FileOutputStream(createdFile);
			SocketUtils.readFile(in, out);
			ServerReceiveFileEvent fileEvent = new ServerReceiveFileEvent(client, data, createdFile);
			EventManager.call(fileEvent);
			break;
		}
		}
	}
}