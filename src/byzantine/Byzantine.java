package byzantine;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is the main class you need to implement byzantine instances.
 */
public class Byzantine implements PaxosRMI, Runnable{

    public final static int default_value = 0;

    ReentrantLock mutex;
    String[] peers; // hostname
    int[] ports; // host port
    int me; // index into peers[]

    Registry registry;
    PaxosRMI stub;

    AtomicBoolean dead;// for testing
    AtomicBoolean unreliable;// for testing

    // Your data here
    int f;
    int phase;
    int my_value;
    List<Integer> values;
    boolean done;

    /**
     * Call the constructor to create a Byzantine peer.
     * The hostnames of all the Byzantine peers (including this one)
     * are in peers[]. The ports are in ports[].
     */
    public Byzantine(int me, String[] peers, int[] ports, int propose_value, int f){

        this.me = me;
        this.peers = peers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.dead = new AtomicBoolean(false);
        this.unreliable = new AtomicBoolean(false);

        // Your initialization code here
        this.f = f;
        this.phase = -1;
        this.my_value = propose_value;
        this.values = new ArrayList<>();
        for (int i = 0; i < peers.length; i++) {
            if (i == this.me) {
                this.values.add(propose_value);
            } else {
                this.values.add(default_value);
            }
        }
        done = false;

        // register peers, do not modify this part
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
            registry = LocateRegistry.createRegistry(this.ports[this.me]);
            stub = (PaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("Byzantine", stub);
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

        PaxosRMI stub;
        try{
            Registry registry=LocateRegistry.getRegistry(this.ports[id]);
            stub=(PaxosRMI) registry.lookup("Byzantine");
            if(rmi.equals("Round1"))
                callReply = stub.Round1(req);
            else if(rmi.equals("Round2"))
                callReply = stub.Round2(req);
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
    public void Start(){
        // Your code here
        Thread t = new Thread(this);
        t.start();
    }

    public int majority(List<Integer> nums) {
        int count=0, ret = 0;
        for (int num: nums) {
            if (count==0)
                ret = num;
            if (num!=ret)
                count--;
            else
                count++;
        }
        return ret;
    }

    @Override
    public void run(){
        //Your code here
        for (this.phase = 0; this.phase < this.f; this.phase++) {
            for (int i = 0; i < this.peers.length; i++) {
                if (i != this.me) {
                    Request req = new Request(this.values.get(this.me), this.me, "Round1");
                    Response rsp = this.Call("Round1", req, i);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.mutex.lock();
            this.my_value = majority(this.values);
            this.mutex.unlock();

            if (this.phase == this.me) {
                for (int i = 0; i < this.peers.length; i++) {
                    if (i != this.me) {
                        Request req = new Request(this.my_value, this.me, "Round1");
                        Response rsp = this.Call("Round2", req, i);
                    }
                }
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mutex.lock();
        this.done = true;
        this.mutex.unlock();
    }

    // RMI handler
    public Response Round1(Request req){
        // your code here
        this.mutex.lock();

        int j = req.me;
        this.values.set(j, req.v);

        this.mutex.unlock();
        return new Response(true);
    }

    public Response Round2(Request req){
        // your code here
        this.mutex.lock();

        int j = req.me;
        int queen_value = req.v;
        int count = 0;
        for (int i = 0; i < this.values.size(); i++) {
            if (this.values.get(i) == this.my_value) {
                count++;
            }
        }
        if (count >= this.peers.length / 2 + this.f + 1) {
            this.values.set(this.me, this.my_value);
        } else {
            this.values.set(this.me, queen_value);
        }

        this.mutex.unlock();
        return new Response(true);
    }

    public Response Receive(Request req){
        this.mutex.lock();

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
    public retStatus Status(int seq){
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
