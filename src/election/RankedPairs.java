package election;

import java.util.*;

/**
 * Created by qin on 2017/11/20.
 */
public class RankedPairs {

    private class CandNode {
        int key;
        int indegree;
        Set<Integer> children;

        public CandNode(int key) {
            this.key = key;
            this.indegree = 0;
            this.children = new HashSet<>();
        }
    }

    private class Pair implements Comparable<Pair>{
        int first;
        int second;
        int fst_ct;
        int snd_ct;
        int count;

        public Pair(int first, int second) {
            this.first = first;
            this.second = second;
        }

        public void setWinner() {
            if (fst_ct >= snd_ct) {
                count = fst_ct;
            } else {
                count = snd_ct;
                int tmp = first;
                first = second;
                second = tmp;
            }
        }

        @Override
        public int compareTo(Pair o) {
            return o.count - this.count;
        }
    }

    int[][] ballots;
    int num_cands;
    int[] cands;
    List<Pair> rank;
    Map<Integer, Pair> store;
    Map<Integer, CandNode> graph;
    Map<Integer, Set<Integer>> path;

    public RankedPairs(int[][] ballots, int num_cands) {
        this.ballots = ballots;
        this.num_cands = num_cands;
        this.cands = new int[this.num_cands];
        this.rank = new ArrayList<>();
        this.store = new HashMap<>();
        this.path = new HashMap<>();
        this.graph = new HashMap<>();
        for (int i = 0; i < this.num_cands - 1; i++) {
            for (int j = i + 1; j < this.num_cands; j++) {
                this.store.put(i * this.num_cands + j, new Pair(i, j));
            }
        }
        for (int i = 0; i < this.num_cands; i++) {
            this.path.put(i, new HashSet<>());
            this.graph.put(i, new CandNode(i));
        }
    }

    public void tally() {
        for (int[] ballot:this.ballots) {
            for (int i = 0; i < ballot.length - 1; i++) {
                for (int j = i + 1; j < ballot.length; j++) {
                    Pair p;
                    if (ballot[i] < ballot[j]) {
                        p = store.get(ballot[i] * num_cands + ballot[j]);
                        p.fst_ct++;
                    } else {
                        p = store.get(ballot[j] * num_cands + ballot[i]);
                        p.snd_ct++;
                    }
                }
            }
        }
    }

    public void sort() {
        for (Integer key:this.store.keySet()) {
            Pair p = store.get(key);
            p.setWinner();
            rank.add(p);
        }
        Collections.sort(rank);
    }

    public void lock() {
        for (Pair p:this.rank){
            if (!path.get(p.second).contains(p.first)) {
                Set<Integer> s = path.get(p.first);
                s.add(p.second);
                path.put(p.first, s);
                for (Integer key :this.path.keySet()) {
                    if (path.get(key).contains(p.first)) {
                        s = path.get(key);
                        s.add(p.second);
                        path.put(key, s);
                    }
                }
                CandNode c = graph.get(p.first);
                c.children.add(p.second);
                graph.put(p.first, c);
                c = graph.get(p.second);
                c.indegree++;
                graph.put(p.second, c);
            }
        }
    }

    public Integer[] find() {
        List<Integer> l = new ArrayList<>();
        for (int round = 0; round < this.num_cands; round++) {
            for (int i = 0; i < this.num_cands; i++) {
                if (graph.containsKey(i)) {
                    if (graph.get(i).indegree == 0) {
                        l.add(i);
                        for (int key : graph.get(i).children) {
                            CandNode c = graph.get(key);
                            c.indegree--;
                            graph.put(key, c);
                        }
                        graph.remove(i);
                    }
                }
            }
        }
        return l.toArray(new Integer[0]);
    }

    public Integer[] getRank() {
        this.tally();
        this.sort();
        this.lock();
        return this.find();
    }

    public static void main(String[] args) {
        int[][] tbs = {{1,2,0}, {1,2,0}, {1,0,2}, {1,2,0}, {2,0,1}, {2,0,1}, {2,0,1}};
        RankedPairs r = new RankedPairs(tbs, 3);
        System.out.println(Arrays.toString(r.getRank()));
    }
}
