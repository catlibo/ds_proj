package byzantine;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is the main class you need to implement byzantine instances.
 */
public class ByzantineKing implements ByzantineKingRMI, Runnable{

    public final static int default_value = 0;
    public final static int pulse = 20;

    ReentrantLock mutex;
    String[] peers; // hostname
    int[] ports; // host port
    int me; // index into peers[]

    Registry registry;
    ByzantineKingRMI stub;

    AtomicBoolean dead;// for testing
    AtomicBoolean unreliable;// for testing

    // Your data here
    int tag;// 0: good; 1: bad_1
    int f;
    int phase;
    int my_value;
    ArrayList<Integer> values;
    Map<Integer, Integer> receivedProposal;
    boolean done;

    long startTime;

    Integer[] firstTransfer;
    /**
     * Call the constructor to create a Byzantine peer.
     * The hostnames of all the Byzantine peers (including this one)
     * are in peers[]. The ports are in ports[].
     */
    public ByzantineKing(int me, String[] peers, int[] ports, int propose_value, int tag){

        this.me = me;
        this.peers = peers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.dead = new AtomicBoolean(false);
        this.unreliable = new AtomicBoolean(false);

        // Your initialization code here
        this.tag = tag;
        this.f = (this.peers.length - 1) / 3;
        this.phase = -1;
        this.my_value = propose_value;
        this.values = new ArrayList<>();
        for (int i = 0; i < peers.length; i++) {
            if (i == this.me) {
                this.values.add(propose_value);
            } else {
                this.values.add(-1);
            }
        }
        done = false;

        receivedProposal = new HashMap<Integer, Integer>();
        firstTransfer = new Integer[peers.length];
        //this.startTime = startTime;

        // register peers, do not modify this part
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
            registry = LocateRegistry.createRegistry(this.ports[this.me]);
            stub = (ByzantineKingRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("ByzantineKing", stub);
        } catch(Exception e){
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
    public Response Call(String rmi, Request req, int id){
        Response callReply = null;

        ByzantineKingRMI stub;
        try{
            Registry registry=LocateRegistry.getRegistry(this.ports[id]);
            stub=(ByzantineKingRMI) registry.lookup("ByzantineKing");
            if(rmi.equals("Round1"))
                callReply = stub.Round1(req);
            else if(rmi.equals("Round2"))
                callReply = stub.Round2(req);
            else if(rmi.equals("Round3"))
                callReply = stub.Round3(req);
            else if(rmi.equals("Receive"))
                callReply = stub.Receive(req);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }


    /**
     * The application wants Byzantine to start agreement on instance seq,
     * with proposed value v. Start() should start a new thread to run
     * Byzantine on instance seq. Multiple instances can be run concurrently.
     *
     * Hint: You may start a thread using the runnable interface of
     * Byzantine object. One Byzantine object may have multiple instances, each
     * instance corresponds to one proposed value/command. Java does not
     * support passing arguments to a thread, so you may reset seq and v
     * in Byzantine object before starting a new thread. There is one issue
     * that variable may change before the new thread actually reads it.
     * Test won't fail in this case.
     *
     * Start() just starts a new thread to initialize the agreement.
     * The application will call Status() to find out if/when agreement
     * is reached.
     */
    public void Start(long startTime){
        // Your code here
        this.startTime = startTime;
        Thread t = new Thread(this);
        t.start();
        //run();
    }

    public static int generateRandom(int b) {
        return (int) (Math.random() * b);
    }

    public void Start(long startTime, int proposalValue){
        // Your code here
        this.mutex.lock();
        //System.out.println("start lock:" + Thread.currentThread());
        this.startTime = startTime;
        this.my_value = proposalValue;
        this.values.set(this.me, proposalValue);
        this.mutex.unlock();
        //System.out.println("start unlock:" + Thread.currentThread());
        Thread t = new Thread(this);
        t.start();
    }

    public Integer enoughInitial(ArrayList<Integer> nums) {
        int bound = peers.length - this.f;
        Map<Integer, Integer> temp = new HashMap<Integer, Integer>();
        for (int num: nums) {
            if(temp.containsKey(num)){
                if (temp.get(num) + 1 >= bound) return num;
                temp.put(num, temp.get(num) + 1);
            }else{
                temp.put(num, 1);
            }
        }
        return null;
    }

    @Override
    public void run(){
        //Your code here

        for (this.phase = 0; this.phase < (this.f+1) ; this.phase++) {
            for (int i = 0; i < this.peers.length; i++) {
                if (i != this.me) {
                    Request req;
                    if (this.tag == 1) { //bad
                        req = new Request(ByzantineKing.generateRandom(3), this.me, "Round1", 1);
                    } else {
                        req = new Request(this.values.get(this.me), this.me, "Round1", 1);
                    }
                    Response rsp = this.Call("Round1", req, i);
                }
            }

            while(this.startTime + pulse > System.currentTimeMillis()){
                try {
                    Thread.sleep(pulse/2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.startTime = this.startTime + pulse;


            Integer enoughInit = enoughInitial(this.values);
            if(enoughInit!= null || this.tag == 1){
                for (int i = 0; i < this.peers.length; i++) {
                    Request req;
                    if (this.tag == 1) { //bad
                        req = new Request(ByzantineKing.generateRandom(3), this.me, "Round2", 2);
                    } else {
                        req = new Request(enoughInit, this.me, "Round2", 2);
                    }
                    Response rsp = this.Call("Round2", req, i);
                }
            }

            while(this.startTime + pulse > System.currentTimeMillis()){
                try {
                    Thread.sleep(pulse/2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.startTime = this.startTime + pulse;

            if (this.phase == this.me) {
                for (int i = 0; i < this.peers.length; i++) {
                    Request req;
                    if (this.tag == 1) { //bad
                        req = new Request(ByzantineKing.generateRandom(3), this.me, "Round3", 3);
                    } else {
                        req = new Request(this.values.get(this.me), this.me, "Round3", 3);
                    }
                    Response rsp = this.Call("Round3", req, i);
                }
            }

            while(this.startTime + pulse > System.currentTimeMillis()){
                try {
                    Thread.sleep(pulse/2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.startTime = this.startTime + pulse;

            receivedProposal = new HashMap<Integer, Integer>();

            while(this.startTime + pulse > System.currentTimeMillis()){
                try {
                    Thread.sleep(pulse/2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.startTime = this.startTime + pulse;

        }

        this.mutex.lock();
        this.done = true;
        this.my_value = this.values.get(this.me);
        this.mutex.unlock();
    }

    // RMI handler
    public Response Round1(Request req){
        // your code here
        this.mutex.lock();
        //if (this.me == 0) System.out.println("Round1 lock:" + Thread.currentThread());

        int j = req.me;
        this.values.set(j, req.v);

        this.mutex.unlock();
        //if (this.me == 0) System.out.println("Round1 unlock:" + Thread.currentThread());
        return new Response(true);
    }

    public Response Round2(Request req){
        // your code here
        this.mutex.lock();
        //if (this.me == 0) System.out.println("Round2 lock:" + Thread.currentThread());
        int proposedValue = req.v;
        if (!receivedProposal.containsKey(proposedValue)){
            receivedProposal.put(proposedValue, 1);
        }else{
            receivedProposal.put(proposedValue, receivedProposal.get(proposedValue) + 1);
        }

        if(receivedProposal.get(proposedValue) >= this.f + 1){
            this.values.set(this.me, proposedValue);
        }

        this.mutex.unlock();
        //if (this.me == 0) System.out.println("Round2 unlock:" + Thread.currentThread());
        return new Response(true);
    }

    public Response Round3(Request req){
        // your code here
        this.mutex.lock();
        //if (this.me == 0) System.out.println("Round3 lock:" + this.me);
        int j = req.me;
        int king_value = req.v;
        Integer count = receivedProposal.get(this.values.get(this.me));
        if (count != null && count >= this.peers.length - this.f) {
            this.values.set(this.me, this.values.get(this.me));
        } else {
            this.values.set(this.me, king_value);
        }

        this.mutex.unlock();
        //if (this.me == 0) System.out.println("Round3 unlock:" + this.me);
        return new Response(true);
    }

    public void firstTransfer(int proposalValue, long startTime){
        // Your code here
        this.mutex.lock();
        //System.out.println("start lock:" + Thread.currentThread());
        long transferTime = System.currentTimeMillis();
        this.mutex.unlock();
        //System.out.println("start unlock:" + Thread.currentThread());

        for (int i = 0; i < this.peers.length; i++) {
            if (i != this.me) {
                Request req;
                if (this.tag == 1) { //bad
                    req = new Request(ByzantineKing.generateRandom(3), this.me, "Receive", 1);
                } else {
                    req = new Request(this.values.get(this.me), this.me, "Receive", 1);
                }
                Response rsp = this.Call("Receive", req, i);
            }
            else firstTransfer[i] = proposalValue;
        }

        while(transferTime + pulse > System.currentTimeMillis()){
            try {
                Thread.sleep(pulse/2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<Integer> getTransfer(){
        return new ArrayList<Integer>(Arrays.asList(firstTransfer));
    }

    public Response Receive(Request req){
        this.mutex.lock();
        this.firstTransfer[req.me] = req.v;
        this.mutex.unlock();
        return new Response(true);
    }
    /**
     * The application on this machine is done with
     * all instances <= seq.
     *
     * see the comments for Min() for more explanation.
     */
    public void Done(int seq) {
        // Your code here
    }



    /**
     * the application wants to know whether this
     * peer thinks an instance has been decided,
     * and if so what the agreed value is. Status()
     * should just inspect the local peer state;
     * it should not contact other Byzantine peers.
     */
    public retStatus Status(){
        // Your code here
        this.mutex.lock();

        retStatus r = new retStatus(State.Pending, this.values.get(this.me));
        if (done) {
            r.state = State.Decided;
        }

        this.mutex.unlock();
        return r;
    }

    /**
     * helper class for Status() return
     */
    public class retStatus{
        public State state;
        public Object v;

        public retStatus(State state, Object v){
            this.state = state;
            this.v = v;
        }
    }

    /**
     * Tell the peer to shut itself down.
     * For testing.
     * Please don't change these four functions.
     */
    public void Kill(){
        this.dead.getAndSet(true);
        if(this.registry != null){
            try {
                UnicastRemoteObject.unexportObject(this.registry, true);
            } catch(Exception e){
                System.out.println("None reference");
            }
        }
    }

    public boolean isDead(){
        return this.dead.get();
    }

    public void setUnreliable(){
        this.unreliable.getAndSet(true);
    }

    public boolean isunreliable(){
        return this.unreliable.get();
    }


}
