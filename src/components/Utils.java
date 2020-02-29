package components;

import genius.core.issue.IssueDiscrete;
import genius.core.utility.AdditiveUtilitySpace;


/*
Class utils implements utility helper functions for the modules
 */
public class Utils {
    private static int numIssues = 0;

    public static int[][] getOrderedIssueValues(AdditiveUtilitySpace utilSpace) {
        numIssues = utilSpace.getNrOfEvaluators();
        int temp;
        int numValues;
        int[][] sortedValues = new int[numIssues][];

        for (int i = 0; i < numIssues; i++)
        {
            // getIssue deprecated??
            numValues = ((IssueDiscrete) (utilSpace.getIssue(i))).getNumberOfValues();
            sortedValues[i] = new int[numValues];
        }
        for (int i = 0; i < sortedValues.length; ++i)
        {
            for (int j = 0; j < sortedValues[i].length; ++j)
            {
                sortedValues[i][j] = j + 1;
            }
        }
        return sortedValues;
    }


    /*
    * Create the issues in descending order according to their weight
    * */
    public static int[] getOrderedIssues(AdditiveUtilitySpace utilSpace) {
        numIssues = utilSpace.getNrOfEvaluators();
        int[] orderedIssues = new int[numIssues];

        // Init Issue order list
        for (int i = 0; i < orderedIssues.length; i++) {
            orderedIssues[i] = i;
        }


        return orderedIssues;
    }






}
