import java.io.*;
import java.net.*;
import java.util.*;
public class BookServer {
	public static void main(String[] args) {
		int udpPort;
		int tcpPort;
		
		Library lib = new Library();
		if (args.length != 1) {
			System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
			System.exit(-1);
		}
		String fileName = args[0];
		tcpPort = 7000;
		udpPort = 8000;

		lib = parseFile(fileName, lib);


		// TODO: handle request from clients
		Thread UDP = new UDPHandler(udpPort, lib);
		Thread TCP = new TCPHandler(tcpPort, lib);
		UDP.start(); //UDP Thread
		TCP.start(); //TCP Thread
	}


	public static Library parseFile(String fileName,Library lib){
		Scanner scan = null;
		try{
			scan = new Scanner(new FileReader(fileName));
		}
		catch(FileNotFoundException e){
			System.out.println("Could not find input file file :(");
			e.printStackTrace();
		}
		while(scan.hasNextLine()){
			String command = scan.nextLine();
			String[] bookTokens = command.split("\"");
			String bookName = bookTokens[1];
			Integer numBooks = Integer.parseInt(bookTokens[2].trim());

			lib.addBook(bookName, numBooks);
		}
		scan.close();
		return lib;
	}
}