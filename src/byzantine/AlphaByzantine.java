package byzantine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by qin on 2017/11/15.
 */
public class AlphaByzantine {


    private ByzantineKing[] initByzantineKings(int nbyzantine, int f_index){
        String host = "127.0.0.1";
        String[] peers = new String[nbyzantine];
        int[] ports = new int[nbyzantine];
        ByzantineKing[] bgs = new ByzantineKing[nbyzantine];

        for(int i = 0 ; i < nbyzantine; i++){
            ports[i] = (int)(Math.random() * 2000) + i + 10000;
            peers[i] = host;
        }

        for(int i = 0; i < nbyzantine; i++) {
            bgs[i] = new ByzantineKing(i, peers, ports, 0, 0);
            if (i <= f_index) {
                bgs[i].tag = 1;
            }
        }
        return bgs;
    }

    public int runByzantine(int[] T, int round, int f_index) {
        int l = T.length;
        ByzantineKing[] generals = initByzantineKings(l, f_index);
        long startTime = System.currentTimeMillis();
        for(int i = 0; i < l; i++){
            generals[i].Start(startTime, T[i]);
        }
        for(int i = 0; i < l; i++){
            while(generals[i].Status().state != State.Decided){
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println(String.format("General[%d] agree voter[%d] vote for (%s)", i, round, generals[i].Status().v.toString()));
        }
        return Integer.valueOf(generals[1].Status().v.toString());
    }

    public static void alpha(int[] tops, int f_index) {
        int[] real_ballots = new int[tops.length];
        for (int round = 0; round < tops.length; round++) {
            int[] t = new int[tops.length];
            for (int gi = 0; gi < tops.length; gi++) {
                if (round <= f_index) {
                    t[gi] = (int) (Math.random() * 3);
                } else {
                    t[gi] = tops[round];
                }
            }
            System.out.println("Top for voter[" + round + "] is " + Arrays.toString(t));
            AlphaByzantine ab = new AlphaByzantine();
            real_ballots[round] = ab.runByzantine(t, round, f_index);
            System.out.println("voter[" + round + "] vote for (" + real_ballots[round] + ")\n");
        }
        System.out.println("Consistent ballots are "+ Arrays.toString(real_ballots) + ", election result is " + elect(real_ballots));
    }

    public static void alphaAll(int[][] tbs) {

    }

    public static int elect(int[] ballots) {
        Map<Integer, Integer> count = new HashMap<>();
        Integer cand = -1, num = 0;
        for (int key: ballots) {
            if (count.containsKey(key)) {
                count.put(key, count.get(key) + 1);
            } else {
                count.put(key, 1);
            }
        }
        for (int key: count.keySet()) {
            int c = count.get(key);
            if (c > num) {
                num = c;
                cand = key;
            } else if (c == num && key < cand) {
                cand = key;
            }
        }
        return cand;
    }
}
