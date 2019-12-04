package group4;

import genius.core.Bid;
import genius.core.boaframework.*;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.ValueDiscrete;
import genius.core.parties.NegotiationInfo;
import genius.core.uncertainty.BidRanking;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.EvaluatorDiscrete;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class Agent4 extends BoaParty {
    private HashMap<String, Double> issueValUtils = new HashMap<>();

    @Override
    public void init(NegotiationInfo info)
    {
        AcceptanceStrategy ac  = new group4.components.Group4AS();
        OfferingStrategy   os  = new group4.components.Group4OS();
        OpponentModel      om  = new group4.components.Group4OM();
        OMStrategy         oms = new group4.components.Group4OMS();

        Map<String, Double> noparams = Collections.emptyMap();

        configure(ac, noparams, os, noparams, om, noparams, oms, noparams);
        super.init(info);

        AbstractUtilitySpace utilitySpace = info.getUtilitySpace();
        AdditiveUtilitySpace additiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;
        System.out.println("---------------------Default Estimated Userspace");

        this.spaceInspect(additiveUtilitySpace);


//        opponentModel.init(negotiationSession, noparams);
//        ExperimentalUserModel e = (ExperimentalUserModel) userModel ;
//        UncertainAdditiveUtilitySpace realUSpace = e.getRealUtilitySpace();
//        System.out.println("---------------------Real Userspace");
//        this.spaceInspect(realUSpace);


        this.updateUserParam(additiveUtilitySpace);
    }

    @Override
    public String getDescription() {
        return "Group 4";
    }

    private void spaceInspect(AdditiveUtilitySpace additiveUtilitySpace) {
        List<Issue> issues = additiveUtilitySpace.getDomain().getIssues();
        for (Issue issue : issues) {
            int issueNumber = issue.getNumber();
            //Map<String, List<Double>> optionFrequency = new HashMap<>();
            System.out.println(">> " + issue.getName() + " weight: " + additiveUtilitySpace.getWeight(issueNumber));

            // Assuming that issues are discrete only
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            EvaluatorDiscrete evaluatorDiscrete = (EvaluatorDiscrete) additiveUtilitySpace.getEvaluator(issueNumber);

            for (ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
                String optionName = valueDiscrete.getValue();
                System.out.println(optionName);
                System.out.println("Evaluation(getValue): " + evaluatorDiscrete.getValue(valueDiscrete));
                try {
                    System.out.println("Evaluation(getEvaluation): " + evaluatorDiscrete.getEvaluation(valueDiscrete));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // initialization of options for each issue
                //optionFrequency.put(optionName, new ArrayList<>(Arrays.asList(0.0, 0.0)));
            }
            // issue weight and normalized weight
            //optionFrequency.put(ISSUE_WEIGHT, new ArrayList<>(Arrays.asList(0.0, 0.0)));
            //issueOptionFrequecies.put(issueNumber, optionFrequency);
        }
    }

    private void updateUserParam(AdditiveUtilitySpace additiveUtilitySpace) {
        BidRanking userBidRanking = userModel.getBidRanking();
        List<Bid> rankedUserBids = userBidRanking.getBidOrder(); // From low to high
        int rankedBidsNum = rankedUserBids.size();
        long totalBidsNum = userModel.getDomain().getNumberOfPossibleBids();
        double elicitCost = user.getElicitationCost();

        try {
            lpMethod(rankedUserBids, additiveUtilitySpace);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double utilityEstimate(Bid bid) {
        double util = 0.0;
        for(Issue issue : bid.getIssues()) {
            util += this.issueValUtils.get(issue.getName() + bid.getValue(issue).toString());
        }
        return util;
    }


    private void lpMethod(List<Bid> bids, AdditiveUtilitySpace additiveUtilitySpace) throws Exception {
        LPWizard lpw = new LPWizard();
        List<Issue> issues = userModel.getDomain().getIssues();
        int oSize = bids.size();
        // Constraints: each slack term is non-negative
        for(int i = 0; i < oSize-1; i++) {
            lpw.plus("Slack" + i, 1.0);
            lpw.addConstraint("s" + i, 0, "<=").plus("Slack" + i, 1.0);
        }
        //varList.stream().forEach(s -> lpw.plus(s, 1.0));
        // Constraints: comparisons
        for(int i=0; i < bids.size()-1; i++) {
            // Phi(o_i) - phi(o_i+1) - z <= 0
            LPWizardConstraint lpwc = lpw.addConstraint("c"+ i, 0, ">=");
            for (Issue issue : issues) {
                if (!bids.get(i).getValue(issue).toString().equals(bids.get(i + 1).getValue(issue).toString())) {
                    lpwc.plus(issue.getName() + bids.get(i).getValue(issue).toString(), 1.0)
                            .plus(issue.getName() + bids.get(i + 1).getValue(issue).toString(), -1.0)
                            .plus("Slack" + i, -1.0);
                }
            }
        }
        // Constraints: util of each issue value is non-negative
        List<String> iVals = ivPairs();
        AtomicInteger k= new AtomicInteger(1);
        iVals.forEach(s -> lpw.addConstraint("v" + k.getAndIncrement(), 0, "<=").plus(s, 1.0));

        // Constraints: util of gloabl max bid is 1
        Bid maxBid = additiveUtilitySpace.getMaxUtilityBid();
        LPWizardConstraint lpwc = lpw.addConstraint("max", 1.0, "=");
        for(Issue issue : issues) {
            lpwc.plus(issue.getName() + maxBid.getValue(issue).toString(), 1.0);
        }
        // Constraints: util of gloabl min bid is 0
        /*Bid minBid = additiveUtilitySpace.getMinUtilityBid();
        lpwc = lpw.addConstraint("min", 0.0, "=");
        for(Issue issue : issues) {
            lpwc.plus(issue.getName() + minBid.getValue(issue).toString(), 1.0);
        }*/

        lpw.setMinProblem(true);
        LPSolution lpSolution = lpw.solve();
        try {
            lpSolution.toString();
        } catch(Exception e) {
            e.printStackTrace();
        }
        for(String s : iVals) {
            this.issueValUtils.put(s, lpSolution.getDouble(s));
        }
    }

    /**
     *
     * @return all issue+value pairs
     */
    private List<String> ivPairs(){
        List<Issue> issues = userModel.getDomain().getIssues();
        List<String> vars = new ArrayList<>();
        for(Issue issue : issues) {
            IssueDiscrete id = (IssueDiscrete) issue;
            for(ValueDiscrete val : id.getValues()) {
                vars.add(issue.getName()+val.toString());
            }
        }
        return vars;
    }
}