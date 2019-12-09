package components;

import genius.core.Bid;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.ValueDiscrete;
import genius.core.utility.AdditiveUtilitySpace;

/*
Class utils implements utility helper functions for the modules
 */
public class Utils {
    private static int numIssues = 0;

    public static int[][] getOrderedIssueValues(AdditiveUtilitySpace utilSpace) {
        numIssues = utilSpace.getNrOfEvaluators();
        int[][] sortedValues = new int[numIssues][];

        for (int i = 0; i < numIssues; i++) {
            int numValues = ((IssueDiscrete) (utilSpace.getIssue(i))).getNumberOfValues();
            sortedValues[i] = new int[numValues];
        }
        for (int i = 0; i < sortedValues.length; ++i) {
            for (int j = 0; j < sortedValues[i].length; ++j) {
                sortedValues[i][j] = j + 1;
            }
        }

        //
        for (int i = 0; i < sortedValues.length; i++) {
            for (int j = 0; j < sortedValues[i].length; j++) {
                double value1 = getValueOfIssueVal(utilSpace, i, j);
                for (int k = j; k < sortedValues[i].length; k++) {
                    double value2 = getValueOfIssueVal(utilSpace, i, k);
                    if (value1 < value2) {
                        int temp = sortedValues[i][j];
                        sortedValues[i][j] = sortedValues[i][k];
                        sortedValues[i][k] = temp;
                    }
                }
            }
        }
        return sortedValues;
    }

    public static double getValueOfIssueVal(AdditiveUtilitySpace utilSpace, int issue, int value) {
        double issueValue;
        Bid temp = getMaximumBid(utilSpace);
        ValueDiscrete evalIssue = ((IssueDiscrete) utilSpace.getIssue(issue)).getValue(value);
        temp = temp.putValue(issue + 1, evalIssue);
        issueValue = evalIssue(utilSpace, temp, issue);
        return issueValue;
    }

    public static int[] getOrderedIssues(AdditiveUtilitySpace utilSpace) {
        numIssues = utilSpace.getNrOfEvaluators();
        int[] orderedIssues = new int[numIssues];
        int temp;

        // Init Issue order list
        for (int i = 0; i < orderedIssues.length; i++) {
            orderedIssues[i] = i;
        }

        // Sort issues based on their weight
        for (int i = 0; i < orderedIssues.length; i++) {
            for (int j = i + 1; j < orderedIssues.length; j++) {
                if (utilSpace.getWeight(orderedIssues[i]) < utilSpace.getWeight(orderedIssues[j])) {
                    temp = orderedIssues[i];
                    orderedIssues[i] = orderedIssues[j];
                    orderedIssues[j] = temp;
                }
            }
        }
        return orderedIssues;
    }

    public static Bid getMaximumBid(AdditiveUtilitySpace utilSpace) {
        try {
            return new Bid(utilSpace.getMaxUtilityBid());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static double evalIssue(AdditiveUtilitySpace utilSpace, Bid bid, int issue) {
        try {
            return utilSpace.getEvaluator(issue + 1).getEvaluation(utilSpace, bid, issue + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }


}
