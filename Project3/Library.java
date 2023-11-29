import java.util.*;

//class to manage book inventory, record list, and carry out command functions
public class Library {
	ArrayList<Book> inventory;
	Integer lID;
	HashMap<String, ArrayList<Record>> studentList;
	HashMap<Integer, Record> recordList;

	//Constructor
	Library() {
		inventory = new ArrayList<>();
		lID = 0;
		studentList = new HashMap<>();
		recordList = new HashMap<>();
	}
	
	//add book to library
	public void addBook(String bookname, int amount) {
		Book newbook = new Book(bookname, amount);
		inventory.add(newbook);
	}

	//Checks if book is available to be loaned and if so, given to client
	public synchronized String beginLoan(String bookname, String client) {
		String s = "";
		for (Book b : inventory) {
			if ((b.name).equals(bookname)) {
				if (b.quantity <= 0) {
					s = "Request Failed - Book not available";
					return s;
				}
				Integer index = inventory.indexOf(b);
				if (studentList.containsKey(client)) {
					b.quantity--;
					inventory.set(index, b);
					lID++;
					ArrayList<Record> temp = studentList.get(client);
					Record tempR = new Record(b.name, client, lID, index);
					temp.add(tempR);
					studentList.replace(client, temp);
					recordList.put(lID, tempR);
				} else {
					b.quantity--;
					inventory.set(index, b);
					lID++;
					ArrayList<Record> temp = new ArrayList<Record>();
					Record tempR = new Record(b.name, client, lID, index);
					temp.add(tempR);
					studentList.put(client, temp);
					recordList.put(lID, tempR);
				}
				s = "Your request has been approved, " + lID.toString() + " " + client + " \"" + b.name + "\" ";
				return s;
			}
		}
		s = "Request Failed - We do not have this book";
		return s;
	}

	//Checks that book is currently being loaned, and if so, returns the book of given record ID
	public synchronized String endLoan(int loanID) {
		String s = "";
		if (recordList.containsKey(loanID)) {
			Record rReturned = recordList.remove(loanID);
			Book b = inventory.get(rReturned.bookID);
			b.quantity++;
			inventory.set(rReturned.bookID, b);
			ArrayList<Record> temp = studentList.get(rReturned.studentName);
			temp.remove(rReturned);
			s = loanID + " is returned";
		} else {
			s = loanID + " not found, no such borrow record";
		}
		return s;
	}

	//Lists all books loaned out to client, if no record, returns that message instead
	public synchronized String getLoans(String client) {
		String s = "";
		if (studentList.containsKey(client)) {
			ArrayList<Record> temp = studentList.get(client);
			for (int i = 0; i < temp.size(); i++) {
				s += temp.get(i).loanID.toString() + " \"" + temp.get(i).bookName + "\"" + "$";
			}
		} else {
			s = "No record found for " + client;
		}
		return s;
	}

	//Returns the library book inventory
	public synchronized String getInventory() {
		String s = "";
		for (Book b : inventory) {
			String str = ("\"" + b.name + "\" " + b.quantity + "$");
			s = s + str;
		}
		return s;
    }
}