package me.devtec.shared.sockets.implementation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import me.devtec.shared.API;
import me.devtec.shared.events.EventManager;
import me.devtec.shared.events.api.ClientResponde;
import me.devtec.shared.events.api.ServerClientConnectedEvent;
import me.devtec.shared.events.api.ServerClientRespondeEvent;
import me.devtec.shared.sockets.SocketClient;
import me.devtec.shared.sockets.SocketUtils;

public class SocketClientHandler implements SocketClient {
	public static byte[] serverName;

	private final String ip;
	private final int port;
	private Socket socket;
	private boolean connected;
	private boolean manuallyClosed;
	private byte[] password;

	private DataInputStream in;
	private  DataOutputStream out;
	private int task = 0;
	private long lastPing;
	private long lastPong;

	public SocketClientHandler(String ip, int port, String password) {
		this.ip=ip;
		this.port=port;
		this.password=password.getBytes();
	}

	@Override
	public String serverName() {
		return new String(SocketClientHandler.serverName);
	}

	@Override
	public String ip() {
		return ip;
	}

	@Override
	public int port() {
		return port;
	}

	@Override
	public int ping() {
		return (int) (-lastPing + lastPong);
	}

	@Override
	public boolean isConnected() {
		return connected && checkRawConnected();
	}

	public boolean checkRawConnected() {
		return socket!=null && !socket.isInputShutdown() && !socket.isOutputShutdown() && !socket.isClosed() && socket.isConnected();
	}

	@Override
	public void start() {
		if(!API.isEnabled())
			return;
		try {
			while(API.isEnabled() && !checkRawConnected()) {
				socket=tryConnect();
				if(!checkRawConnected())
					try {
						Thread.sleep(5000);
					} catch (Exception e) {
					}
			}
			if(!checkRawConnected()) { //What happened? API is disabled?
				start();
				return;
			}
			try {
				in=new DataInputStream(socket.getInputStream());
				out=new DataOutputStream(socket.getOutputStream());
			}catch(Exception err) {
				connected=false;
				if(API.isEnabled())
					start();
				return;
			}
			//PROCESS LOGIN
			if(checkRawConnected() && in.readInt()==ClientResponde.PROCESS_LOGIN.getResponde()) {
				out.writeInt(password.length);
				out.write(password);
				int result = in.readInt();
				ServerClientRespondeEvent respondeEvent = new ServerClientRespondeEvent(SocketClientHandler.this, result);
				EventManager.call(respondeEvent);
				if(result==ClientResponde.RECEIVE_NAME.getResponde()) {
					out.writeInt(SocketClientHandler.serverName.length);
					out.write(SocketClientHandler.serverName);
					result = in.readInt(); //await for respond
					respondeEvent = new ServerClientRespondeEvent(SocketClientHandler.this, result);
					EventManager.call(respondeEvent);
				}
				if(result==ClientResponde.ACCEPTED.getResponde()) {
					connected=true;
					manuallyClosed=false;
					//LOGGED IN, START READER
					lastPing = System.currentTimeMillis()/100;
					lastPong = System.currentTimeMillis()/100;
					new Thread(()->{
						ServerClientConnectedEvent connectedEvent = new ServerClientConnectedEvent(SocketClientHandler.this);
						EventManager.call(connectedEvent);
						while(API.isEnabled() && isConnected()) {
							try {
								task = in.readInt();
								if(task==20) { //ping
									out.writeInt(21);
									try {
										Thread.sleep(100);
									} catch (Exception e) {
									}
									continue;
								}
								if(task==21) { //pong
									lastPong = System.currentTimeMillis()/100;
									try {
										Thread.sleep(100);
									} catch (Exception e) {
									}
									continue;
								}
								ServerClientRespondeEvent crespondeEvent = new ServerClientRespondeEvent(SocketClientHandler.this, task);
								EventManager.call(crespondeEvent);
								SocketUtils.process(this, task);
							} catch (Exception e) {
								break;
							}
							try {
								Thread.sleep(100);
							} catch (Exception e) {
							}
						}
						if(socket!=null && connected && !manuallyClosed) {
							stop();
							start();
						}
					}).start();
					//ping - pong service
					new Thread(()->{
						while(API.isEnabled() && isConnected())
							try {
								Thread.sleep(15000);
								lastPing = System.currentTimeMillis()/100;
								out.writeInt(20);
							} catch (Exception e) {
								break;
							}
						if(socket!=null && connected && !manuallyClosed) {
							stop();
							start();
						}
					}).start();
				}
			}
		} catch (Exception e) {
			socket=null;
			connected=false;
			try {
				Thread.sleep(5000);
			} catch (Exception err) {
			}
			start();
		}
	}

	private Socket tryConnect() {
		try {
			Socket socket=new Socket(ip, port);
			socket.setReuseAddress(true);
			socket.setKeepAlive(true);
			socket.setReceiveBufferSize(4*1024);
			socket.setTcpNoDelay(true);
			return socket;
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	public void stop() {
		manuallyClosed=true;
		connected=false;
		try {
			socket.close();
		} catch (Exception e) {
		}
		socket=null;
	}

	@Override
	public Socket getSocket() {
		return socket;
	}

	@Override
	public DataInputStream getInputStream() {
		return in;
	}

	@Override
	public DataOutputStream getOutputStream() {
		return out;
	}

	@Override
	public boolean canReconnect() {
		return true;
	}

}
