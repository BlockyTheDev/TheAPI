package me.devtec.shared.sockets;

import me.devtec.shared.dataholder.Config;
import me.devtec.shared.dataholder.loaders.ByteLoader;
import me.devtec.shared.events.EventManager;
import me.devtec.shared.events.api.ClientResponde;
import me.devtec.shared.events.api.ServerClientPreReceiveFileEvent;
import me.devtec.shared.events.api.ServerClientReceiveDataEvent;
import me.devtec.shared.events.api.ServerClientReceiveFileEvent;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

	public static boolean readFile(DataInputStream in, FileOutputStream out, File file) throws IOException {
		int bytes;
		long size = in.readLong();
		long origin = size;
		byte[] buffer = new byte[2*1024];
		while (size > 0 && (bytes = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
			out.write(buffer,0,bytes);
			size -= bytes;
		}
		out.close();
		return origin == file.length();
	}

	public static void process(SocketClient client, int taskId) throws IOException {
		DataInputStream in = client.getInputStream();
		Config data = null;
		switch(ClientResponde.fromResponde(taskId)) {
		case RECEIVE_DATA:{
			ServerClientReceiveDataEvent event = new ServerClientReceiveDataEvent(client, SocketUtils.readConfig(in));
			EventManager.call(event);
			break;
		}
		case RECEIVE_DATA_AND_FILE:
			data = SocketUtils.readConfig(in);
		case RECEIVE_FILE:
			client.lock();

			ServerClientPreReceiveFileEvent event = new ServerClientPreReceiveFileEvent(client, data, SocketUtils.readText(in));
			EventManager.call(event);
			if(event.isCancelled()) {
				client.getOutputStream().writeInt(ClientResponde.REJECTED_FILE.getResponde());
				client.getOutputStream().flush();
				client.unlock();
				break;
			}

			client.getOutputStream().writeInt(ClientResponde.ACCEPTED_FILE.getResponde());
			String folder = event.getFileDirectory();
			if(!folder.isEmpty() && !folder.endsWith("/"))folder+="/";
			File createdFile = SocketUtils.findUsableName(folder+event.getFileName());
			FileOutputStream out = new FileOutputStream(createdFile);
			if(!SocketUtils.readFile(in, out, createdFile)) {
				client.getOutputStream().flush();
				client.getOutputStream().writeInt(ClientResponde.FAILED_DOWNLOAD_FILE.getResponde());
				client.getOutputStream().flush();
				client.unlock();
				createdFile.delete(); //Failed to download file! Repeat.
				break;
			}
			client.getOutputStream().flush();
			client.getOutputStream().writeInt(ClientResponde.SUCCESSFULLY_DOWNLOADED_FILE.getResponde());
			client.getOutputStream().flush();
			client.unlock();
			ServerClientReceiveFileEvent fileEvent = new ServerClientReceiveFileEvent(client, data, createdFile);
			EventManager.call(fileEvent);
			break;
		default:
			break;
		}
	}

	private static File findUsableName(String fileName) {
		File file = new File(fileName);
		if(file.exists()) {
			String end = fileName.split("\\.")[fileName.split("\\.").length-1];
			return SocketUtils.findUsableName(fileName.substring(0, fileName.length()-(end.length()+1))+"-copy."+end);
		}
		if(file.getParentFile()!=null)
			file.getParentFile().mkdirs();
		try {
			file.createNewFile();
		} catch (Exception e) {
		}
		return file;
	}
}