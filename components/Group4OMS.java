package group4.components;

import genius.core.bidding.BidDetails;
import genius.core.boaframework.OMStrategy;

import java.util.List;

public class Group4OMS extends OMStrategy {
    /**
     * Returns a bid selected using the opponent model from the given set of
     * similarly preferred bids.
     *
     * @param bidsInRange set of similarly preferred bids
     * @return bid
     */
    @Override
    public BidDetails getBid(List<BidDetails> bidsInRange) {
        return null;
    }

    /**
     * @return if given the negotiation state the opponent model may be updated
     */
    @Override
    public boolean canUpdateOM() {
        return false;
    }

    /**
     * @return a short name for this component.
     */
    @Override
    public String getName() {
        return null;
    }
}
