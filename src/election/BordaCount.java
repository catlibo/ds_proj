package election;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by qin on 2017/11/21.
 */
public class BordaCount {

    private class Point implements Comparable<Point>{
        int key;
        int count;

        public Point(int key) {
            this.key = key;
            this.count = 0;
        }

        @Override
        public int compareTo(Point o) {
            return o.count - this.count;
        }
    }

    int[][] ballots;
    int num_cands;
    List<Point> rank;

    public BordaCount(int[][] ballots, int num_cands) {
        this.ballots = ballots;
        this.num_cands = num_cands;
        this.rank = new ArrayList<>();
        for (int i = 0; i < num_cands; i++) {
            this.rank.add(new Point(i));
        }
    }

    public void tally() {
        for (int[] b:ballots) {
            for (int i = 0; i < b.length; i++) {
                Point p = rank.get(b[i]);
                p.count += (num_cands - i - 1);
                rank.set(b[i], p);
            }
        }
    }

    public Integer[] getRank(){
        this.tally();
        Integer[] res = new Integer[num_cands];
        Collections.sort(rank);
        for (int i = 0; i < num_cands; i++) {
            res[i] = rank.get(i).key;
        }
        return res;
    }


    public static void main(String[] args) {
        int[][] tbs = {{1,2,0}, {1,2,0}, {1,0,2}, {1,2,0}, {2,0,1}, {2,0,1}, {2,0,1}};
        BordaCount b = new BordaCount(tbs, 3);
        System.out.println(Arrays.toString(b.getRank()));
    }
}
