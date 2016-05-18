package org.ggp.base.util.propnet.architecture;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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
    public enum Type {
        UNSET,              // UNSET
        TRANSITION,         // set in propNet.setComponentTypes()
        NOT,                // set in propNet.setComponentTypes()
        AND,                // set in propNet.setComponentTypes()
        OR,                 // set in propNet.setComponentTypes()
        CONSTANT,           // set in propNet.setComponentTypes()
        INIT_PROP,          // set in PropNet.recordInitProposition()
        TERMINAL_PROP,      // set in PropNet.recordTerminalProposition()
        LEGAL_PROP,         // set in PropNet.recordLegalPropositions()
        GOAL_PROP,          // set in PropNet.recordGoalPropositions()
        BASE_PROP,          // set in PropNet.recordBasePropositions()
        INPUT_PROP,         // set in PropNet.recordInputPropositions()
        VIEW_PROP;          // set in propNet.setComponentTypes()
    }

    // base identifier for components    
    private Type identifier = Type.UNSET;
    public void setType(Type type) { this.identifier = type; }
    public Type getType() { return this.identifier; }
    public boolean isTypeSet() { return this.identifier != Type.UNSET; }
    public String getTypeString() { return this.identifier.name(); } // returns UNSET, NOT.... depending on value
    public boolean isGateComponent() { 
        return (this.identifier == Type.NOT || this.identifier == Type.AND || this.identifier == Type.OR);
    }

    // the current marking of a component
    private boolean currMarking = false;
    public void setMarking(boolean mark) { this.currMarking = mark; }
    public boolean getMarking() { return this.currMarking; }

    // check whether the component's current marking is correct
    private boolean correctlyMarked = false;
    public void setCorrectlyMarked(boolean correct) { this.correctlyMarked = correct; }
    public boolean isCorrectlyMarked() { return this.correctlyMarked; }

    // original private variables
    private static final long serialVersionUID = 352524175700224447L;
    private final Set<Component> inputs;
    private final Set<Component> outputs;

    /**
     * Creates a new Component with no inputs or outputs.
     */
    public Component()
    {
        this.inputs = new HashSet<Component>();
        this.outputs = new HashSet<Component>();
        this.identifier = Type.UNSET;
        this.currMarking = false;
        this.correctlyMarked = false;
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