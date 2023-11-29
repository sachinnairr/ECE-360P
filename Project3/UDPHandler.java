import java.io.*;
import java.net.*;
import java.util.*;

public class UDPHandler extends Thread {
	DatagramPacket datapacket;
    DatagramPacket returnpacket;
	DatagramSocket datasocket;
	int len = 10000;
	byte[] buf;
	int portNum;
	Library lib;

	UDPHandler(int port, Library lib) {
		portNum = port;
		this.lib = lib;
	}

	@Override
	public void run() {
		//System.out.println("TEST IN THREAD");
        try {
			datasocket = new DatagramSocket(portNum);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		while (true) {
			try {
				byte[] buf = new byte[len];
				datapacket = new DatagramPacket(buf, buf.length);
				datasocket.receive(datapacket);

				Thread t = new UDPServerThread(datapacket, datasocket, lib);
				
                t.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}