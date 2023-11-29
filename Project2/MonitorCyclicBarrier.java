import javax.management.monitor.Monitor;

// svn343
// amn2867

/* Use only Java monitors to accomplish the required synchronization */
public class MonitorCyclicBarrier implements CyclicBarrier {

    private int parties;
    private int arrived = 0;
    private boolean status = true;
    private Monitor monitor = new Monitor() {

        @Override
        public void start() {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void stop() {
            // TODO Auto-generated method stub
            
        }
        
    };
    
    // TODO Add other useful variables

    public MonitorCyclicBarrier(int parties) {
        this.parties = parties;

        // TODO Add any other initialization statements
    }

    /*
     * An active CyclicBarrier waits until all parties have invoked
     * await on this CyclicBarrier. If the current thread is not
     * the last to arrive then it is disabled for thread scheduling
     * purposes and lies dormant until the last thread arrives.
     * An inactive CyclicBarrier does not block the calling thread. It
     * instead allows the thread to proceed by immediately returning.
     * Returns: the arrival index of the current thread, where index 0
     * indicates the first to arrive and (parties-1) indicates
     * the last to arrive.
     */
    public int await() throws InterruptedException {
        // TODO Implement this function
        int index = -1;
        synchronized (monitor) {
            if(status == false){
                return 0;
            }

            arrived++;
            if (arrived == parties) {
                arrived = 0;
                monitor.notifyAll();
            } else {
                monitor.wait();
                index = parties - arrived - 1;
            }
        }
        return index;
    }
    

    /*
     * This method activates the cyclic barrier. If it is already in
     * the active state, no change is made.
     * If the barrier is in the inactive state, it is activated and
     * the state of the barrier is reset to its initial value.
     */
    public void activate() throws InterruptedException {
        // TODO Implement this function
        synchronized (monitor) {
            if (!status) {
                status = true;
                arrived = 0;
            }
        }
    }

    /*
     * This method deactivates the cyclic barrier.
     * It also releases any waiting threads
     */
    public void deactivate() throws InterruptedException {
        // TODO Implement this function
        synchronized(monitor){
            status = false;
            monitor.notifyAll();
        }
    }
}