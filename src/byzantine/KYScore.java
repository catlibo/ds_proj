package byzantine;

import java.util.ArrayList;
import java.util.Arrays;

public class KYScore {

    public static int getKYScore(ArrayList<Integer> rank, ArrayList<ArrayList<Integer>> TotalBallot){
        int  score = 0;
        for (int i = 0; i < rank.size(); i++){
            for (int j = i + 1; j < rank.size(); j++){
                int a = rank.get(i);
                int b = rank.get(j);
                for (int m = 0; m < TotalBallot.size(); m++){
                    ArrayList<Integer> temp = TotalBallot.get(m);
                    if (temp.indexOf(a) < temp.indexOf(b)) score++;
                }
            }
        }
        return score;
    }

    public static Integer[] getRank(int[] rank, ArrayList<ArrayList<Integer>> TotalBallot) {
        ArrayList<ArrayList<Integer>> perm = PermutationUtil.permute(rank);
        Integer[] res = new Integer[0];
        int score = 0;
        for (int i = 0; i < perm.size(); i++) {
            int tmp = getKYScore(perm.get(i), TotalBallot);
            if (tmp > score) {
                score = tmp;
                res = perm.get(i).toArray(new Integer[0]);
            }
        }
        return res;
    }

    public static Double getDefinedScore(Integer[] rank, ArrayList<Double> positions) {
        double scores = 0;
        for (int j = 0; j < rank.length; j++){
            int a = rank[j];
            scores = scores + Math.abs(j - positions.get(a));
        }
        return scores;
    }

    public static ArrayList<Double> getDefinedScore(ArrayList<ArrayList<Integer>> ranks, ArrayList<ArrayList<Integer>> TotalBallot){
        ArrayList<Double>  positions = getExpectedAverageRank(TotalBallot);
        ArrayList<Double>  scores = new ArrayList<>();

        for(int i = 0; i < ranks.size(); i++){
            ArrayList<Integer> temp = ranks.get(i);
            double score = 0;
            for (int j = 0; j < temp.size(); j++){
                int a = temp.get(j);
                score = score + Math.abs(j - positions.get(a));
            }
            scores.add(score);
        }
        return scores;
    }

    public static ArrayList<Double> getExpectedAverageRank(ArrayList<ArrayList<Integer>> TotalBallot){
        ArrayList<Double>  positions = new ArrayList<>();
        for (int i = 0; i < TotalBallot.get(0).size(); i++){
            int score = 0;
            for (int j = 0; j < TotalBallot.size(); j++){
                score += TotalBallot.get(j).indexOf(i);
            }
            positions.add((double) score/TotalBallot.size());
        }
        return positions;
    }

    public static ArrayList<ArrayList<Integer>> transfer(int[][] blts) {
        ArrayList<ArrayList<Integer>> res = new ArrayList<>();
        for (int i = 0; i < blts.length; i++) {
            ArrayList<Integer> l = new ArrayList<>();
            for (int j = 0; j < blts[i].length; j++) {
                l.add(blts[i][j]);
            }
            res.add(l);
        }
        return res;
    }

    public static int[][] transfer(ArrayList<ArrayList<Integer>> tbs) {
        int[][] res = new int[tbs.size()][];
        for (int i = 0; i < tbs.size(); i++) {
            int[] tmp = new int[tbs.get(0).size()];
            for (int j = 0; j < tmp.length; j++) {
                tmp[j] = tbs.get(i).get(j);
            }
            res[i] = tmp;
        }
        return res;
    }
}
