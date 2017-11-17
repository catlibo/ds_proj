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
}
