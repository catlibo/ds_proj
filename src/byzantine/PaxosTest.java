package byzantine;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

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
            pxa[i] = new ByzantineKing(i, peers, ports, 0, 1, 0);
        }
        return pxa;
    }

    private ByzantineKing[] initByzantineKings(int npaxos, ArrayList<Integer> failNodes){
        String host = "127.0.0.1";
        String[] peers = new String[npaxos];
        int[] ports = new int[npaxos];
        ByzantineKing[] pxa = new ByzantineKing[npaxos];
        for(int i = 0 ; i < npaxos; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }

        for(int i = 0; i < npaxos; i++){
            int tag = (failNodes.contains(i)) ? 1 : 0;
            pxa[i] = new ByzantineKing(i, peers, ports, 0, failNodes.size(), tag);
        }
        return pxa;
    }

    private ByzantineKing[] initByzantineKings(int npaxos, int f){
        String host = "127.0.0.1";
        String[] peers = new String[npaxos];
        int[] ports = new int[npaxos];
        ByzantineKing[] pxa = new ByzantineKing[npaxos];
        for(int i = 0 ; i < npaxos; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }

        for(int i = 0; i < npaxos; i++){
            pxa[i] = new ByzantineKing(i, peers, ports, 0, f, 0);
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
            while(generals[i].Status().state != State.Decided){
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

    @Test
    public void TestBetaNode(){

        final int npaxos = 5;
        ByzantineKing[] generals = initByzantineKings(npaxos);
        BetaNode[] nodes = new BetaNode[npaxos];
        ArrayList<Integer> ballot = new ArrayList<>(Arrays.asList(new Integer[] { 1, 2, 3 }));
        ArrayList<Integer> ballot2 = new ArrayList<>(Arrays.asList(new Integer[] { 3, 2, 1 }));
        for(int i = 0; i < npaxos; i++){
            nodes[i] = new BetaNode(i == 0 ? ballot: ballot2, generals[i], npaxos, i);
        }

        long startTime = System.currentTimeMillis();
        for(int i = 0; i < npaxos; i++){
            nodes[i].firstTransfer(startTime);
        }

        for(int i = 0; i < npaxos; i++){
            nodes[i].transferResult(1);
        }

        for(int i = 0; i < npaxos; i++){
            startTime = System.currentTimeMillis();
            for(int j = 0; j < npaxos; j++){
                nodes[j].agreeOn(i, startTime, 1);
            }

            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            for(int j = 0; j < npaxos; j++){
                nodes[j].getConsensus(i, 1);
            }
        }

        for(int j = 0; j < npaxos; j++){
            nodes[j].getDiscard();
        }

        startTime = System.currentTimeMillis();
        for(int i = 0; i < npaxos; i++){
            nodes[i].secondTransfer(startTime);
        }

        for(int i = 0; i < npaxos; i++){
            nodes[i].transferResult(2);
        }

        for(int i = 0; i < npaxos; i++){
            startTime = System.currentTimeMillis();
            for(int j = 0; j < npaxos; j++){
                nodes[j].agreeOn(i, startTime, 2);
            }

            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            for(int j = 0; j < npaxos; j++){
                nodes[j].getConsensus(i, 2);
            }
        }

        for(int j = 0; j < npaxos; j++){
            nodes[j].getTally();
            assertEquals(3, nodes[j].leader);
        }

        Object a = generals[0].Status().v;
    }


    @Test
    public void TestBetaNodeTotal(){

        final int npaxos = 5;
        ByzantineKing[] generals = initByzantineKings(npaxos);
        BetaNode[] nodes = new BetaNode[npaxos];
        ArrayList<Integer> ballot = new ArrayList<>(Arrays.asList(new Integer[] { 1, 2, 3 }));
        ArrayList<Integer> ballot2 = new ArrayList<>(Arrays.asList(new Integer[] { 3, 2, 1 }));
        for(int i = 0; i < npaxos; i++){
            nodes[i] = new BetaNode(i == 0 ? ballot: ballot2, generals[i], npaxos, i);
        }

        for(int j = 0; j < ballot.size(); j++) {
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < npaxos; i++) {
                nodes[i].generalTransfer(j, startTime);
            }

            for (int i = 0; i < npaxos; i++) {
                nodes[i].generalResult(j);
            }
        }

        Object a = generals[0].Status().v;
    }

    @Test
    public void TestAlpha() {
        int[] tops = {1,0,2,2,1,2,2};
        AlphaByzantine.alpha(tops, 1);
    }

    @Test
    public void KYScoreTest() {
        final int npaxos = 5;
        ByzantineKing[] generals = initByzantineKings(npaxos);
        BetaNode[] nodes = new BetaNode[npaxos];
        ArrayList<Integer> ballot = new ArrayList<>(Arrays.asList(new Integer[] { 0, 1, 2 }));
        ArrayList<Integer> ballot2 = new ArrayList<>(Arrays.asList(new Integer[] { 2, 1, 0 }));
        for(int i = 0; i < npaxos; i++){
            nodes[i] = new BetaNode(i > 2 ? ballot: ballot2, generals[i], npaxos, i);
        }

        for(int j = 0; j < ballot.size(); j++) {
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < npaxos; i++) {
                nodes[i].generalTransfer(j, startTime);
            }

            for (int i = 0; i < npaxos; i++) {
                nodes[i].generalResult(j);
            }
        }

        for (int i = 0; i < npaxos; i++) {
            nodes[i].rankToBallot();
        }

        ArrayList<Integer> scores = new ArrayList<>();

        int[] ranks = {0,1,2};
        ArrayList<ArrayList<Integer>> allSet = PermutationUtil.permute(ranks);

        for (int i = 0; i < allSet.size(); i++){
            scores.add(KYScore.getKYScore(allSet.get(i), nodes[0].TotalBallot));
        }

        ArrayList<Double> result = KYScore.getDefinedScore(allSet, nodes[0].TotalBallot);

        return;
    }

    @Test
    public void resultAlphaScoreTest() {
        final int npaxos = 16;
        int key = 7;
        int fail = 5;
        int[] ranks = new int[key];
        for (int i = 0; i< key; i ++) ranks[i] = i;
        ArrayList<ArrayList<Integer>> allSet = PermutationUtil.permute(ranks);
        ArrayList<Integer> offsets = new ArrayList<>();
        ByzantineKing[] generals = initByzantineKings(npaxos, fail);

        for(int testRun = 0; testRun <10; testRun ++){
            ArrayList<ArrayList<Integer>> goodBallot = new ArrayList<>(new ArrayList<>());

            ArrayList<Integer> failNodes = new ArrayList<Integer>();
            while (failNodes.size() < fail){
                int a = PermutationUtil.generateRandom(npaxos);
                if(!failNodes.contains(a)) failNodes.add(a);
            }

            BetaNode[] nodes = new BetaNode[npaxos];
            for(int i = 0; i < npaxos; i++){
                ArrayList<Integer> newBallot = allSet.get(PermutationUtil.generateRandom(allSet.size()));
                if (!failNodes.contains(i)){
                    goodBallot.add(newBallot);
                    generals[i].tag = 0 ;
                } else {
                    generals[i].tag = 1 ;
                }
                nodes[i] = new BetaNode(newBallot, generals[i], npaxos, i);
            }

            long startTime = System.currentTimeMillis();
            for(int i = 0; i < npaxos; i++){
                nodes[i].secondTransfer(startTime);
            }

            for(int i = 0; i < npaxos; i++){
                nodes[i].transferResult(2);
            }

            for(int i = 0; i < npaxos; i++){
                startTime = System.currentTimeMillis();
                for(int j = 0; j < npaxos; j++){
                    nodes[j].agreeOn(i, startTime, 2);
                }

                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for(int j = 0; j < npaxos; j++){
                    nodes[j].getConsensus(i, 2);
                }
            }

            int leader = -1;
            for(int j = 0; j < npaxos; j++){
                nodes[j].getTally();
                if (!failNodes.contains(j)) leader = nodes[j].leader;
            }


            ArrayList<Double> result = KYScore.getExpectedAverageRank(goodBallot);
            double rank  = result.get(leader);
            int offset = 0;
            for(int i = 0; i < result.size(); i++){
                if (result.get(i) < rank) offset++;
            }
            offsets.add(offset);
        }

        int total = 0;
        for(int i = 0; i < offsets.size(); i++){
            total = total + offsets.get(i);
        }

        System.out.print(total);
        return;
    }

    @Test
    public void resultBetaScoreTest() {
        final int npaxos = 16;
        int key = 3;
        int fail = 5;
        int[] ranks = new int[key];
        for (int i = 0; i< key; i ++) ranks[i] = i;
        ArrayList<ArrayList<Integer>> allSet = PermutationUtil.permute(ranks);
        ArrayList<Integer> offsets = new ArrayList<>();
        ByzantineKing[] generals = initByzantineKings(npaxos, fail);

        for(int testRun = 0; testRun <10; testRun ++){
            ArrayList<ArrayList<Integer>> goodBallot = new ArrayList<>(new ArrayList<>());

            ArrayList<Integer> failNodes = new ArrayList<Integer>();
            while (failNodes.size() < fail){
                int a = PermutationUtil.generateRandom(npaxos);
                if(!failNodes.contains(a)) failNodes.add(a);
            }

            BetaNode[] nodes = new BetaNode[npaxos];
            for(int i = 0; i < npaxos; i++){
                ArrayList<Integer> newBallot = allSet.get(PermutationUtil.generateRandom(allSet.size()));
                if (!failNodes.contains(i)){
                    goodBallot.add(newBallot);
                    generals[i].tag = 0 ;
                } else {
                    generals[i].tag = 1 ;
                }
                nodes[i] = new BetaNode(newBallot, generals[i], npaxos, i);
            }

            long startTime = System.currentTimeMillis();
            for(int i = 0; i < npaxos; i++){
                nodes[i].firstTransfer(startTime);
            }

            for(int i = 0; i < npaxos; i++){
                nodes[i].transferResult(1);
            }

            for(int i = 0; i < npaxos; i++){
                startTime = System.currentTimeMillis();
                for(int j = 0; j < npaxos; j++){
                    nodes[j].agreeOn(i, startTime, 1);
                }

                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for(int j = 0; j < npaxos; j++){
                    nodes[j].getConsensus(i, 1);
                }
            }

            for(int j = 0; j < npaxos; j++){
                nodes[j].getDiscard();
            }

            startTime = System.currentTimeMillis();
            for(int i = 0; i < npaxos; i++){
                nodes[i].secondTransfer(startTime);
            }

            for(int i = 0; i < npaxos; i++){
                nodes[i].transferResult(2);
            }

            for(int i = 0; i < npaxos; i++){
                startTime = System.currentTimeMillis();
                for(int j = 0; j < npaxos; j++){
                    nodes[j].agreeOn(i, startTime, 2);
                }

                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for(int j = 0; j < npaxos; j++){
                    nodes[j].getConsensus(i, 2);
                }
            }

            int leader = -1;
            for(int j = 0; j < npaxos; j++){
                nodes[j].getTally();
                if (!failNodes.contains(j)) leader = nodes[j].leader;
            }


            ArrayList<Double> result = KYScore.getExpectedAverageRank(goodBallot);
            double rank  = result.get(leader);
            int offset = 0;
            for(int i = 0; i < result.size(); i++){
                if (result.get(i) < rank) offset++;
            }
            offsets.add(offset);
        }

        int total = 0;
        for(int i = 0; i < offsets.size(); i++){
            total = total + offsets.get(i);
        }

        System.out.print(total);
        return;
    }

    @Test
    public void permutationUtilTest() {
        int[] ranks = {1,2,3,4,5};
        ArrayList<ArrayList<Integer>> allSet = PermutationUtil.permute(ranks);
        return;
    }
}
