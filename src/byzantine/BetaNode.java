package byzantine;

import java.util.ArrayList;

public class BetaNode {

    int numPeers;
    ByzantineKing myByzantine;
    ArrayList<Integer> vote;
    ArrayList<Integer> T;
    int me;

    BetaNode(ArrayList<Integer> vote, ByzantineKing byzantineKing, int numPeers, int me ){
        this.myByzantine = byzantineKing;
        this.vote = vote;
        this.numPeers = numPeers;
        this.me = me;
        this.T = new ArrayList<>();

        for(int i =0; i < numPeers; i++){
            if (i != me) T.add(-1);
            else T.add(vote.get(0));
        }
    }

    public void firstTransfer(long startTime){
        myByzantine.firstTransfer(vote.get(0), startTime);
    }

    public void firstTransferResult(){
        this.T = myByzantine.getFirstTransfer();
    }
}
