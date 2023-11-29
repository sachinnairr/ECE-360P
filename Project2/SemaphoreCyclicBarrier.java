// svn343
// amn2867

import java.util.concurrent.Semaphore;

/* Use only semaphores to accomplish the required synchronization */
public class SemaphoreCyclicBarrier implements CyclicBarrier {

	// TODO Add other useful variables
	int parties;
    int arrived;
    boolean status;
    Semaphore mutex;
    Semaphore sem1;
    Semaphore sem2;
	
	public SemaphoreCyclicBarrier(int parties) {
		// TODO Add any other initialization statements
		this.parties = parties;
        this.arrived = 0;
        this.status = true;
        this.mutex = new Semaphore(1);
        this.sem1 = new Semaphore(0);
        this.sem2 = new Semaphore(1);
	}
	
	/*
	 * An active CyclicBarrier waits until all parties have invoked await on this
	 * CyclicBarrier. If the current thread is not the last to arrive then it is
	 * disabled for thread scheduling purposes and lies dormant until the last
	 * thread arrives. An inactive CyclicBarrier does not block the calling thread.
	 * It instead allows the thread to proceed by immediately returning. Returns:
	 * the arrival index of the current thread, where index 0 indicates the first to
	 * arrive and (parties-1) indicates the last to arrive.
	 */
	public int await() throws InterruptedException {
		// TODO Implement this function
		if(!status) {
			return 0;
		}else {
			int index = 0;
	        mutex.acquire();
	        index = arrived;
	        arrived++;

	        if(arrived == parties) {
	            sem2.acquire();
	            sem1.release();
	        }
	        mutex.release();

	        sem1.acquire();
	        sem1.release();

	        mutex.acquire();
	        arrived--;

	        if(arrived == 0){
	            sem1.acquire();
	            sem2.release();
	        }
	        mutex.release();
	        sem2.acquire();
	        sem2.release();
	        return index;
		}
	}

	/*
	 * This method activates the cyclic barrier. If it is already in the active
	 * state, no change is made. If the barrier is in the inactive state, it is
	 * activated and the state of the barrier is reset to its initial value.
	 */
	public void activate() throws InterruptedException {
		// TODO Implement this function
		if(!status) {
			status = true;
			arrived = 0;
		}
		return;
	}

	/*
	 * This method deactivates the cyclic barrier. It also releases any waiting
	 * threads
	 */
	public void deactivate() throws InterruptedException {
		// TODO Implement this function
		status = false;
		return;
	}
}