package org.ggp.base.util.propnet.architecture.components;

import org.ggp.base.util.propnet.architecture.Component;

/**
 * The Constant class is designed to represent nodes with fixed logical values.
 */
@SuppressWarnings("serial")
public final class Constant extends Component
{
    /**
     * Creates a new Constant with value <tt>value</tt>.
     *
     * @param value
     *            The value of the Constant.
     */
    public Constant(boolean value)
    {
        setValue(value);
    }

    /**
     * Clears value of component as well as relevant component info
     */
    @Override
    public void clearComponent()
    {
        // No need to change value
    }

    /**
     * Recursively forward propogate the new value of the component
     */
    @Override
    public void forwardPropagate(boolean val)
    {
        Component[] outputs = getOutputArray();
        for (Component comp : outputs) {
            forwardPropagate(getValue());
        }
    }

    /**
     * @see org.ggp.base.util.propnet.architecture.Component#toString()
     */
    @Override
    public String toString()
    {
        return toDot("doublecircle", "grey", Boolean.toString(value).toUpperCase());
    }
}