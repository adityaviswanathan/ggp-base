package org.ggp.base.util.propnet.architecture.components;

import org.ggp.base.util.propnet.architecture.Component;

/**
 * The Not class is designed to represent logical NOT gates.
 */
@SuppressWarnings("serial")
public final class Not extends Component
{
    /**
     * Recursively forward propogate the new value of the component
     */
    @Override
    public void forwardPropagate(boolean unused)
    {
        setValue(!getSingleInput().getValue());
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
        return toDot("invtriangle", "grey", "NOT");
    }
}