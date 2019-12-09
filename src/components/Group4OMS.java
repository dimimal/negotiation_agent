package components;

import genius.core.bidding.BidDetails;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OpponentModel;

import java.util.List;
import java.util.Map;
import java.util.Random;

/*
* This module is based on the example from exampleWiki module
* */

public class Group4OMS extends OMStrategy {
    OpponentModel opModel;
    private NegotiationSession negotiationSession;

    @Override
    public void init(NegotiationSession negotiationSession, OpponentModel model, Map<String, Double> parameters) {
        // super.init(negotiationSession, model, parameters);
        this.negotiationSession = negotiationSession;
        this.opModel = model;
    }

    /**
     * Returns a bid selected using the opponent model from the given set of
     * similarly preferred bids.
     *
     * @param bidsInRange set of similarly preferred bids
     * @return bid
     */
    @Override
    public BidDetails getBid(List<BidDetails> bidsInRange) {
        double evalBid = 0.;
        double maxUtil = 0;
        boolean isOpModelWork = false;

        // Only one bid, return it
        if (bidsInRange.size() == 1) {
            return bidsInRange.get(0);
        }

        BidDetails bestBid = bidsInRange.get(0);

        for (BidDetails bid : bidsInRange) {
            evalBid = opModel.getBidEvaluation(bid.getBid());
            if (evalBid > 0.0001) {
                isOpModelWork = true;
            }
            if (evalBid > maxUtil ) {
                bestBid = bid;
                maxUtil = evalBid;
            }
        }

        if (!isOpModelWork) {
            Random random = new Random();
            int randomInt = random.nextInt(bidsInRange.size());
            return bidsInRange.get(randomInt);
        }
        return bestBid;
    }

    /**
     * @return if given the negotiation state the opponent model may be updated
     */
    @Override
    public boolean canUpdateOM() {

        if (this.negotiationSession.getTime() < 1.1){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * @return a short name for this component.
     */
    @Override
    public String getName() {
        return "OMS Strategy";
    }
}
