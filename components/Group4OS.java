package group4.components;

import genius.core.bidding.BidDetails;
import genius.core.boaframework.OfferingStrategy;

public class Group4OS extends OfferingStrategy {
    /**
     * Determines the first bid to be offered by the agent
     *
     * @return the opening bid of the agent.
     */
    @Override
    public BidDetails determineOpeningBid() {
        return null;
    }

    /**
     * Determines the next bid the agent will offer to the opponent
     *
     * @return bid to offer to the opponent.
     */
    @Override
    public BidDetails determineNextBid() {
        return null;
    }

    /**
     * @return a short name for this component.
     */
    @Override
    public String getName() {
        return null;
    }
}
