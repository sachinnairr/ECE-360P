import java.util.*;
import java.net.*;
import java.io.*;

public class UDPServerThread extends Thread {
	DatagramPacket datapacket, returnpacket;
	DatagramSocket datasocket;
	Library lib;

	UDPServerThread(DatagramPacket dp, DatagramSocket dS, Library lib) {
		datasocket = dS;
		datapacket = dp;
		this.lib = lib;
	}

	@Override
	public void run() {
		byte[] buffer = new byte[0];
		String command = new String(datapacket.getData());
		buffer = command.getBytes();
		String[] tokens = command.split(" ");
		for (int i = 0; i < tokens.length; i++) {
			tokens[i] = tokens[i].trim();
		}
		if (tokens[0].equals("set-mode")) {
			String response = "The communication mode is set to UDP";
			buffer = response.getBytes();
		} else if (tokens[0].equals("begin-loan")) {
			String name = tokens[1];
			String[] arg = command.split("\"");
			arg[1].trim();
			String bName = arg[1];
			String response = lib.beginLoan(bName, name);
			buffer = response.getBytes();
		} else if (tokens[0].equals("end-loan")) {
			String id = tokens[1];
			id = id.trim();
			Integer lID = Integer.parseInt(id);
			String response = lib.endLoan(lID);
			buffer = response.getBytes();
		} else if (tokens[0].equals("get-loans")) {
			String name = tokens[1];
			String response = lib.getLoans(name);
			buffer = response.getBytes();
		} else if (tokens[0].equals("get-inventory")) {
			String response = lib.getInventory();
			buffer = response.getBytes();
		} else if (tokens[0].equals("exit")) {
			String response = lib.getInventory();
			buffer = response.getBytes();
		} else {
			System.out.println("ERROR: Invalid command");
		}
		DatagramPacket returnpacket = new DatagramPacket(buffer, buffer.length, datapacket.getAddress(), datapacket.getPort());
		try {
			datasocket.send(returnpacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}