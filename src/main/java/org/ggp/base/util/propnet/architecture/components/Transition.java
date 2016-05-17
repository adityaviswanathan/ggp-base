package org.ggp.base.util.propnet.architecture.components;

import org.ggp.base.util.propnet.architecture.Component;

/**
 * The Transition class is designed to represent pass-through gates.
 */
@SuppressWarnings("serial")
public final class Transition extends Component
{
    /**
     * Recursively forward propoaate the new value of the component
     */
    @Override
    public void forwardPropagate(boolean val)
    {
        if (val != getValue()) {
            setValue(val);
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