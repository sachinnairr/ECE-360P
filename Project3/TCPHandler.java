import java.util.*;
import java.io.*;
import java.net.*;
public class TCPHandler extends Thread {
	Library lib;
	int port;

	public TCPHandler(int tcpPort, Library lib) {
		port = tcpPort;
		this.lib = lib;
	}

	@Override
	public void run() {
		try {
			ServerSocket socket = new ServerSocket(port);
			Socket s;
			while ((s = socket.accept()) != null) {
				Thread t = new TCPServerThread(s, lib);
				t.start();
			}
		} catch (IOException e) {
			System.err.println("Server aborted:" + e);
		}
	}
}
