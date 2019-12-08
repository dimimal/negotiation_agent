package components;

import genius.core.Bid;
import genius.core.actions.Action;
import genius.core.boaframework.*;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import sun.nio.cs.ext.MacGreek;

import javax.naming.ldap.PagedResultsControl;
import java.util.Map;

public class Group4AS extends AcceptanceStrategy {

    private double discount = 1.0;
    private AdditiveUtilitySpace utilitySpace;
    public double discountThreshold = 0.845;
    //public double discountThreshold = 0.75;
    double reluctance = 1.1;
    int nBidsToConsiderFromOp = 100;
    int round = 0;
    public double agreementValue;

    public Group4AS() {
    }

    public Group4AS(NegotiationSession negoSession, OfferingStrategy strat) throws Exception {
        init(negoSession, strat, null, null);
    }

    public Group4AS(NegotiationSession negoSession, OfferingStrategy strat, OpponentModel om) throws Exception {
        init(negoSession, strat, om, null);
    }

    @Override
    public void init(NegotiationSession negoSession, OfferingStrategy strat, OpponentModel om, Map<String, Double> parameters) throws Exception {
        super.init(negoSession, strat, om, parameters);
        this.negotiationSession = negoSession;
        offeringStrategy = strat;
        opponentModel = om;
        utilitySpace = (AdditiveUtilitySpace) opponentModel.getOpponentUtilitySpace();
        this.agreementValue = offeringStrategy.getAgreementValue();

        if (utilitySpace.getDiscountFactor() <= 1.0 && utilitySpace.getDiscountFactor() > 0.0)
            discount = utilitySpace.getDiscountFactor();

    }

    @Override
    public Actions determineAcceptability() {
        Bid partnerBid = negotiationSession.getOpponentBidHistory().getLastBid();
        double time = negotiationSession.getTime();
        if (utilitySpace.getDiscountFactor() <= 1.0 && utilitySpace.getDiscountFactor() > 0.0)
            discount = utilitySpace.getDiscountFactor();
//        if (time > 0.97) {
//            if (bidAlreadyMade(partnerBid)) {
//                return Actions.Accept;
//
//            }
//        } else if (discount >= discountThreshold) {
//            if (bidAlreadyMade(partnerBid)) {
//                return Actions.Accept;
//            }
//        }
        double myOfferedUtil = negotiationSession.getDiscountedUtility(offeringStrategy.getNextBid().getBid(), time);
        double offeredUtilFromOpponent = negotiationSession.getDiscountedUtility(negotiationSession.getOpponentBidHistory().getLastBid(), time);

        try {
            if (isAcceptable(offeredUtilFromOpponent, myOfferedUtil, time, partnerBid))
                return Actions.Accept;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Actions.Reject;
    }

    private boolean isAcceptable(double offeredUtilFromOpponent, double myOfferedUtil, double time, Bid oppBid) throws Exception {

//        if (offeredUtilFromOpponent >= myOfferedUtil) {
//            return true;
//        }
//
//        return false;
        round += 1;

        // Every 10 rounds update the AV and bids to consider for the opponent
        if (round % 10 == 0) {
            //agreeMentValue = Functions.calcStopVal(parties, nBidsToConsiderFromOp, (AdditiveUtilitySpace) utilitySpace);
            agreeMentValue = this.calcStopVal(nBidsToConsiderFromOp, this.opponentModel.getOpponentUtilitySpace());
            offeringStrategy().setAgreementValue(agreeMentValue);
            // System.out.println(getPartyId());
            reluctance *= .995;
            System.out.println(reluctance);
            agreeMentValue *= reluctance;
            if (agreeMentValue >= 1)
                agreeMentValue = 0.99;
            System.out.println("Agreement  Value = " + agreeMentValue);
            if (nBidsToConsiderFromOp > 10) {
                nBidsToConsiderFromOp -= 5;
            }
            System.out.println(nBidsToConsiderFromOp);
        }

        double d = 0;
        if (oppBid != null)
            d = utilitySpace.getUtility(oppBid);

        // System.out.println(d);
        if (d >= agreeMentValue)
            // return new Accept(getPartyId(), lastBid);
            return true;
        else{
            return false;
        }

        // Agent TD
//        if (offeredUtilFromOpponent > myOfferedUtil) {
//            if (time < 0.7) {
//                if (offeredUtilFromOpponent > 0.85)
//                    return true;
//            } else if (time < 0.98) {
//                if (offeredUtilFromOpponent > 0.75)
//                    return true;
//            } else {
//                return true;
//            }
//        }
//        return false;
    }

    public boolean bidAlreadyMade(Bid a) {
        boolean result = false;
        for (int i = 0; i < negotiationSession.getOwnBidHistory().size(); i++) {
            if (a.equals(negotiationSession.getOwnBidHistory().getHistory().get(i).getBid())) {
                result = true;
            }
        }
        return result;
    }

    /*
     Take into account both own and op utility space
     */
    private double calcStopVal(int numOfBids) {

        Vector<BidHolder> topNbids = new Vector<BidHolder>();
        for (int i = 0; i < n && i < parties.get(0).orderedBids.size(); i++) {
            topNbids.add(parties.get(0).orderedBids.get(i));
        }
        for (int i = 0; i < topNbids.size(); i++) {
            for (Party p : parties) {
                if (p.orderedBids.indexOf(topNbids.get(i)) > numOfBids
                        || p.orderedBids.indexOf(topNbids.get(i)) == -1) {
                    topNbids.remove(i);
                    i--;
                    break;
                }
            }
        }
        System.out.println("Common Bids : " + topNbids.size());
        double max = 0;
        for (BidHolder b : topNbids) {
            double v = opponentModel.getBidEvaluation(); // Functions.getBidValue(us, b.b);
            if (v > max)
                max = v;
        }
        // Select the greates of max and MOT
        return Math.max(max, 0.6);
    }



    @Override
    public String getName() {
        return "Group4AS";
    }
}