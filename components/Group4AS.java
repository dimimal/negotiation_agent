package group4.components;

import genius.core.boaframework.AcceptanceStrategy;
import genius.core.boaframework.Actions;

public class Group4AS extends AcceptanceStrategy {
    /**
     * Determines to either to either accept or reject the opponent's bid or
     * even quit the negotiation.
     *
     * @return one of three possible actions: Actions.Accept, Actions.Reject,
     * Actions.Break.
     */
    @Override
    public Actions determineAcceptability() {
        return null;
    }

    /**
     * @return a short name for this component.
     */
    @Override
    public String getName() {
        return null;
    }
}
