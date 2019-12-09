package group4;

import components.Group4AS;
import components.Group4OM;
import components.Group4OMS;
import components.Group4OS;
import genius.core.Bid;
import genius.core.boaframework.*;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.ValueDiscrete;
import genius.core.parties.NegotiationInfo;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.uncertainty.BidRanking;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.EvaluatorDiscrete;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

//import group4.components.*;


@SuppressWarnings({"serial", "deprecation"})
public class Agent4 extends BoaParty {
    private Map<String, Double> issueValUtils = new HashMap<>();
    //private Map<String, Double> realValUtils = new HashMap<>();

    @Override
    public void init(NegotiationInfo info)
    {
        AcceptanceStrategy ac  = new Group4AS();
        OfferingStrategy   os  = new Group4OS();
        // OfferingStrategy 	os  = new TimeDependent_Offering();
        OpponentModel      om  = new Group4OM();
        OMStrategy         oms = new Group4OMS();
        //OMStrategy         oms = new NullStrategy();

        Map<String, Double> noparams = Collections.emptyMap();
        Map<String, Double> osParams = new HashMap<String, Double>();

        osParams.put("e", 0.2);
        osParams.put("av", 1.0);

        configure(ac, osParams, os, osParams, om, noparams, oms, noparams);
        //configure(ac, osParams, os, osParams, om, noparams, null, null);
        super.init(info);

        AbstractUtilitySpace utilitySpace = info.getUtilitySpace();
        AdditiveUtilitySpace additiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;
        System.out.println("---------------------Default Estimated Userspace");

        //this.spaceInspect(additiveUtilitySpace);

        /*for(Bid bid : userModel.getBidRanking().getBidOrder()) {
            System.out.println("Bid ranking "+ userModel.getBidRanking().getBidOrder().indexOf(bid) + " value: " + this.utilityEstimate(bid));
        }*/

        /*ExperimentalUserModel e = (ExperimentalUserModel) userModel ;
        UncertainAdditiveUtilitySpace realUSpace = e.getRealUtilitySpace();
        System.out.println("---------------------Real Userspace");
        this.spaceInspect(realUSpace);
        for(Bid bid : userModel.getBidRanking().getBidOrder()) {
            System.out.println("Bid ranking "+ userModel.getBidRanking().getBidOrder().indexOf(bid) + " value: " + this.realUtilityEstimate(bid));
        }*/

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
                double v = evaluatorDiscrete.getDoubleValue(valueDiscrete);
                System.out.println("Evaluation(getValue): " + evaluatorDiscrete.getDoubleValue(valueDiscrete));
                try {
                    System.out.println("Evaluation(getEvaluation): " + evaluatorDiscrete.getEvaluation(valueDiscrete));
                    //realValUtils.put(issue.getName() + optionName, evaluatorDiscrete.getEvaluation(valueDiscrete));
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

    private void updateUserParam() {
        System.out.println("updateUserParam");
        BidRanking userBidRanking = userModel.getBidRanking();
        List<Bid> rankedUserBids = userBidRanking.getBidOrder(); // From low to high
        int rankedBidsNum = rankedUserBids.size();
        long totalBidsNum = userModel.getDomain().getNumberOfPossibleBids();
        double elicitCost = user.getElicitationCost();

        try {
            lpMethod(rankedUserBids);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public AbstractUtilitySpace estimateUtilitySpace()
    {
        this.updateUserParam();
        System.out.println("estimateUtilitySpace");
        AdditiveUtilitySpaceFactory additiveUtilitySpaceFactory = new AdditiveUtilitySpaceFactory(getDomain());
        List<IssueDiscrete> issues = additiveUtilitySpaceFactory.getIssues();
        for (IssueDiscrete i : issues)
        {
            //additiveUtilitySpaceFactory.setWeight(i, rand.nextDouble());
            additiveUtilitySpaceFactory.setWeight(i, 1);
            for (ValueDiscrete v : i.getValues()) {
                //additiveUtilitySpaceFactory.setUtility(i, v, rand.nextDouble());
                additiveUtilitySpaceFactory.setUtility(i, v, this.issueValUtils.get(i.getName() + v.toString()));
                System.out.println(i+": "+v+": "+this.issueValUtils.get(i.getName() + v.toString()));
            }
        }

        // Normalize the weights, since we picked them randomly in [0, 1]
        //additiveUtilitySpaceFactory.normalizeWeights();

        // The factory is done with setting all parameters, now return the estimated utility space
        return additiveUtilitySpaceFactory.getUtilitySpace();
    }

    public double utilityEstimate(Bid bid) {
        double util = 0.0;
        for(Issue issue : bid.getIssues()) {
            util += this.issueValUtils.get(issue.getName() + bid.getValue(issue).toString());
        }
        return util;
    }

    /*public double realUtilityEstimate(Bid bid) {
        double util = 0.0;
        for(Issue issue : bid.getIssues()) {
            util += this.realValUtils.get(issue.getName() + bid.getValue(issue).toString());
        }
        return util;
    }*/


    private void lpMethod(List<Bid> bids) throws Exception {
        LPWizard lpw = new LPWizard();
        List<Issue> issues = userModel.getDomain().getIssues();
        int oSize = bids.size();
        // Constraints: each slack term is non-negative
        for(int i = 0; i < oSize-1; i++) {
            lpw.plus("Slack" + i, 1.0);
            lpw.addConstraint("s" + i, 0.0, "<=").plus("Slack" + i, 1.0);
        }
        //varList.stream().forEach(s -> lpw.plus(s, 1.0));
        // CONSTRAINT: comparisons
        for(int i=0; i < bids.size()-1; i++) {
            // Phi(o_i+1) - phi(o_i) + z >= 0
            LPWizardConstraint lpwc = lpw.addConstraint("c"+ i, 0d, "<=");
            for (Issue issue : issues) {
                if (!bids.get(i).getValue(issue).toString().equals(bids.get(i + 1).getValue(issue).toString())) {
                    lpwc.plus(issue.getName() + bids.get(i + 1).getValue(issue).toString(), 1.0)
                            .plus(issue.getName() + bids.get(i).getValue(issue).toString(), -1.0)
                            .plus("Slack" + i, 1.0);
                }
            }
        }
        // CONSTRAINT: util of each issue value is non-negative
        List<String> iVals = ivPairs();
        AtomicInteger k= new AtomicInteger(1);
        iVals.forEach(s -> lpw.addConstraint("v" + k.getAndIncrement(), 0.0, "<=").plus(s, 1.0));
        // CONSTRAINT: util of gloabl max bid is 1
        //Bid maxBid = additiveUtilitySpace.getMaxUtilityBid();
        // Use local maximum
        Bid maxBid = userModel.getBidRanking().getMaximalBid();
        LPWizardConstraint lpwc = lpw.addConstraint("max", 1.0, "=");
        for(Issue issue : issues) {
            lpwc.plus(issue.getName() + maxBid.getValue(issue).toString(), 1.0);
        }
        // CONSTRAINT: util of gloabl min bid is 0
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
            if(lpSolution.getDouble(s) < 0)
                this.issueValUtils.put(s, 0d);
            else
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