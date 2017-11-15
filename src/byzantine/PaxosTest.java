package byzantine;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * This is a subset of entire test cases
 * For your reference only.
 */
public class PaxosTest {

    private int ndecided(Byzantine[] pxa, int seq){
        int counter = 0;
        Object v = null;
        Byzantine.retStatus ret;
        for(int i = 0; i < pxa.length; i++){
            if(pxa[i] != null){
                ret = pxa[i].Status(seq);
                if(ret.state == State.Decided) {
                    assertFalse("decided values do not match: seq=" + seq + " i=" + i + " v=" + v + " v1=" + ret.v, counter > 0 && !v.equals(ret.v));
                    counter++;
                    v = ret.v;
                }

            }
        }
        return counter;
    }

    private void waitn(Byzantine[] pxa, int seq, int wanted){
        int to = 10;
        for(int i = 0; i < 30; i++){
            if(ndecided(pxa, seq) >= wanted){
                break;
            }
            try {
                Thread.sleep(to);
            } catch (Exception e){
                e.printStackTrace();
            }
            if(to < 1000){
                to = to * 2;
            }
        }

        int nd = ndecided(pxa, seq);
        assertFalse("too few decided; seq=" + seq + " ndecided=" + nd + " wanted=" + wanted, nd < wanted);

    }

    private void waitmajority(Byzantine[] pxa, int seq){
        waitn(pxa, seq, (pxa.length/2) + 1);
    }

    private void cleanup(Byzantine[] pxa){
        for(int i = 0; i < pxa.length; i++){
            if(pxa[i] != null){
                pxa[i].Kill();
            }
        }
    }

    private Byzantine[] initPaxos(int npaxos){
        String host = "127.0.0.1";
        String[] peers = new String[npaxos];
        int[] ports = new int[npaxos];
        Byzantine[] pxa = new Byzantine[npaxos];
        for(int i = 0 ; i < npaxos; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }
        for(int i = 0; i < npaxos; i++){
            pxa[i] = new Byzantine(i, peers, ports, 0, 1);
        }
        return pxa;
    }

    private ByzantineKing[] initByzantineKings(int npaxos){
        String host = "127.0.0.1";
        String[] peers = new String[npaxos];
        int[] ports = new int[npaxos];
        ByzantineKing[] pxa = new ByzantineKing[npaxos];
        for(int i = 0 ; i < npaxos; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }

        for(int i = 0; i < npaxos; i++){
            pxa[i] = new ByzantineKing(i, peers, ports, 0, 1);
        }
        return pxa;
    }

    @Test
    public void TestBasic(){

        final int npaxos = 5;
        Byzantine[] generals = initPaxos(npaxos);
    }

    @Test
    public void TestByzantineKing(){

        final int npaxos = 5;
        ByzantineKing[] generals = initByzantineKings(npaxos);
        long startTime = System.currentTimeMillis();
        for(int i = 0; i < npaxos; i++){
            generals[i].Start(startTime, i);
        }
        for(int i = 0; i < npaxos; i++){
            while(generals[i].done != true){
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.print(generals[i].Status().v + "\n");
        }

        Object a = generals[0].Status().v;
    }

}