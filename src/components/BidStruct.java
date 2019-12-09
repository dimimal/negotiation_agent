package components;

import genius.core.Bid;

public class BidStruct implements Comparable<BidStruct>  {

    Bid bid;
    double value;
    /*
    Constructor
     */
    public BidStruct(Bid bidIn, double valueIn) {
        bid = bidIn;
        value = valueIn;
    }

    @Override
    public int compareTo(BidStruct compBidStruct) {
        return Double.compare(compBidStruct.value, value);
    }

    @Override
    public boolean equals(Object obj) {
        return bid.equals(((BidStruct)obj).bid);
    }
}
