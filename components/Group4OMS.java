package group4.components;

import genius.core.bidding.BidDetails;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OpponentModel;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class Group4OMS extends OMStrategy {
    OpponentModel opModel;

    @Override
    public void init(NegotiationSession negotiationSession, OpponentModel model, Map<String, Double> parameters) {
        super.init(negotiationSession, model, parameters);
        opModel = model;
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
        double selectUtil = 0;
        boolean isOpModelWork = true;

        if (bidsInRange.size() == 1) {
            return bidsInRange.get(0);
        }

        BidDetails bestBid = bidsInRange.get(0);

        for (BidDetails bid : bidsInRange) {
            evalBid = opModel.getBidEvaluation(bid.getBid());
            if (evalBid > 0.0001) {
                isOpModelWork = false;
            }
            if (evalBid > selectUtil ) {
                bestBid = bid;
                selectUtil = evalBid;
            }
        }

        if (isOpModelWork) {
            Random random = new Random();
            return bidsInRange.get(random.nextInt(bidsInRange.size()));
        }
        return bestBid;
    }

    /**
     * @return if given the negotiation state the opponent model may be updated
     */
    @Override
    public boolean canUpdateOM() {

        if (negotiationSession.getTime() < 1.){
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
