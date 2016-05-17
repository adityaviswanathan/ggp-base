package org.ggp.base.util.propnet.architecture.components;

import org.ggp.base.util.propnet.architecture.Component;

/**
 * The Or class is designed to represent logical OR gates.
 */
@SuppressWarnings("serial")
public final class Or extends Component
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
        setValue((numTrue != 0) ^ false);
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
     * Recursively forward propogate the new value of the component
     */
    public void forwardPropagate2(boolean val)
    {
        // each time an example is propogated, if an example is true, the propogate true
        // the first time an example is true, the rest of propogations will do nothing
        /*
        if (timesPropogated < getInputArray().length) {
            timesPropogated++;
            setValue(getValue() || val);
            if (getValue()) {
                timesPropogated = getInputArray().length;
                if (getValue() != getLastValue()) {
                    setLastValue(getValue());
                    Component[] outputs = getOutputArray();
                    for (Component comp : outputs) {
                        comp.forwardPropagate(getValue());
                    }
                }
            } else if (timesPropogated == getInputArray().length) {
                if (getValue() != getLastValue()) {
                    setLastValue(getValue());
                    Component[] outputs = getOutputArray();
                    for (Component comp : outputs) {
                        comp.forwardPropagate(getValue());
                    }
                }
            }
        } 
        */
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