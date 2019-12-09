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

    public static int[] calcOrderOfIssues(AdditiveUtilitySpace uSpace) {
        numIssues = uSpace.getNrOfEvaluators();
        int[] issueOrder = new int[numIssues];

        // Init Issue order list
        for (int i = 0; i < issueOrder.length; i++) {
            issueOrder[i] = i;
        }

        // Sort issues based on their weight
        for (int i = 0; i < issueOrder.length; i++) {
            for (int j = i + 1; j < issueOrder.length; j++) {
                if (uSpace.getWeight(issueOrder[i]) < uSpace.getWeight(issueOrder[j])) {
                    int temp = issueOrder[i];
                    issueOrder[i] = issueOrder[j];
                    issueOrder[j] = temp;
                }
            }
        }
        return issueOrder;
    }

    public static int[][] calcOrderOfIssueVals(AdditiveUtilitySpace uSpace) {
        numIssues = uSpace.getNrOfEvaluators();
        int[][] orderOfVals = new int[numIssues][];
        for (int i = 0; i < numIssues; i++) {
            int noVals = ((IssueDiscrete) (uSpace.getIssue(i))).getNumberOfValues();
            orderOfVals[i] = new int[noVals];
        }
        for (int i = 0; i < orderOfVals.length; ++i) {
            for (int j = 0; j < orderOfVals[i].length; ++j) {
                orderOfVals[i][j] = j + 1;
            }
        }
        // TODO Not Ordering
        for (int i = 0; i < orderOfVals.length; i++) {
            for (int j = 0; j < orderOfVals[i].length; j++) {
                double value1 = getValueOfIssueVal(uSpace, i, j);
                for (int k = j; k < orderOfVals[i].length; k++) {
                    double value2 = getValueOfIssueVal(uSpace, i, k);
                    if (value1 < value2) {
                        int temp = orderOfVals[i][j];
                        orderOfVals[i][j] = orderOfVals[i][k];
                        orderOfVals[i][k] = temp;
                    }
                }
            }
        }
        return orderOfVals;
    }

    public static double getValueOfIssueVal(AdditiveUtilitySpace us, int issue, int val) {
        Bid temp = getCopyOfBestBid(us);
        //ValueDiscrete vd = getVal(us, issue, val);
        ValueDiscrete vd = ((IssueDiscrete) us.getIssue(issue)).getValue(val);
        temp = temp.putValue(issue + 1, vd);
        double value = evaluateOneIssue(us, issue, temp);
        return value;
    }

//    public static ValueDiscrete getVal(AdditiveUtilitySpace us, int issue, int valID) {
//        IssueDiscrete is = (IssueDiscrete) us.getIssue(issue);
//        return is.getValue(valID);
//    }

    public static double evaluateOneIssue(AdditiveUtilitySpace us, int issue, Bid b) {
        try {
            return us.getEvaluator(issue + 1).getEvaluation(us, b, issue + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static Bid getCopyOfBestBid(AdditiveUtilitySpace us) {
        try {
            return new Bid(us.getMaxUtilityBid());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
