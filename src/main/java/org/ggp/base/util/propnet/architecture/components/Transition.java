package org.ggp.base.util.propnet.architecture.components;

import org.ggp.base.util.propnet.architecture.Component;

/**
 * The Transition class is designed to represent pass-through gates.
 */
@SuppressWarnings("serial")
public final class Transition extends Component
{
    /**
     * Clears value of component as well as relevant component info
     */
    @Override
    public void clearComponent()
    {
        setValue(false);
        setLastValue(false);
    }

    /**
     * Recursively forward propoaate the new value of the component
     */
    @Override
    public void forwardPropagate(boolean val)
    {
        setValue(val);
        if (getValue() != getLastValue()) {
            setLastValue(value);
            Component[] outputArray = getOutputArray();
            for (Component comp : outputArray) {
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
        return toDot("box", "grey", "TRANSITION");
    }
}