import java.util.LinkedList;

// svn343
// amn2867
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

class Node implements Comparable<Node> {

	public String name;
	public int priority;
	public Node next;
	public ReentrantLock lock;

	public Node(String name, int priority) {
		this.name = name;
		this.priority = priority;
		next = null;
		lock = new ReentrantLock();
	}

	@Override
	public int compareTo(Node other) {
		return Integer.compare(priority, other.priority);

	}

}

public class PriorityQueue {

	Node head;
	int maxSize;
	int currSize;

	public PriorityQueue(int maxSize) {
		// Creates a Priority queue with maximum allowed size as capacity
		head = null;
		this.maxSize = maxSize;
		currSize = 0;

	}

	public int add(String name, int priority) {
		// Adds the name with its priority to this queue.
		// Returns the current position in the list where the name was inserted;
		// otherwise, returns -1 if the name is already present in the list.
		// This method blocks when the list is full.

		// check if list is full and block if so
		while (!(currSize < maxSize)) {
			try {
				Thread.sleep(10);

			} catch (InterruptedException e) {

			}
		}

		if (search(name) != -1) {
			return -1;
		}

		// if not full and not in list create a new node
		Node node = new Node(name, priority);
		int index = 0;
		// cases for node to be the new head
		if (node.priority < head.priority || isEmpty()) {
			node.lock.lock();
			head.lock.lock();
			try {
				node.next = head;
				head = node;
			} finally {
				node.lock.unlock();
				head.lock.unlock();
			}
		} else {
			Node temp = head;
			temp.lock.lock();
			try {
				while (temp.next.priority < node.priority && temp.next != null) {
					temp.lock.unlock();
					temp = temp.next;
					temp.lock.lock();
					index++;
				}

			} finally {
				temp.lock.unlock();

			}
			node.next = temp.next;
			temp.next = node;
			index++;

		}
		currSize += 1;
		return index;
	}

	public boolean isEmpty() {
		return (currSize == 0);
	}

	public int search(String name) {
		// Returns the position of the name in the list;
		// otherwise, returns -1 if the name is not found.
		Node temp = head;
		int index = 0;
		temp.lock.lock();
		try {
			while (temp.next != null) {
				if (temp.name.equals(name)) {
					temp.lock.unlock();
					return index;
				} else {
					temp.lock.unlock();
					temp = temp.next;
					temp.lock.lock();
					index++;
				}
			}
		} finally {
			temp.lock.unlock();
		}
		return -1;

	}

	public String getFirst() {
		// Retrieves and removes the name with the highest priority in the list,
		// or blocks the thread if the list is empty.
		while (isEmpty()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {

			}
		}

		String temp = head.name;
		head = head.next;
		currSize--;
		head.lock.unlock();
		return temp;
	}
}