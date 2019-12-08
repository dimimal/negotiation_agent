package components;

import genius.core.bidding.BidDetails;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;

public class TdOfferOS extends OfferingStrategy {

    public TdOfferOS(NegotiationSession negotiationSession, OpponentModel model, OMStrategy oms) {
        this.negotiationSession = negotiationSession;
        double discountFactor = negotiationSession.getDiscountFactor();
//        outcomespace = new SortedOutcomeSpace(negotiationSession.getUtilitySpace());
//        negotiationSession.setOutcomeSpace(outcomespace);
//        List<BidDetails> alloutcomes = outcomespace.getAllOutcomes();
//        outcomeSize = alloutcomes.size();
//        startSize = 0.01 * outcomeSize;
//        finSize = 0.1 * outcomeSize;
//        this.opponentModel = model;
//        this.omStrategy = oms;

    }
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
        return "TD Ofering Strategy";
    }
}
