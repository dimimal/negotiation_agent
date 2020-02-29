
package components;

import genius.core.Bid;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.ValueDiscrete;
import genius.core.utility.AdditiveUtilitySpace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Group4OS extends OfferingStrategy {
    /*
    Implements the Johnny black bidding strategy
     */
    double opCare = 0.4;

    double lowUtilValue = 0.6;
    int lastBid = 0; // Keep track of the last bid
    public static double agreeMentValue;
    private AdditiveUtilitySpace ownUtilitySpace;
    private List<components.BidStruct> feasibleBids;
    private Utils utilities;
    int round = 0;
    //
    int[] orderedIssues;
    int[][] orderedIssuesValues;
    private boolean isFsbCalculated;

    @Override
    public void init(NegotiationSession negoSession, OpponentModel model, OMStrategy oms,
                     Map<String, Double> parameters) throws Exception {
        super.init(negoSession, model, oms, parameters);

        this.ownUtilitySpace = (AdditiveUtilitySpace) negoSession.getUtilitySpace();
        this.opponentModel = model;
        this.omStrategy = oms;
        this.utilities = new Utils();
        this.orderedIssues = utilities.getOrderedIssues(ownUtilitySpace);
        this.orderedIssuesValues = utilities.getOrderedIssueValues(ownUtilitySpace);
        this.feasibleBids = getFeasibleBidsList();
        Collections.sort(this.feasibleBids);
        this.agreeMentValue = parameters.get("av");
    }

    /**
     * Determines the first bid to be offered by the agent
     *
     * @return the opening bid of the agent.
     */
    @Override
    public BidDetails determineOpeningBid() {
        return determineNextBid();
    }

    /**
     * Determines the next bid the agent will offer to the opponent
     *
     * @return bid to offer to the opponent.
     */
    @Override
    public BidDetails determineNextBid() {
        opCare *= 1.004;
        BidDetails offerBid = getNewBid();

        return  offerBid;
    }

    /*
      Returns the generated bid according to the pseudo code from Johnny black bidding strategy
     */
    public BidDetails getNewBid() {
        for (int i = lastBid + 1; i < feasibleBids.size(); i++) {
            components.BidStruct fsBid = feasibleBids.get(i);
//            System.out.println(fsBid.value > agreeMentValue && opponentModel.getBidEvaluation(fsBid.bid) > opCare);
            if (fsBid.value > agreeMentValue && opponentModel.getBidEvaluation(fsBid.bid) > opCare) {
                lastBid = i;
                return new BidDetails(fsBid.bid, fsBid.value);
            }
            if (fsBid.value < agreeMentValue)
                break;
        }

        lastBid = 0;
        return new BidDetails(feasibleBids.get(0).bid, feasibleBids.get(0).value);
    }

    /**
     * @return a short name for this component.
     */
    @Override
    public String getName() {
        return "JB Bidding";
    }

    /* Create Bid candidates according to our own preference!
     */
    public List<components.BidStruct> getFeasibleBidsList() throws Exception {
        List<components.BidStruct> validBids = new ArrayList<components.BidStruct>();
        int issueID;
        int item;
        int issueNumber;
        Bid maxBid =  ownUtilitySpace.getMaxUtilityBid();
        Bid tempBid = new Bid(maxBid);

        List<Issue> issues = ownUtilitySpace.getDomain().getIssues();
        for (Issue issue : issues) {
            issueNumber = issue.getNumber();

            if (issueNumber >= orderedIssues.length) break;
            for (int i = 0; i < orderedIssuesValues[orderedIssues[issueNumber]].length; ++i) {
                issueID = orderedIssues[issueNumber];
                item = orderedIssuesValues[issueID][i] - 1;
                try {
                    ValueDiscrete issueValue = ((IssueDiscrete) ownUtilitySpace.getIssue(issueID)).getValue(item);
                    tempBid = tempBid.putValue(issueID + 1, issueValue);
                    if (ownUtilitySpace.getUtility(tempBid) > lowUtilValue) {
                        validBids.add(new components.BidStruct(tempBid, ownUtilitySpace.getUtility(tempBid)));
                    }
                } catch (Exception e) {
                    System.out.println(1);
                    continue;
                }

            }
        }

        return validBids;

    }

}



