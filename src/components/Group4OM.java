package components;

import genius.core.Bid;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OpponentModel;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.ValueDiscrete;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.EvaluatorDiscrete;
import group4.OptionComparator;

import java.util.*;
import java.util.stream.Collectors;

public class Group4OM extends OpponentModel {

    /**
     * As {@link #updateModel(Bid)} but with the current time added.
     *
     * @param bid
     * @param time
     */
    private String ISSUE_WEIGHT = "IssueWeight";
    private Bid lastOffer;
    private double bidsReceived = 0.0;

    // Key: issue number; value: option name, List contains option frequency, estimated option value
    private HashMap<Integer, Map<String, List<Double>>> issueOptionFrequecies = new HashMap<>();

    @Override
    public void init(NegotiationSession negotiationSession, Map<String, Double> parameters) {
        this.negotiationSession = negotiationSession;
        opponentUtilitySpace = (AdditiveUtilitySpace) negotiationSession
                .getUtilitySpace().copy();

        List<Issue> issues = opponentUtilitySpace.getDomain().getIssues();
        for (Issue issue : issues) {
            int issueNumber = issue.getNumber();
            Map<String, List<Double>> optionFrequency = new HashMap<>();
            System.out.println(">> " + issue.getName() + " weight: " + opponentUtilitySpace.getWeight(issueNumber));

            // Assuming that issues are discrete only
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            EvaluatorDiscrete evaluatorDiscrete = (EvaluatorDiscrete) opponentUtilitySpace.getEvaluator(issueNumber);

            for (ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
                String optionName = valueDiscrete.getValue();
                System.out.println(optionName);
                System.out.println("Evaluation(getValue): " + evaluatorDiscrete.getValue(valueDiscrete));
                try {
                    System.out.println("Evaluation(getEvaluation): " + evaluatorDiscrete.getEvaluation(valueDiscrete));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                optionFrequency.put(optionName, new ArrayList<>(Arrays.asList(0.0, 0.0)));
            }
            // issue weight and normalized weight
            optionFrequency.put(ISSUE_WEIGHT, new ArrayList<>(Arrays.asList(0.0, 0.0)));
            issueOptionFrequecies.put(issueNumber, optionFrequency);
        }
    }

    @Override
    public double getBidEvaluation(Bid bid) {
        double lastBid = 0;
        try {
            // TODO Update the weights in the op utility space
            lastBid = opponentUtilitySpace.getUtility(bid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastBid;
    }

    private void updateFrequencies() {
        this.bidsReceived += 1;
        // update frequency
        for(Issue issue : lastOffer.getIssues()){
            String option = lastOffer.getValue(issue).toString();
            Map<String, List<Double>> frequencies = issueOptionFrequecies.get(issue.getNumber());
            // add 1 to counter
            frequencies.get(option).set(0, frequencies.get(option).get(0) + 1);
            issueOptionFrequecies.put(issue.getNumber(), frequencies);
        }
        // calculate option value
        for(Map<String, List<Double>> map : issueOptionFrequecies.values()) {
            // sort the map by the frequency
            Map<String, List<Double>> sortedMap = map.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(new OptionComparator()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            // calculate estimated value of each option
            int rank = 1;
            int i = 1;
            double prev = -1d;
            for(String key : sortedMap.keySet()) {
                if (key.equals(ISSUE_WEIGHT)) continue;
                //int i = new ArrayList<>(sortedMap.keySet()).indexOf(key);
                if(prev == -1d) {
                    prev = sortedMap.get(key).get(0);
                } else {
                    if(sortedMap.get(key).get(0) < prev) {
                        rank = i;
                        prev = sortedMap.get(key).get(0);
                    }
                }
                double w = (sortedMap.size()-rank + 0d)/(sortedMap.size()-1d);
                sortedMap.get(key).set(1, w);
                i += 1;
            }
        }
    }
    @Override
    public void updateModel(Bid opBid, double time) {
        lastOffer = opBid;
        this.updateFrequencies();
        this.updateIssueWeight();
        this.updateEstimatedOpponentUtility();
    }

    /*
     * Place the estimated opponent utility weights into opponent's utility space
     */
    private void updateEstimatedOpponentUtility() {
        double estWeight;
        List<Issue> issues = opponentUtilitySpace.getDomain().getIssues();
        for (Issue issue : issues) {
            estWeight = issueOptionFrequecies.get(issue.getNumber()).get(ISSUE_WEIGHT).get(1);
            opponentUtilitySpace.setWeight(issue, estWeight);
        }

    }

    private void updateIssueWeight() {
        double weightSum = 0.0;
        // first loop, update weights
        for(Integer i : issueOptionFrequecies.keySet()) {
            Map<String, List<Double>> optionFrequencies = issueOptionFrequecies.get(i);
            double issueWeight = 0.0;
            for(List<Double> l : optionFrequencies.values()) {
                issueWeight = issueWeight + Math.pow(l.get(0), 2);
            }
            issueWeight = issueWeight / Math.pow(this.bidsReceived, 2);
            weightSum += issueWeight;
            optionFrequencies.get(ISSUE_WEIGHT).set(0, issueWeight);
        }
        // Update the normalized weights
        for(Integer i : issueOptionFrequecies.keySet()) {
            issueOptionFrequecies.get(i).get(ISSUE_WEIGHT).set(1, issueOptionFrequecies.get(i).get(ISSUE_WEIGHT).get(0) / weightSum);
        }
    }

    /*
     *  Get the issue option frequencies
     */
    public HashMap<Integer, Map<String, List<Double>>> getIssueOptionFreqs() {
        return this.issueOptionFrequecies;
    }

    @Override
    public String getName() {
        return "Johnny black Opponent Modelling";
    }
}
