package group4.components;
import genius.core.Bid;
import genius.core.boaframework.*;
import genius.core.utility.AdditiveUtilitySpace;

import java.util.Map;

public class Group4AS extends AcceptanceStrategy {

    private double discount = 1.0;
    private AdditiveUtilitySpace utilitySpace;
    public double discountThreshold = 0.845;

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
        this.negotiationSession = negoSession;
        offeringStrategy = strat;
        opponentModel = om;
        utilitySpace = (AdditiveUtilitySpace) opponentModel.getOpponentUtilitySpace();

        if (utilitySpace.getDiscountFactor() <= 1.0 && utilitySpace.getDiscountFactor() > 0.0)
            discount = utilitySpace.getDiscountFactor();

    }

    @Override
    public Actions determineAcceptability() {
        Bid partnerBid = negotiationSession.getOpponentBidHistory().getLastBid();
        double time = negotiationSession.getTime();
         if (time > 0.97) {
            if (bidAlreadyMade(partnerBid)) {
                return Actions.Accept;

            }
        } else if (discount < discountThreshold) {
            if (bidAlreadyMade(partnerBid)) {
                return Actions.Accept;
            }
        }
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

        if (offeredUtilFromOpponent >= myOfferedUtil) {
            return true;
        }

        return false;
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

    @Override
    public String getName() {
        return "Group4AS";
    }
}
