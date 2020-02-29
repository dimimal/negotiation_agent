package components;

import genius.core.Bid;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.*;
import genius.core.uncertainty.UserModel;
import genius.core.utility.AdditiveUtilitySpace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Group4AS extends AcceptanceStrategy {

    private AdditiveUtilitySpace utilitySpace;
    private double motValue = 0.65;
    double reluctance = 1.1;
    int nBidsToConsiderFromOp = 120;
    int round = 0;
    public double agreeMentValue;
    private UserModel userModelSpace;
    private int roundInterval = 10;
    private double lowestOwnAVThreshold = 0.67;


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
        negotiationSession = negoSession;
        offeringStrategy = strat;
        opponentModel = om;
        utilitySpace = (AdditiveUtilitySpace) opponentModel.getOpponentUtilitySpace();
        agreeMentValue = parameters.get("av");


        userModelSpace = negotiationSession.getUserModel();
    }

    @Override
    public Actions determineAcceptability() {
        Bid opponentBid = negotiationSession.getOpponentBidHistory().getLastBid();
        double time = negotiationSession.getTime();

        try {
            if (isAcceptable(time, opponentBid))
                return Actions.Accept;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Actions.Reject;
    }

    public boolean isAcceptable(double time, Bid oppBid) throws Exception {

        double bidValue = 0;
        round += 1;

        // Every Interval rounds update the AV and bids to consider for the opponent
        if (round % roundInterval == 0) {
            agreeMentValue = this.computeAVThreshold();
            reluctance *= .995;
            System.out.println("Reluctance: " + reluctance);
            agreeMentValue *= reluctance;
            if (agreeMentValue >= 1)
                agreeMentValue = 0.99;
            // Maybe necessary
            if(agreeMentValue < lowestOwnAVThreshold) {
                agreeMentValue = lowestOwnAVThreshold;
            }

            if (nBidsToConsiderFromOp > 15) {
                nBidsToConsiderFromOp = nBidsToConsiderFromOp - 5;
            }
            // Update agreementValue
            ((Group4OS) this.offeringStrategy).agreeMentValue = this.agreeMentValue;
        }
        ((Group4OS) this.offeringStrategy).round = this.round;

        if (oppBid != null)
            bidValue = utilitySpace.getUtility(oppBid);


        if (bidValue >= agreeMentValue) {
            System.out.println("bidValue: " + bidValue + " || AgreemValue: " + agreeMentValue);
            return true;
        }
        else{
            return false;
        }
    }



    /*
     Take into account both own and op utility space
     */
    private double computeAVThreshold() {
        double maximumValue = 0;
        List<components.BidStruct> opponentBids = new ArrayList<>();
        List<components.BidStruct> orderedUserBids = new ArrayList<>();

        List<Bid> bidsHistory = userModelSpace.getBidRanking().getBidOrder();
        List<components.BidStruct> allBestBids = new ArrayList<>();
        List<BidDetails> tempOpBids = negotiationSession.getOpponentBidHistory().getNBestBids(nBidsToConsiderFromOp);

        // Create ordered user bids
        for (Bid bid: bidsHistory) {
            orderedUserBids.add(new components.BidStruct(bid, utilitySpace.getUtility(bid)));
        }
        Collections.sort(orderedUserBids);

        for (int i=0; i<nBidsToConsiderFromOp && i<tempOpBids.size(); ++i) {
            opponentBids.add(new components.BidStruct(tempOpBids.get(i).getBid(),
                    opponentModel.getBidEvaluation(tempOpBids.get(i).getBid())));
        }
        Collections.sort(opponentBids);

        for (int i = 0; i < nBidsToConsiderFromOp && i < orderedUserBids.size(); ++i) {
            allBestBids.add(orderedUserBids.get(i));
        }

        for (int i = 0; i < allBestBids.size(); ++i) {
                // If opponents bid not in my preference, remove it.
                if (opponentBids.indexOf(allBestBids.get(i)) > nBidsToConsiderFromOp || opponentBids.indexOf(allBestBids.get(i)) == -1) {
                    allBestBids.remove(i);
                    break;
                }
        }

        for (components.BidStruct bid : allBestBids) {
            if (bid.value > maximumValue)
                maximumValue = bid.value;
        }
        // Select the greatest of max and MOT
        return Math.max(maximumValue, motValue);
    }

    @Override
    public String getName() {
        return "Group4AS";
    }
}