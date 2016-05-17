package org.ggp.base.util.propnet.architecture.components;

import org.ggp.base.util.propnet.architecture.Component;

/**
 * The Or class is designed to represent logical OR gates.
 */
@SuppressWarnings("serial")
public final class Or extends Component
{
    // current number of true input components
    private int trueInputs = 0;

    /**
     * Recursively forward propogate the new value of the component
     */
    @Override
    public void forwardPropagate(boolean val)
    {
        if (val) {
            trueInputs++;
        }
        else trueInputs--;
        
        setValue(trueInputs > 0);
        
        Component[] inputs = getInputArray();
        for (Component comp : inputs) {
            if (comp.getValue()) {
                setValue(true);
                break;
            }
        }

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
        return toDot("ellipse", "grey", "OR");
    }
}