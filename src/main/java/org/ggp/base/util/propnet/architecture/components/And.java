package org.ggp.base.util.propnet.architecture.components;

import org.ggp.base.util.propnet.architecture.Component;

/**
 * The And class is designed to represent logical AND gates.
 */
@SuppressWarnings("serial")
public final class And extends Component
{
    // current number of true input components
    private int trueInputs = 0;
    
    @Override
    public void forwardPropagate(boolean val)
    {
        if (val)
            trueInputs++;
        else 
            trueInputs--;

        boolean newVal = (trueInputs == getNumInputs());
        setValue(newVal);

        if (getValue() != getLastValue()) {
            setLastValue(getValue());
            Component[] outputs = getOutputArray();
            for (Component comp : outputs) {
                comp.forwardPropagate(getValue());
            }
        }
    }

    /**
     * @see org.ggp.base.util.propnet.architecture.Component#toString()
     */
    @Override
    public String toString()
    {
        return toDot("invhouse", "grey", "AND");
    }

}
