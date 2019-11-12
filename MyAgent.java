package group4;

import java.util.*;
import java.util.stream.Collectors;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.EndNegotiation;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.ValueDiscrete;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.EvaluatorDiscrete;

/**
 * A simple example agent that makes random bids above a minimum target utility.
 *
 * @author Tim Baarslag
 */
public class MyAgent extends AbstractNegotiationParty
{
	private static double MINIMUM_TARGET = 0.8;
	private double beta = 1.0;
	private Bid lastOffer;
	private double bidsReceived = 0.0;
	private String ISSUE_WEIGHT = "IssueWeight";
	// Key: issue number; value: option name, List contains option frequency, estimated option value
	private HashMap<Integer, Map<String, List<Double>>> issueOptionFrequecies = new HashMap<>();

	/**
	 * Initializes a new instance of the agent.
	 */
	@Override
	public void init(NegotiationInfo info)
	{
		super.init(info);
		// Lab 4 Exercise

		if (hasPreferenceUncertainty()) {
			System.out.println("Preference uncertainty is enabled.");
		}
		AbstractUtilitySpace utilitySpace = info.getUtilitySpace();
		AdditiveUtilitySpace additiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;

		System.out.println("The Agent ID is: " + info.getAgentID());
		System.out.println("User model is: " + info.getUserModel());
		System.out.println("Number of bids: " + info.getUserModel().getBidRanking().getSize());
		System.out.println("Elicitation Cost: " + info.getUser().getElicitationCost());



		List<Issue> issues = additiveUtilitySpace.getDomain().getIssues();
		for (Issue issue : issues) {
			int issueNumber = issue.getNumber();
			Map<String, List<Double>> optionFrequency = new HashMap<>();
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
				optionFrequency.put(optionName, new ArrayList<>(Arrays.asList(0.0, 0.0)));
			}
			// issue weight and normalized weight
			optionFrequency.put(ISSUE_WEIGHT, new ArrayList<>(Arrays.asList(0.0, 0.0)));
			issueOptionFrequecies.put(issueNumber, optionFrequency);
		}
	}

	/**
	 * Makes a random offer above the minimum utility target
	 * Accepts everything above the reservation value at the very end of the negotiation; or breaks off otherwise.
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions)
	{
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>choose action");
		double time = getTimeLine().getTime();
		double threshold = 0.6;
		double targetUtil = genTargetUtil(time, beta, threshold);
		//double within_limit_thres = 0.8;

		// Check for acceptance if we have received an offer
		if (lastOffer != null)
			if (time >= 0.99) {
				if (getUtility(lastOffer) >= threshold)
					// if (getUtility(lastOffer) >= utilitySpace.getReservationValue())
					return new Accept(getPartyId(), lastOffer);
				else
					return new EndNegotiation(getPartyId());
			}
			else {
				if (getUtility(lastOffer) >= targetUtil)
					// if (getUtility(lastOffer) >= utilitySpace.getReservationValue())
					return new Accept(getPartyId(), lastOffer);
				else
					// Otherwise, send out a random offer above the target utility
					return new Offer(getPartyId(), generateRandomBidAboveTarget());
			}

		// Otherwise, send out a random offer above the target utility
		return new Offer(getPartyId(), generateRandomBidAboveTarget());
	}

	private Bid generateRandomBidAboveTarget()
	{
		Bid randomBid;
		double util;
		int i = 0;
		// try 100 times to find a bid under the target utility
		do
		{
			randomBid = generateRandomBid();
			util = utilitySpace.getUtility(randomBid);
		}
		while (util < MINIMUM_TARGET && i++ < 100);
		return randomBid;
	}

	/**
	 * Remembers the offers received by the opponent.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action)
	{
		//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>receive message");
		if (action instanceof Offer)
		{
			System.out.println(">>>>>>>>>>>>>>>> Bid Received");
			lastOffer = ((Offer) action).getBid();
			this.updateFrequencies();
			this.updateIssueWeight();
			System.out.println(">>>>>>>>>>>>>>>> Estimated opponent bid util: " + this.utilityPrediction(lastOffer));
		}
		if(action instanceof EndNegotiation)
			System.out.println(">>>>>>>>>>>>>>>> Negotiation Failed");
		if(action instanceof Accept)
			System.out.println(">>>>>>>>>>>>>>>> Negotiation Successful");
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
				/*System.out.println(">>>>>>>>>>>>>>>index  " + i);
				System.out.println(">>>>>>>>>>>>>>>rank  " + rank);
				System.out.println(">>>>>>>>>>>>>>>freq  " + sortedMap.get(key).get(0));
				System.out.println(">>>>>>>>>>>>>>>key  " + key);*/

				double w = (sortedMap.size()-rank+0d)/(sortedMap.size()-1d);
				sortedMap.get(key).set(1, w);
				i += 1;
				//System.out.println(key + ": " + map.get(key));
			}
		}
		//updateIssueWeight();
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
		// second loop, update normalized weights
		for(Integer i : issueOptionFrequecies.keySet()) {
			issueOptionFrequecies.get(i).get(ISSUE_WEIGHT).set(1, issueOptionFrequecies.get(i).get(ISSUE_WEIGHT).get(0) / weightSum);
		}
	}

	private double utilityPrediction(Bid bid) {
		double util = 0.0;
		for(Issue issue : bid.getIssues()) {
			util += this.issueOptionFrequecies.get(issue.getNumber()).get(ISSUE_WEIGHT).get(1)
					* this.issueOptionFrequecies.get(issue.getNumber()).get(bid.getValue(issue).toString()).get(1);
		}
		return util;
	}
	private Bid getMaxUtilityBid() {
		try {
			return utilitySpace.getMaxUtilityBid();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public String getDescription()
	{
		return "Places random bids >= " + MINIMUM_TARGET;
	}

	/**
	 * This stub can be expanded to deal with preference uncertainty in a more sophisticated way than the default behavior.
	 */
	@Override
	public AbstractUtilitySpace estimateUtilitySpace()
	{
		return super.estimateUtilitySpace();
	}

	/**
	 * Target Utility Generator
	 *
	 * Time-dependent:
	 *      U_target(t) = U_min + (1 - F(t)) * (U_max - U_min)
	 *      F(t) = (t/T_max)^(1/b)
	 *      b>1 Conceder; b=1 Linear; b<1 boulware
	 */
	private double genTargetUtil(double t, double beta, double minUtil) {
		double maxUtil = this.utilitySpace.getUtility(this.getMaxUtilityBid());
		return minUtil + (1 - Math.pow(t,1/beta)) * (maxUtil - minUtil);
	}
	public void setBeta(double b) {this.beta = b;}
	public double getBeta() {return this.beta;}
}

class OptionComparator implements Comparator<List<Double>> {
	@Override
	public int compare(List<Double> o1, List<Double> o2) {
	    /*
	    if(o1.get(0) > o2.get(0)) return -1;
		if(o1.get(0) < o2.get(0)) return 1;
		return 0;
	     */
		return o2.get(0).compareTo(o1.get(0));
	}
}