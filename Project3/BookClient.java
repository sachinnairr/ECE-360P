import java.io.*;
import java.util.*;
import java.net.*;

public class BookClient {
	public static void main(String[] args) {
		String hostAddress;
		int tcpPort;
		int udpPort;
		int clientId;

		Scanner din = null;
		PrintStream pout = null;
		Socket server = null;
		InetAddress ia = null;
		DatagramSocket datasocket = null;
		DatagramPacket sPacket = null;
		DatagramPacket rPacket = null;
		byte[] rbuffer = new byte[10000];
		Boolean tcp = false;	//tcp mode true, else udp mode

		if (args.length != 2) {
			System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
			System.out.println("\t(1) <command-file>: file with commands to the server");
			System.out.println("\t(2) client id: an integer between 1..9");
			System.exit(-1);
		}

		String commandFile = args[0];
		clientId = Integer.parseInt(args[1]);
		hostAddress = "localhost";
		tcpPort = 7000;// hardcoded -- must match the server's tcp port
		udpPort = 8000;// hardcoded -- must match the server's udp port

		//output file names
		String outputFile = "";
		String invenFile = "inventory.txt";

		//generate inventory and output files
		try {
			outputFile = "out_" + Integer.toString(clientId) + ".txt";
			File myObj = new File(outputFile);
			File invenObj = new File(invenFile);
			if (myObj.createNewFile()) {
				System.out.println(myObj.getName() + " created");
			}
			if (invenObj.createNewFile()) {
				System.out.println(invenObj.getName() + " created");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//read commands
		try {
			Scanner sc = new Scanner(new FileReader(commandFile));
			PrintWriter output = new PrintWriter(new FileWriter((outputFile)), true);
			PrintWriter invenOutput = new PrintWriter(new FileWriter((invenFile)), true);
			String retString = "";
			while (sc.hasNextLine()) {
				String cmd = sc.nextLine();
				String[] tokens = cmd.split(" ");

				if (tokens[0].equals("set-mode")) {
					// TODO: set the mode of communication for sending commands to the server
					if (tokens[1].equals("t")) {
						//TCP mode
						tcp = true;
						server = new Socket(hostAddress, tcpPort);
						din = new Scanner(server.getInputStream());
						pout = new PrintStream(server.getOutputStream(), true);
						pout.println("set-mode t");
						retString = (din.nextLine());
					} else if (tokens[1].equals("u")){
						//UDP mode
						if (tcp) {
							tcp = false;
							server.close();
						}
						ia = InetAddress.getByName(hostAddress);
						datasocket = new DatagramSocket();
						byte[] buffer = cmd.getBytes();
						sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
						datasocket.send(sPacket);
						rPacket = new DatagramPacket(rbuffer, rbuffer.length);
						datasocket.receive(rPacket);
						retString = new String(rPacket.getData(), 0, rPacket.getLength());
						System.out.println(retString);
					}
					output.println(retString);
				} else if (tokens[0].equals("begin-loan")) {
					// TODO: send appropriate command to the server and display the
					// appropriate responses from the server
					if (tcp) {
						pout.println(cmd);
						retString = (din.nextLine());
					} else {
						byte[] buffer = cmd.getBytes();
						sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
						datasocket.send(sPacket);
						rPacket = new DatagramPacket(rbuffer, rbuffer.length);
						datasocket.receive(rPacket);
						retString = new String(rPacket.getData(), 0, rPacket.getLength());
						System.out.println(retString);
					}
					output.println(retString);
				} else if (tokens[0].equals("end-loan")) {
					// TODO: send appropriate command to the server and display the
					// appropriate responses from the server
					if (tcp) {
						pout.println(cmd);
						retString = (din.nextLine());
					} else {
						byte[] buffer = cmd.getBytes();
						sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
						datasocket.send(sPacket);
						rPacket = new DatagramPacket(rbuffer, rbuffer.length);
						datasocket.receive(rPacket);
						retString = new String(rPacket.getData(), 0, rPacket.getLength());
						System.out.println(retString);
					}
					output.println(retString);
				} else if (tokens[0].equals("get-loans")) {
					// TODO: send appropriate command to the server and display the
					// appropriate responses from the server
					if (tcp) {
						pout.println(cmd);
						retString = (din.nextLine());
					} else {
						byte[] buffer = cmd.getBytes();
						sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
						datasocket.send(sPacket);
						rPacket = new DatagramPacket(rbuffer, rbuffer.length);
						datasocket.receive(rPacket);
						retString = new String(rPacket.getData(), 0, rPacket.getLength());
						System.out.println(retString);
					}
					retString = retString.replace("$", "\n");
					output.print(retString);
				} else if (tokens[0].equals("get-inventory")) {
					// TODO: send appropriate command to the server and display the
					// appropriate responses from the server
					if (tcp) {
						pout.println(cmd);
						retString = din.nextLine();
					} else {
						byte[] buffer = cmd.getBytes();
						sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
						datasocket.send(sPacket);
						rPacket = new DatagramPacket(rbuffer, rbuffer.length);
						datasocket.receive(rPacket);
						retString = new String(rPacket.getData(), 0, rPacket.getLength());
						System.out.println(retString);
					}
					retString = retString.replace("$", "\n");
					output.println(retString);
				} else if (tokens[0].equals("exit")) {
					// TODO: send appropriate command to the server
					if (tcp) {
						pout.println(cmd);
						retString = (din.nextLine());
					} else {
						byte[] buffer = cmd.getBytes();
						sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
						datasocket.send(sPacket);
						rPacket = new DatagramPacket(rbuffer, rbuffer.length);
						datasocket.receive(rPacket);
						retString = new String(rPacket.getData(), 0, rPacket.getLength());
						System.out.println(retString);
					}
					retString = retString.replace("$", "\n");
					invenOutput.println(retString);
					output.close();
					invenOutput.close();
				} else {
					System.out.println("ERROR: Invalid command");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
