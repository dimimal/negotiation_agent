package components;

import genius.core.bidding.BidDetails;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OpponentModel;

import java.util.List;
import java.util.Map;

/*
* This module is based on the example from exampleWiki module
* */

public class Group4OMS extends OMStrategy {
    OpponentModel opModel;
    private NegotiationSession negotiationSession;

    @Override
    public void init(NegotiationSession negotiationSession, OpponentModel model, Map<String, Double> parameters) {
        super.init(negotiationSession, model, parameters);
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
        // This method is never called. We do not use it in the framework
        return bidsInRange.get(0);
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
