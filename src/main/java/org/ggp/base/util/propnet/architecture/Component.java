package org.ggp.base.util.propnet.architecture;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.List;


/**
 * The root class of the Component hierarchy, which is designed to represent
 * nodes in a PropNet. The general contract of derived classes is to override
 * all methods.
 * 
 * Andrew: Augmented class to add type enum, as well as currMarking, a modified flag,
 *          and a flag that determines the correctness of the component given it's parents
 */
public abstract class Component implements Serializable
{
    // helps to determine component type without using instance of
    public enum CmpType {
        UNSET,              // Default 
        TRANSITION,         // set in propNet.setComponentTypes()
        NOT,                // set in propNet.setComponentTypes()
        AND,                // set in propNet.setComponentTypes()
        OR,                 // set in propNet.setComponentTypes()
        CONSTANT,           // set in propNet.setComponentTypes()
        INIT_PROP,          // set in propNet.recordInitProposition()
        TERMINAL_PROP,      // set in propNet.recordTerminalProposition()
        LEGAL_PROP,         // set in PropNet.recordLegalPropositions()
        GOAL_PROP,          // set in PropNet.recordGoalPropositions()
        BASE_PROP,          // set in PropNet.recordBasePropositions()
        INPUT_PROP,         // set in PropNet.recordInputPropositions()
        VIEW_PROP;          // set in propNet.setComponentTypes()
    }

    // base identifier for components    
    private CmpType identifier = CmpType.UNSET;
    public void setType(CmpType type) { this.identifier = type; }
    public CmpType getType() { return this.identifier; }
    public boolean isTypeSet() { return this.identifier != CmpType.UNSET; }
    public String getTypeString() { return this.identifier.name(); }

    // speed improvements
    private boolean marked = false;
    private boolean mark = false;
    public boolean isMarked() { return this.marked; }
    public boolean getMarking() { return this.mark; }
    public void setMarking(boolean val) { 
        this.marked = true;
        this.mark = val; 
    }
    public void reset() {
        this.marked = false;
        this.mark = false;
    }

    // latch variables / methods
    private boolean latch = false; // whether component is a latch
    private boolean latchValue = false; // if component is a false latch or true latch
    private boolean isLatch() { return this.latch; }
    private void setIsLatch(boolean latchVal) { 
        this.latch = true;
        this.latchValue = latchVal;
    }

    // original private variables
    private static final long serialVersionUID = 352524175700224447L;
    private final Set<Component> inputs;
    private final Set<Component> outputs;

    private Component[] inputArr;
    public Component[] getInputArr() { return this.inputArr; } 
    private Component[] outputArr;
    public Component[] getOutputArr() { return this.outputArr; } 
    public void crystalize() {
        inputArr = new Component[inputs.size()];
        inputs.toArray(inputArr);
        outputArr = new Component[outputs.size()];
        outputs.toArray(outputArr);
    }

    private List<Component> upstreamProps = null;
    public List<Component> getUpstreamProps() { return this.upstreamProps; }
    public void setUpstreamProps(List<Component> upstream) { this.upstreamProps = upstream; }

    /**
     * Creates a new Component with no inputs or outputs.
     */
    public Component()
    {
        this.inputs = new HashSet<Component>();
        this.outputs = new HashSet<Component>();
    }

    /**
     * Adds a new input.
     *
     * @param input
     *            A new input.
     */
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

    /**
     * Adds a new output.
     *
     * @param output
     *            A new output.
     */
    public void addOutput(Component output)
    {
        outputs.add(output);
    }

    /**
     * Getter method.
     *
     * @return The inputs to the component.
     */
    public Set<Component> getInputs()
    {
        return inputs;
    }

    /**
     * A convenience method, to get a single input.
     * To be used only when the component is known to have
     * exactly one input.
     *
     * @return The single input to the component.
     */
    public Component getSingleInput() {
        assert inputs.size() == 1;
        return inputs.iterator().next();
    }

    /**
     * Getter method.
     *
     * @return The outputs of the component.
     */
    public Set<Component> getOutputs()
    {
        return outputs;
    }

    /**
     * A convenience method, to get a single output.
     * To be used only when the component is known to have
     * exactly one output.
     *
     * @return The single output to the component.
     */
    public Component getSingleOutput() {
        assert outputs.size() == 1;
        return outputs.iterator().next();
    }

    /**
     * Returns the value of the Component.
     *
     * @return The value of the Component.
     */
    public abstract boolean getValue();

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