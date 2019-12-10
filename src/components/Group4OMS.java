package components;

import agents.anac.y2019.harddealer.SortBids;
import agents.anac.y2019.harddealer.SortBidsOpponent;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OpponentModel;

import java.util.ArrayList;
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
        boolean isOpModelWork = true;

        // Only one bid, return it
//        if (bidsInRange.size() == 1) {
//            return bidsInRange.get(0);
//        }
//
//        BidDetails bestBid = bidsInRange.get(0);
//
//        for (BidDetails bid : bidsInRange) {
//            evalBid = opModel.getBidEvaluation(bid.getBid());
//            if (evalBid > 0.0001) {
//                isOpModelWork = true;
//            }
//            if (evalBid > maxUtil ) {
//                bestBid = bid;
//                maxUtil = evalBid;
//            }
//        }

        // TODO HardDealer's check
        // Will contain x best bids
        List<BidDetails> bestBids = new ArrayList<BidDetails>();
        for (BidDetails bid : bidsInRange) {

            double evaluation = opModel.getBidEvaluation(bid.getBid());

            // model works
            if (evaluation > 0.0001)
            {
                isOpModelWork = false;
            }

            // bestBids's allowed size will decrease during negotiation. For more info, see OMstrategy part in report
            if (bestBids.size() < (int)(5 * (1 - negotiationSession.getTime())) + 1)
            {
                bestBids.add(bid);
            }
            else
            {
                // Find the five best bids according to the opponent model
                // TODO Remove the library later!!!
                bestBids.sort(new SortBidsOpponent(opModel));

                if (model.getBidEvaluation(bestBids.get(0).getBid()) < evaluation)
                    bestBids.set(0, bid);
            }

        }

        if (isOpModelWork) {
            Random random = new Random();
            int randomInt = random.nextInt(bidsInRange.size());
            return bidsInRange.get(randomInt);
        }
        // TODO Remove this Sort also!!!
        bestBids.sort(new SortBids());
        // Find the best bid from the bids the opponent model selected.
        BidDetails myBid = bestBids.get(bestBids.size() - 1);

        if (negotiationSession.getOpponentBidHistory().isEmpty())
        {
            return myBid;
        }
        else
        {
            // If the opponent has offered something before with a better utility than our current selected bid, return their best bid.
            BidDetails theirBestBid = negotiationSession.getOpponentBidHistory().getBestBidDetails();

            if (myBid.getMyUndiscountedUtil() >= theirBestBid.getMyUndiscountedUtil())
            {
                return myBid;
            }
            else
            {
                return theirBestBid;
            }
        }


    }
        // return bestBids;


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
