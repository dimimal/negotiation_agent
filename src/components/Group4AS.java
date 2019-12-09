package components;

import genius.core.Bid;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.*;
import genius.core.uncertainty.UserModel;
import genius.core.utility.AdditiveUtilitySpace;

import java.util.*;

public class Group4AS extends AcceptanceStrategy {

    // private double discount = 1.0;
    private AdditiveUtilitySpace utilitySpace;
//    public double discountThreshold = 0.845;
    private double motValue = 0.6;
    double reluctance = 1.1;
    int nBidsToConsiderFromOp = 100;
    int round = 0;
    public double agreeMentValue;
    private UserModel userModelSpace;
    private int roundInterval = 5;

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
        // super.init(negoSession, strat, om, parameters);
        negotiationSession = negoSession;
        offeringStrategy = strat;
        opponentModel = om;
        utilitySpace = (AdditiveUtilitySpace) opponentModel.getOpponentUtilitySpace();
        agreeMentValue = parameters.get("av");

        userModelSpace = negotiationSession.getUserModel();
//        if (utilitySpace.getDiscountFactor() <= 1.0 && utilitySpace.getDiscountFactor() > 0.0)
//            discount = utilitySpace.getDiscountFactor();

    }

    @Override
    public Actions determineAcceptability() {
        Bid partnerBid = negotiationSession.getOpponentBidHistory().getLastBid();
        double time = negotiationSession.getTime();
//        if (utilitySpace.getDiscountFactor() <= 1.0 && utilitySpace.getDiscountFactor() > 0.0)
//            discount = utilitySpace.getDiscountFactor();
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

    public boolean isAcceptable(double offeredUtilFromOpponent, double myOfferedUtil, double time, Bid oppBid) throws Exception {

//        if (offeredUtilFromOpponent >= myOfferedUtil) {
//            return true;
//        }
//
//        return false;

        double bidValue = 0;
        round += 1;

        // Every 10 rounds update the AV and bids to consider for the opponent
        if (round % roundInterval == 0) {
            agreeMentValue = this.computeAVThreshold(nBidsToConsiderFromOp);
            reluctance *= .995;
            System.out.println(reluctance);
            agreeMentValue *= reluctance;
            if (agreeMentValue >= 1)
                agreeMentValue = 0.99;
            System.out.println("Agreement  Value = " + agreeMentValue);
            if (nBidsToConsiderFromOp > roundInterval) {
                nBidsToConsiderFromOp -= 5;
            }
            System.out.println(nBidsToConsiderFromOp);

            // Update agreementValue
            ((Group4OS) this.offeringStrategy).agreeMentValue = this.agreeMentValue;
        }

        if (oppBid != null)
            bidValue = utilitySpace.getUtility(oppBid);

        // System.out.println(bidValue);
        if (bidValue >= agreeMentValue)
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

//    public boolean bidAlreadyMade(Bid a) {
//        boolean result = false;
//        for (int i = 0; i < negotiationSession.getOwnBidHistory().size(); i++) {
//            if (a.equals(negotiationSession.getOwnBidHistory().getHistory().get(i).getBid())) {
//                result = true;
//            }
//        }
//        return result;
//    }

    /*
     Take into account both own and op utility space
     */
    private double computeAVThreshold(int numOfBids) {
//        List<Bid> bidsHistory = new ArrayList<>();
        List<components.BidStruct> allBestBids = new ArrayList<>();

//        bidsHistory = userModelSpace.getBidRanking().getBidOrder();
//        Collections.reverse(bidsHistory);
        List<BidDetails> tempOpBids = negotiationSession.getOpponentBidHistory().getNBestBids(numOfBids);
        List<components.BidStruct> opponentBids = new ArrayList<>();

        for (int i=0; i<numOfBids && i<tempOpBids.size(); ++i) {
            opponentBids.add(new components.BidStruct(tempOpBids.get(i).getBid(), opponentModel.getBidEvaluation(tempOpBids.get(i).getBid())));
        }
// TODO Probably useless!!!
//        for (int i = 0; i < numOfBids && i < bidsHistory.size(); i++) {
//            allBestBids.add(new components.BidStruct(bidsHistory.get(i), utilitySpace.getUtility(bidsHistory.get(i))));
//        }
        for (int i = 0; i < allBestBids.size(); i++) {
                if (opponentBids.indexOf(allBestBids.get(i)) > numOfBids || opponentBids.indexOf(allBestBids.get(i)) == -1) {
                    allBestBids.remove(i);
                    i--;
                    break;
                }
        }

        System.out.println("Top Bids : " + allBestBids.size());
        double maximumValue = 0;
        for (components.BidStruct b : allBestBids) {
            double bidValue = b.value;
            if (bidValue > maximumValue)
                maximumValue = bidValue;
        }
        // Select the greater of max and MOT
        return Math.max(maximumValue, motValue);
    }

//    @Override
//    public Set<BOAparameter> getParameterSpec() {
//        Set<BOAparameter> set = new HashSet<BOAparameter>();
//        set.add(new BOAparameter("av", this.agreeMentValue, "Updated Agreement Value"));
//        return set;
//    }


    @Override
    public String getName() {
        return "Group4AS";
    }
}