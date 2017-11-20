package byzantine;

import java.util.ArrayList;

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

    public static ArrayList<Double> getDefinedScore(ArrayList<ArrayList<Integer>> ranks, ArrayList<ArrayList<Integer>> TotalBallot){
        ArrayList<Double>  positions = new ArrayList<>();
        ArrayList<Double>  scores = new ArrayList<>();
        for (int i = 0; i < TotalBallot.get(0).size(); i++){
            int score = 0;
            for (int j = 0; j < TotalBallot.size(); j++){
                score += TotalBallot.get(j).indexOf(i);
            }
            positions.add((double) score/TotalBallot.size());
        }

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
}
