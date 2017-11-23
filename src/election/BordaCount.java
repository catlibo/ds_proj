package election;

import java.util.*;

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

    public Double getDefinedScore(Integer[] rank, int[][] totalBallots) {
        ArrayList<Double>  positions = new ArrayList<>();
        Double scores = 0.0;
        for (int i = 0; i < totalBallots[0].length; i++){
            int score = 0;
            for (int j = 0; j < totalBallots.length; j++){
                List<Integer> tmp = new ArrayList<>();
                for (int index = 0; index < totalBallots[j].length; index++)
                {
                    tmp.add(totalBallots[j][index]);
                }
                score += tmp.indexOf(i);
            }
            positions.add((double) score/totalBallots.length);
        }
        for (int j = 0; j < rank.length; j++){
            int a = rank[j];
            scores = scores + Math.abs(j - positions.get(a));
        }
        return scores;
    }


    public static void main(String[] args) {
        int[][] tbs = {{1,2,0}, {1,2,0}, {1,2,0}, {2,0,1}, {2,0,1}, {2,0,1}, {1,2,0}};
        BordaCount b = new BordaCount(tbs, 3);
        System.out.println(Arrays.toString(b.getRank()));
        System.out.println(b.getDefinedScore(b.getRank(), tbs));
        int[][] tbs2 = {{2,1,0}, {2,1,0}, {2,1,0}, {0,1,2}, {0,1,2}};
        BordaCount b2 = new BordaCount(tbs2, 3);
        System.out.println(Arrays.toString(b2.getRank()));
        System.out.println(b2.getDefinedScore(b.getRank(), tbs2));
    }
}
