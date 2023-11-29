package paxos;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is the main class you need to implement paxos instances.
 * It corresponds to a single Paxos peer.
 */
public class Paxos implements PaxosRMI, Runnable {
    ReentrantLock mutex;
    String[] peers; // hostnames of all peers
    int[] ports; // ports of all peers
    int me; // this peer's index into peers[] and ports[]

    Registry registry;
    PaxosRMI stub;

    AtomicBoolean dead; // for testing
    AtomicBoolean unreliable; // for testing

    // Your data here
    int highestDoneSeq = -1;
    ConcurrentHashMap<Integer, Instance> instances = new ConcurrentHashMap<>();
    ConcurrentLinkedQueue<Instance> instancesQ = new ConcurrentLinkedQueue<>();
    ArrayList<Integer> doneList = new ArrayList<>();

    public class Instance {
        int sequence;
        int n;  //highest proposal num
        int np; //highest prep num
        int na; //highest accept num
        Object va;  //highest accept val
        Object myVal;   //val started
        Object decidedVal;  //val decided
        State s;

        public Instance(int seq) {
            this.sequence = seq;
            np = 0;
            na = 0;
            n = 0;
            va = null;
            s = State.Pending;
        }
    }
    
    /**
     * Call the constructor to create a Paxos peer.
     * The hostnames of all the Paxos peers (including this one)
     * are in peers[]. The ports are in ports[].
     */
    public Paxos(int me, String[] peers, int[] ports) {
        this.me = me;
        this.peers = peers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.dead = new AtomicBoolean(false);
        this.unreliable = new AtomicBoolean(false);

        // Your initialization code here
        for (int i = 0; i < peers.length; i++) {
            this.doneList.add(i, -1);
        }

        // register peers, do not modify this part
        try {
            System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
            registry = LocateRegistry.createRegistry(this.ports[this.me]);
            stub = (PaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("Paxos", stub);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Call() sends an RMI to the RMI handler on server with
     * arguments rmi name, request message, and server id. It
     * waits for the reply and return a response message if
     * the server responded, and return null if Call() was not
     * be able to contact the server.
     *
     * You should assume that Call() will time out and return
     * null after a while if it doesn't get a reply from the server.
     *
     * Please use Call() to send all RMIs and please don't change
     * this function.
     */
    public Response Call(String rmi, Request req, int id) {
        Response callReply = null;

        PaxosRMI stub;
        try {
            Registry registry = LocateRegistry.getRegistry(this.ports[id]);
            stub = (PaxosRMI) registry.lookup("Paxos");
            if (rmi.equals("Prepare"))
                callReply = stub.Prepare(req);
            else if (rmi.equals("Accept"))
                callReply = stub.Accept(req);
            else if (rmi.equals("Decide"))
                callReply = stub.Decide(req);
            else
                System.out.println("Wrong parameters!");
        } catch (Exception e) {
            return null;
        }
        return callReply;
    }

    /**
     * The application wants Paxos to start agreement on instance seq,
     * with proposed value v. Start() should start a new thread to run
     * Paxos on instance seq. Multiple instances can be run concurrently.
     *
     * Hint: You may start a thread using the runnable interface of
     * Paxos object. One Paxos object may have multiple instances, each
     * instance corresponds to one proposed value/command. Java does not
     * support passing arguments to a thread, so you may reset seq and v
     * in Paxos object before starting a new thread. There is one issue
     * that variable may change before the new thread actually reads it.
     * Test won't fail in this case.
     *
     * Start() just starts a new thread to initialize the agreement.
     * The application will call Status() to find out if/when agreement
     * is reached.
     */
    public void Start(int seq, Object value) {
        // Your code here
        mutex.lock();
        if(!instances.containsKey(seq)){
            Instance inst = new Instance(seq);
            instances.put(seq, inst);
        }
        Instance inst = instances.get(seq);
        inst.myVal = value;
        instancesQ.add(inst);
        mutex.unlock();
        
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        //Your code here
        Instance inst = instancesQ.remove();
        while(inst.s != State.Decided){
            mutex.lock();
            int n = inst.np*peers.length + me + 1;
            Request pReq = new Request(inst.sequence, n, inst.myVal);
            mutex.unlock();

            Object fail = null;
            Object tempVal = pReq.value;
            int acceptCount = 0;
            int highestNA = pReq.n;

            for(int i = 0; i < peers.length; i++){
                Response pResp;
                if(me != i){
                    pResp = Call("Prepare",pReq,i);
                }else{
                    pResp = Prepare(pReq);
                }
                if(pResp != null && pResp.prepareOK){
                    acceptCount++;
                    if(pResp.na > highestNA){
                        highestNA = pResp.na;
                        tempVal = pResp.va;
                    }
                }else if(pResp!= null && pResp.va != null){
                    fail = pResp.va;
                }
            }
            Response pResp = new Response();
            if(acceptCount >= peers.length/2 + 1){
                pResp.prepareMaj = true;
                pResp.n = pReq.n;
                pResp.v = tempVal;
            }else{
                pResp.v = fail;
            }

            if(pResp.prepareMaj){
                Request aReq = new Request(inst.sequence, pResp.n, pResp.v);
                acceptCount = 0;
                for(int i = 0; i < peers.length;i++){
                    Response aResp;
                    if(me != i){
                        aResp = Call("Accept",aReq,i);
                    }else{
                        aResp = Accept(aReq);
                    }
                    if(aResp != null && aResp.acceptOK){
                        acceptCount++;
                    }

                }

                Response aResp = new Response();
                if(acceptCount >= peers.length/2 + 1){
                    aResp.acceptMaj = true;
                    aResp.n = aReq.n;
                    aResp.v = aReq.value;
                }
            
                if(aResp.acceptMaj){
                    Request dReq = new Request(inst.sequence, aResp.n, aResp.v);
                    for(int i = 0; i < peers.length;i++){
                        Response dResp;
                        if(me != i){
                            dResp = Call("Decide",dReq,i);
                        }else{
                            dResp = Decide(dReq);
                        }
                        mutex.lock();
                        if(dResp != null) {
                            if (this.doneList.get(dResp.peerNum) < dResp.maxSeq) {
                                this.doneList.set(dResp.peerNum, dResp.maxSeq);
                            }
                        }
                        mutex.unlock();
                    }
                }   
            }else{
                inst.myVal = pResp.v;
            }
        }
    }

    // RMI Handler for prepare requests
    public Response Prepare(Request req) {
        // your code here
        mutex.lock();
        if(!instances.containsKey(req.seq)){
            Instance inst = new Instance(req.seq);
            instances.put(req.seq, inst);
        }
        Instance inst = instances.get(req.seq);
        Response res = new Response();

        if(req.n > inst.np){
            inst.np = req.n;
            res.prepareOK = true;
        }

        res.n = req.n;
        res.na = inst.na;
        res.va = inst.va;

        mutex.unlock();
        return res;
    }

    // RMI Handler for accept requests
    public Response Accept(Request req) {
        // your code here
        mutex.lock();
        if(!instances.containsKey(req.seq)){
            Instance inst = new Instance(req.seq);
            instances.put(req.seq, inst);
        }
        Instance inst = instances.get(req.seq);
        Response res = new Response();

        if(req.n >= inst.np){
            inst.np = req.n;
            inst.na = req.n;
            inst.va = req.value;
            res.acceptOK = true;
        }
        
        res.n = req.n;
        mutex.unlock();
        return res;
    }

    // RMI Handler for decide requests
    public Response Decide(Request req) {
        // your code here
        mutex.lock();
        Response res;

        if(!instances.containsKey(req.seq)){
            Instance inst = new Instance(req.seq);
            instances.put(req.seq, inst);
        }
        Instance inst = instances.get(req.seq);

        inst.np = req.n;
        inst.na = req.n;
        inst.decidedVal = req.value;
        inst.s = State.Decided;

        res = new Response();
        res.maxSeq = this.highestDoneSeq;
        res.peerNum = this.me;
        mutex.unlock();
        return res;
    }

    /**
     * The application on this machine is done with
     * all instances <= seq.
     *
     * see the comments for Min() for more explanation.
     */
    public void Done(int seq) {
        // Your code here
        mutex.lock();
        doneList.set(me, seq);
        highestDoneSeq = this.doneList.get(me);
        for(int doneSeq : this.instances.keySet()){
            if(doneSeq < seq){
                this.instances.remove(doneSeq);
            }
        }
        mutex.unlock();
    }

    /**
     * The application wants to know the
     * highest instance sequence known to
     * this peer.
     */
    public int Max() {
        // Your code here
        mutex.lock();
        int max = Integer.MIN_VALUE;
        for(int seq : this.instances.keySet()){
            if(seq > max){
                max = seq;
            }
        }
        mutex.unlock();
        return max;
    }

    /**
     * Min() should return one more than the minimum among z_i,
     * where z_i is the highest number ever passed
     * to Done() on peer i. A peers z_i is -1 if it has
     * never called Done().

     * Paxos is required to have forgotten all information
     * about any instances it knows that are < Min().
     * The point is to free up memory in long-running
     * Paxos-based servers.

     * Paxos peers need to exchange their highest Done()
     * arguments in order to implement Min(). These
     * exchanges can be piggybacked on ordinary Paxos
     * agreement protocol messages, so it is OK if one
     * peers Min does not reflect another Peers Done()
     * until after the next instance is agreed to.

     * The fact that Min() is defined as a minimum over
     * all Paxos peers means that Min() cannot increase until
     * all peers have been heard from. So if a peer is dead
     * or unreachable, other peers Min()s will not increase
     * even if all reachable peers call Done. The reason for
     * this is that when the unreachable peer comes back to
     * life, it will need to catch up on instances that it
     * missed -- the other peers therefore cannot forget these
     * instances.
     */
    public int Min() {
        // Your code here
        mutex.lock();
        int min = Integer.MAX_VALUE;
        for(int seq : doneList){
            if(seq < min){
                min = seq + 1;
            }
        }
        mutex.unlock();
        return min;
    }

    /**
     * The application wants to know whether this
     * peer thinks an instance has been decided,
     * and if so what the agreed value is. Status()
     * should just inspect the local peer state;
     * it should not contact other Paxos peers.
     */
    public retStatus Status(int seq) {
        // Your code here
        mutex.lock();
        if(!instances.containsKey(seq)){
            Instance inst = new Instance(seq);
            instances.put(seq, inst);
        }
        Instance inst = instances.get(seq);
        retStatus ret = new retStatus(inst.s, inst.decidedVal);

        if(seq < Min() - 1) {
            ret.state = State.Forgotten;
        }
        mutex.unlock();
        return ret;
    }

    /**
     * helper class for Status() return
     */
    public class retStatus {
        public State state;
        public Object v;

        public retStatus(State state, Object v) {
            this.state = state;
            this.v = v;
        }
    }

    /**
     * Tell the peer to shut itself down.
     * For testing.
     * Please don't change these four functions.
     */
    public void Kill() {
        this.dead.getAndSet(true);
        if (this.registry != null) {
            try {
                UnicastRemoteObject.unexportObject(this.registry, true);
            } catch (Exception e) {
                System.out.println("None reference");
            }
        }
    }

    public boolean isDead() {
        return this.dead.get();
    }

    public void setUnreliable() {
        this.unreliable.getAndSet(true);
    }

    public boolean isunreliable() {
        return this.unreliable.get();
    }
}
