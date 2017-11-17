package byzantine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BetaNode {

    int numPeers;
    ByzantineKing myByzantine;
    ArrayList<Integer> vote;
    ArrayList<Integer> T;
    ArrayList<Integer> TFirst;
    ArrayList<ArrayList<Integer>> Total;
    ArrayList<Integer> Discard;
    int me;
    int leader;

    BetaNode(ArrayList<Integer> vote, ByzantineKing byzantineKing, int numPeers, int me ){
        this.myByzantine = byzantineKing;
        this.vote = vote;
        this.numPeers = numPeers;
        this.me = me;
        this.T = new ArrayList<>();
        this.TFirst = new ArrayList<>();
        this.Discard = new ArrayList<>();
        this.leader = -1;
        this.Total = new ArrayList<>(new ArrayList<>());

        for(int i =0; i < numPeers; i++){
            if (i != me) {
                T.add(-1);
                TFirst.add(-1);
            }
            else {
                T.add(vote.get(vote.size() - 1));
                TFirst.add(vote.get(0));
            }
        }

        for(int j = 0; j < vote.size(); j++){
            ArrayList<Integer> temp = new ArrayList<>();
            for(int i =0; i < numPeers; i++){
                if (i != me) {
                    temp.add(-1);
                }
                else {
                    temp.add(vote.get(j));
                }
            }
            Total.add(temp);
        }
    }

    public void generalTransfer(int rank, long startTime){
        myByzantine.firstTransfer(Total.get(rank).get(me), startTime);
    }

    public void generalResult(int rank){
        Total.set(rank, myByzantine.getTransfer());
    }

    public void firstTransfer(long startTime){
        myByzantine.firstTransfer(vote.get(vote.size() - 1), startTime);
    }

    public void secondTransfer(long startTime){
        for (int i = 0; i < vote.size(); i++)
            if (!Discard.contains(vote.get(i))) {
                myByzantine.firstTransfer(vote.get(i), startTime);
                TFirst.set(me, vote.get(i));
                break;
            }
    }

    public void transferResult(int round){
        if(round == 1)this.T = myByzantine.getTransfer();
        else this.TFirst = myByzantine.getTransfer();
    }

    public void agreeOn(int j, long startTime, int round){
        if (round == 1) myByzantine.Start(startTime ,T.get(j));
        else  myByzantine.Start(startTime ,TFirst.get(j));
    }

    public void getConsensus(int j, int round){
        if (round == 1) T.set(j, myByzantine.values.get(me));
        else TFirst.set(j, myByzantine.values.get(me));
    }

    public void getDiscard(){
        Map<Integer, Integer> temp = new HashMap<>();
        for (int i = 0; i < T.size(); i++){
            int key = T.get(i);
            if (temp.containsKey(key)){
                temp.put(key, temp.get(key) + 1);
                if(temp.get(key) == ((numPeers - myByzantine.f) /2)+1){
                    Discard.add(key);
                }
            }else{
                temp.put(key, 1);
            }
        }
    }

    public void getTally(){
        Map<Integer, Integer> temp = new HashMap<>();
        int max = 0, tally = 500;
        for (int i = 0; i < TFirst.size(); i++){
            int key = TFirst.get(i);
            if (temp.containsKey(key)){
                temp.put(key, temp.get(key) + 1);
            }else{
                temp.put(key, 1);
            }
            if(temp.get(key) == max){
                if(key < tally){
                    tally = key;
                }
            }else if(temp.get(key) > max){
                max = temp.get(key);
                tally = key;
            }
        }
        this.leader = tally;
    }
}
