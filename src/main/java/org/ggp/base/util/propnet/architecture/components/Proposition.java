package org.ggp.base.util.propnet.architecture.components;

import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.propnet.architecture.Component;

/**
 * The Proposition class is designed to represent named latches.
 */
@SuppressWarnings("serial")
public final class Proposition extends Component
{
    /** The name of the Proposition. */
    private GdlSentence name;

    private boolean baseProp = false;
    public void setIsBaseProp() { this.baseProp = true; }
    public boolean isBaseProp() { return this.baseProp; }

    public boolean inputProp = false;
    public void setIsInputProp() { this.inputProp = true; }
    public boolean isInputProp() { return this.inputProp; }

    /**
     * Creates a new Proposition with name <tt>name</tt>.
     *
     * @param name
     *            The name of the Proposition.
     */
    public Proposition(GdlSentence name)
    {
        this.name = name;
        setValue(false);
    }

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
        if (isInputProp() || isBaseProp()) {
            return;
        } else {
            setValue(val);
            if (getValue() != getLastValue()) {
                setLastValue(getValue());
                Component[] outputArray = getOutputArray();
                for (Component comp : outputArray) {
                    comp.forwardPropagate(val);
                }
            }
        }
    }

    /**
     * Begin forward propogation from the current Proposition
     */
    public void beginForwardPropagation(boolean val)
    {
        setValue(val);
        setLastValue(getValue());
        Component[] outputArray = getOutputArray();
        for (Component comp : outputArray) {
            comp.forwardPropagate(val);
        }
    }

    public void startPropagate()
    {
        setLastValue(getValue());
        for (Component c : getOutputArray()) {
            c.forwardPropagate(getValue());
        }    
    }

    /**
     * Getter method.
     *
     * @return The name of the Proposition.
     */
    public GdlSentence getName()
    {
        return name;
    }

    /**
     * Setter method.
     *
     * This should only be rarely used; the name of a proposition
     * is usually constant over its entire lifetime.
     */
    public void setName(GdlSentence newName)
    {
        name = newName;
    }

    /**
     * @see org.ggp.base.util.propnet.architecture.Component#toString()
     */
    @Override
    public String toString()
    {
        return toDot("circle", value ? "red" : "white", name.toString());
    }
}