package org.ggp.base.util.propnet.architecture;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The root class of the Component hierarchy, which is designed to represent
 * nodes in a PropNet. The general contract of derived classes is to override
 * all methods.
 */

public abstract class Component implements Serializable
{
    private static final long serialVersionUID = 352524175700224447L;
    
    private final Set<Component> inputs;
    private Component[] inputArray;
    protected Component[] getInputArray() { return this.inputArray; }
    private int numInputs;
    public int getNumInputs() { return this.numInputs; }

    private final Set<Component> outputs;
    private Component[] outputArray;
    protected Component[] getOutputArray() { return this.outputArray; }
    private int numOutputs;
    public int getNumOutputs() { return this.numOutputs; }

    protected boolean value = false;

    // set and get value of current component
    public void setValue(boolean val) { this.value = val; }
    public boolean getValue() { return this.value; }

    protected boolean lastValue = false; // cache last value to stop forward propogation

    // set and get last value of current component
    public void setLastValue(boolean val) { this.lastValue = val; }
    public boolean getLastValue() { return this.lastValue; }

    /**
     * Creates a new Component with no inputs or outputs.
     */
    public Component()
    {
        this.inputs = new HashSet<Component>();
        this.outputs = new HashSet<Component>();
    }

    public void initComponent()
    {
        this.numInputs = inputs.size();
        this.inputArray = new Component[this.numInputs];
        inputs.toArray(this.inputArray);

        this.numOutputs = outputs.size();
        this.outputArray = new Component[this.numOutputs];
        outputs.toArray(this.outputArray);
    }

    /**
     * Clears value of component as well as relevant component info
     */
    public abstract void clearComponent();

    /**
     * Recursively forward propogate the new value of the component
     */
    public abstract void forwardPropagate(boolean val);


    public void addInput(Component input)
    {
        inputs.add(input);
    }

    public void removeInput(Component input)
    {
        inputs.remove(input);
    }

    public void removeOutput(Component output)
    {
        outputs.remove(output);
    }

    public void removeAllInputs()
    {
        inputs.clear();
    }

    public void removeAllOutputs()
    {
        outputs.clear();
    }

    public void addOutput(Component output)
    {
        outputs.add(output);
    }

    public Set<Component> getInputs()
    {
        return inputs;
    }

    public Component getSingleInput() {
        assert inputs.size() == 1;
        return inputs.iterator().next();
    }

    public Set<Component> getOutputs()
    {
        return outputs;
    }

    public Component getSingleOutput() 
    {
        assert outputs.size() == 1;
        return outputs.iterator().next();
    }

    /**
     * Returns a representation of the Component in .dot format.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public abstract String toString();

    /**
     * Returns a configurable representation of the Component in .dot format.
     *
     * @param shape
     *            The value to use as the <tt>shape</tt> attribute.
     * @param fillcolor
     *            The value to use as the <tt>fillcolor</tt> attribute.
     * @param label
     *            The value to use as the <tt>label</tt> attribute.
     * @return A representation of the Component in .dot format.
     */
    protected String toDot(String shape, String fillcolor, String label)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("\"@" + Integer.toHexString(hashCode()) + "\"[shape=" + shape + ", style= filled, fillcolor=" + fillcolor + ", label=\"" + label + "\"]; ");
        for ( Component component : getOutputs() )
        {
            sb.append("\"@" + Integer.toHexString(hashCode()) + "\"->" + "\"@" + Integer.toHexString(component.hashCode()) + "\"; ");
        }

        return sb.toString();
    }

}