package components;

import genius.core.Bid;
import genius.core.actions.Offer;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group4OS extends OfferingStrategy {
    /*
    Implements the Johnny black bidding strategy
     */
    double opCare = 0.4;

    int agentToFavor = 0;
    double lowValue = 0.6;
    int lastBid = 0; // Keep track of the last bid
    Bid bidValue;
    private double agreeMentValue = 1;
    private AdditiveUtilitySpace ownUtilitySpace;
    private List<BidStruct> feasibleBids;
    

    public void init(NegotiationSession negoSession, OpponentModel model, OMStrategy oms,
                     Map<String, Double> parameters) throws Exception {
        super.init(negoSession, model, oms, parameters);

        this.ownUtilitySpace = (AdditiveUtilitySpace) negoSession.getUtilitySpace();
        this.opponentModel = model;
        this.omStrategy = oms;
        this.feasibleBids = getFeasibleBids();

    }
    /**
     * Determines the first bid to be offered by the agent
     *
     * @return the opening bid of the agent.
     */
    @Override
    public BidDetails determineOpeningBid() {
        return determineNextBid();
    }

    /**
     * Determines the next bid the agent will offer to the opponent
     *
     * @return bid to offer to the opponent.
     */
    @Override
    public BidDetails determineNextBid() {
        opCare *= 1.004;
        BidDetails b = getNewBid();

        agentToFavor += 1;
        agentToFavor = agentToFavor % 2;

        // Offer??
        return  b;

    }

    /*
      Returns the generated bid acording to the pseudo code from Johnny black bidding strategy
     */
    public BidDetails getNewBid() {
        for (int i = lastBid + 1; i < feasibleBids.size(); i++) {
            BidStruct bh = feasibleBids.get(i);
            if (bh.value > agreeMentValue && opponentModel.getBidEvaluation(bh.bid) > opCare) {
                lastBid = i;
                return new BidDetails(bh.bid, bh.value);
            }
            if (bh.value < agreeMentValue)
                break;
        }

        lastBid = 0;
        return new BidDetails(feasibleBids.get(0).bid, feasibleBids.get(0).value);
    }

    /**
     * @return a short name for this component.
     */
    @Override
    public String getName() {
        return null;
    }

    public List<BidStruct> getFeasibleBids() {
        List<BidStruct> bids = new ArrayList<BidStruct>();
        // TODO Own utility or op utility
        // Bid bid =  opponentModel.getOpponentUtilitySpace().getMaxUtilityBid() //Functions.getCopyOfBestBid((AdditiveUtilitySpace) utilitySpace);
        bids = getTheBestBid();// recurseBids(bid, bids, 0);
        System.out.println("List Size:" + bids.size());
        return bids;
    }

    public List<BidStruct> getTheBestBid() {
        List<BidStruct> feasibleBids = new ArrayList<BidStruct>();
        HashMap<Integer, Map<String, List<Double>>> issueFreqs = opponentModel.getIssueOptionFreqs();

        // Extract the normalized estimated weight
        for (Integer i : issueFreqs.keySet()) {
            System.out.println(issueFreqs.keySet());
            System.out.println("Issue freq: " + issueFreqs.get(i).get("ISSUE_WEIGHT").get(1));

            // return this.getTheBestBid();
        }
    }

    public double getAgreementValue() {
        return this.agreeMentValue;
    }

    public double setAgreementValue(double value) {
        this.agreeMentValue = value;
    }

    public class BidStruct {
        Bid bid;
        double value;
    }
}




//    public Vector<BidHolder> recurseBids(Bid b, Vector<BidHolder> v, int is) {
//        Vector<BidHolder> v1 = new Vector<BidHolder>();
//        if (is == issueOrder.length) {
//            BidHolder bh = new BidHolder();
//            bh.b = b;
//            bh.v = Functions.getBidValue((AdditiveUtilitySpace) utilitySpace,
//                    b);
//            v1.addElement(bh);
//
//            return v1;
//        }
//        for (int i = 0; i < issueValOrder[issueOrder[is]].length; i++) {
//            Bid b1 = new Bid(b);
//            int issueID = issueOrder[is];
//            int item = issueValOrder[issueID][i] - 1;
//            ValueDiscrete val = Functions.getVal((AdditiveUtilitySpace) utilitySpace, issueID, item);
//            b1 = b1.putValue(issueID + 1, val);
//            if (Functions.getBidValue((AdditiveUtilitySpace) utilitySpace,
//                    b1) > this.finalStopVal) {
//                v1.addAll(recurseBids(b1, v1, is + 1));
//            }
//        }
//        return v1;
//    }
