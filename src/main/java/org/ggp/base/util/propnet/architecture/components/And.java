package org.ggp.base.util.propnet.architecture.components;

import org.ggp.base.util.propnet.architecture.Component;

/**
 * The And class is designed to represent logical AND gates.
 */
@SuppressWarnings("serial")
public final class And extends Component
{

    private int numTrue = 0;

    /**
     * Clears value of component as well as relevant component info
     */
    @Override
    public void clearComponent()
    {
        setValue(false);
        setLastValue(false);
        this.numTrue = 0;
    }

    /**
     * Recursively forward propogate the new value of the component
     * REFACTOR
     */
    @Override
    public void forwardPropagate(boolean val)
    {
        numTrue += (val) ? 1 : -1;
        setValue((numTrue == getNumInputs()) ^ false);
        if (getValue() != getLastValue()) {
            setLastValue(getValue());
            Component[] outputs = getOutputArray();
            for (Component comp : outputs) {
                comp.forwardPropagate(getValue());
            }
        }
    }

    /**
     * Recursively forward propogate the new value of the component
     */
    public void forwardPropagate2(boolean val)
    {
        /*
        if (timesPropogated < getInputArray().length) {
            timesPropogated++;
            if (!val) {
                falseEncountered = true;
                timesPropogated = getInputArray().length;
                setValue(false);
                if (getValue() != getLastValue()) {
                    setLastValue(getValue());
                    Component[] outputs = getOutputArray();
                    for (Component comp : outputs) {
                        comp.forwardPropagate(getValue());
                    }
                }
            } 
        } else if (timesPropogated == numInputs) {
            // 
        }
        */
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
