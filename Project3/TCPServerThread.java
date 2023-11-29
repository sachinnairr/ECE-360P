import java.util.*;
import java.io.*;
import java.net.*;
public class TCPServerThread extends Thread {
	Socket theClient;
	Library lib;

	TCPServerThread(Socket s, Library lib) {
		theClient = s;
		this.lib = lib;
	}

	@Override
	public void run() {
		try {
			String response = " ";
			Scanner sc = new Scanner(theClient.getInputStream());
			PrintWriter pout = new PrintWriter(theClient.getOutputStream(), true);
			while (sc.hasNextLine()) {
				String cmd = sc.nextLine();
				String[] tokens = cmd.split(" ");

				if (tokens[0].equals("set-mode")) {
					pout.println("The communication mode is set to TCP");
				} else if (tokens[0].equals("begin-loan")) {
					String stu = tokens[1];
					String[] arg = cmd.split("\"");
					String bName = arg[1];
					response = lib.beginLoan(bName, stu);
					pout.println(response);
				} else if (tokens[0].equals("end-loan")) {
					String id = tokens[1];
					id = id.trim();
					Integer lID = Integer.parseInt(id);
					response = lib.endLoan(lID);
					pout.println(response);
				} else if (tokens[0].equals("get-loans")) {
					String stu = tokens[1];
					response = lib.getLoans(stu);
					pout.println(response);
				} else if (tokens[0].equals("get-inventory")) {
					response = lib.getInventory();
					pout.println(response);
				} else if (tokens[0].equals("exit")) {
					response = lib.getInventory();
					pout.println(response);
					theClient.close();
				} else {
					System.out.println("ERROR: Invalid command");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}