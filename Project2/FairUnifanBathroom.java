// svn343
// amn2867

import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.LinkedList;
import java.util.Queue;

public class FairUnifanBathroom {
	// TESTING
//	public static void main(String[] args) {
//		FairUnifanBathroom bathroom = new FairUnifanBathroom();
//
//		for (int i = 0; i < 50; i++) {
//			Random random = new Random();
//			boolean isUT = (random.nextBoolean());
//			if (isUT) {
//				new Thread(() -> {
//					try {
//						bathroom.enterBathroomUT();
//						Thread.sleep(new Random().nextInt(1000));
//						bathroom.leaveBathroomUT();
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}).start();
//			} else {
//				new Thread(() -> {
//					try {
//						bathroom.enterBathroomOU();
//						Thread.sleep(new Random().nextInt(1000));
//						bathroom.leaveBathroomOU();
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}).start();
//			}
//		}
//	}

	final int CAPACITY = 7;
	private int currentSpace = CAPACITY;
	
	private int num_ut_outside = 0;
	private int num_ou_outside = 0;
	private int num_ut_inside = 0;
	private int num_ou_inside = 0;
	
	ReentrantLock monitorLock = new ReentrantLock();
	Condition isUT = monitorLock.newCondition();
	Condition isOU = monitorLock.newCondition();
	
	Queue<Integer> queue = new LinkedList<>();	// 0 = UT, 1 = OU

	public void enterBathroomUT() {
		// Called when a UT fan wants to enter bathroom
		monitorLock.lock();
		if (currentSpace == 0 || num_ou_inside > 0 || queuePeek(queue) == 1) {
			num_ut_outside++;
			queue.add(0);
//			System.out.println("UT inside: " + num_ut_inside + " UT outside: " + num_ut_outside);
//			System.out.println("OU inside: " + num_ou_inside + " OU outside: " + num_ou_outside + '\n');
		}
		while (currentSpace == 0 || num_ou_inside > 0 || queuePeek(queue) == 1) {
			try {
				isUT.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (num_ut_outside != 0) {
			num_ut_outside--;
			queue.remove();
		}
		currentSpace--;
		num_ut_inside++;
//		System.out.println("UT inside: " + num_ut_inside + " UT outside: " + num_ut_outside);
//		System.out.println("OU inside: " + num_ou_inside + " OU outside: " + num_ou_outside + '\n');
		monitorLock.unlock();
	}

	public void enterBathroomOU() {
		// Called when a OU fan wants to enter bathroom
		monitorLock.lock();

		if (currentSpace == 0 || num_ut_inside > 0 || queuePeek(queue) == 0) {
			num_ou_outside++;
			queue.add(1);
//			System.out.println("UT inside: " + num_ut_inside + " UT outside: " + num_ut_outside);
//			System.out.println("OU inside: " + num_ou_inside + " OU outside: " + num_ou_outside + '\n');
		}

		while (currentSpace == 0 || num_ut_inside > 0 || queuePeek(queue) == 0) {
			try {
				isOU.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (num_ou_outside != 0) {
			num_ou_outside--;
			queue.remove();
		}
		currentSpace--;
		num_ou_inside++;
//		System.out.println("UT inside: " + num_ut_inside + " UT outside: " + num_ut_outside);
//		System.out.println("OU inside: " + num_ou_inside + " OU outside: " + num_ou_outside + '\n');
		monitorLock.unlock();
	}

	public void leaveBathroomUT() {
		// Called when a UT fan wants to leave bathroom
		monitorLock.lock();
		num_ut_inside--;
		currentSpace++;
//		System.out.println("UT inside: " + num_ut_inside + " UT outside: " + num_ut_outside);
//		System.out.println("OU inside: " + num_ou_inside + " OU outside: " + num_ou_outside + '\n');
		isUT.signalAll();
		isOU.signalAll();
		monitorLock.unlock();
	}

	public void leaveBathroomOU() {
		// Called when a OU fan wants to leave bathroom
		monitorLock.lock();
		num_ou_inside--;
		currentSpace++;
//		System.out.println("UT inside: " + num_ut_inside + " UT outside: " + num_ut_outside);
//		System.out.println("OU inside: " + num_ou_inside + " OU outside: " + num_ou_outside + '\n');
		isUT.signalAll();
		isOU.signalAll();
		monitorLock.unlock();
	}
	
	private int queuePeek(Queue<Integer> q) {
		int result = 0;
		try {
			result = queue.peek();
		} catch (NullPointerException e) {
			return -1;
		}
		return result;
	}
}
