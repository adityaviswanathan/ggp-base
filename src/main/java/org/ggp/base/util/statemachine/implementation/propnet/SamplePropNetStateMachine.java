package org.ggp.base.util.statemachine.implementation.propnet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import java.lang.*;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.propnet.architecture.Component;
import org.ggp.base.util.propnet.architecture.components.*;
import org.ggp.base.util.propnet.architecture.PropNet;
import org.ggp.base.util.propnet.architecture.components.Proposition;
import org.ggp.base.util.propnet.factory.OptimizingPropNetFactory;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.query.ProverQueryBuilder;

@SuppressWarnings("unused")
public class SamplePropNetStateMachine extends StateMachine {
    
    // print wrappers
    private static final boolean LOGGING = true;
    private void LOG(String str) { if (LOGGING) System.out.println(str); }
    private void PRINT(String str) { System.out.println(str); }

    /** The underlying proposition network  */
    private PropNet propNet;
    /** The topological ordering of the propositions */
    private List<Proposition> ordering;
    /** The player roles */
    private List<Role> roles;


    private MachineState initialState = null;

    /**
     * Initializes the PropNetStateMachine. You should compute the topological
     * ordering here. Additionally you may compute the initial state here, at
     * your discretion.
     */
    @Override
    public void initialize(List<Gdl> description) {
        try {
            propNet = OptimizingPropNetFactory.create(description);
            roles = propNet.getRoles();
            initialState = setInitialMachineState();
            ordering = getOrdering(); // compute the topological ordering here
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Computes if the state is terminal. Should return the value
     * of the terminal proposition for the state.
     */
    @Override
    public boolean isTerminal(MachineState state) {
        // TODO: Compute whether the MachineState is terminal.
        return false;
    }

    /**
     * Computes the goal for a role in the current state.
     * Should return the value of the goal proposition that
     * is true for that role. If there is not exactly one goal
     * proposition true for that role, then you should throw a
     * GoalDefinitionException because the goal is ill-defined.
     */
    @Override
    public int getGoal(MachineState state, Role role)
            throws GoalDefinitionException {
        // TODO: Compute the goal for role in state.
        return -1;
    }

    /**
     * Sets truth value of init proposition to true and then computes
     * the resulting State. Called only once during initialization.
     */
    private MachineState setInitialMachineState() {
        Proposition initProp = propNet.getInitProposition();
        initProp.setValue(true);
        return getStateFromBase();
    }

    /**
     * Returns the initial state. The initial state can be computed
     * by only setting the truth value of the INIT proposition to true,
     * and then computing the resulting state.
     */
    @Override
    public MachineState getInitialState() {
        return initialState;
    }

    /**
     * Computes all possible actions for role.
     */
    @Override
    public List<Move> findActions(Role role)
            throws MoveDefinitionException {
        return null;
    }

    /**
     * Computes the legal moves for role in state.
     */
    @Override
    public List<Move> getLegalMoves(MachineState state, Role role)
            throws MoveDefinitionException {
        return null;
    }

    /**
     * Computes the next state given state and the list of moves.
     */
    @Override
    public MachineState getNextState(MachineState state, List<Move> moves)
            throws TransitionDefinitionException {
        // TODO: Compute the next state.
        return null;
    }

    /**
     * This should compute the topological ordering of propositions.
     * Each component is either a proposition, logical gate, or transition.
     * Logical gates and transitions only have propositions as inputs.
     *
     * The base propositions and input propositions should always be exempt
     * from this ordering.
     *
     * The base propositions values are set from the MachineState that
     * operations are performed on and the input propositions are set from
     * the Moves that operations are performed on as well (if any).
     *
     * @return The order in which the truth values of propositions need to be set.
     */
    public List<Proposition> getOrdering()
    {
        List<Component> components = new ArrayList<Component>(propNet.getComponents());
        List<Proposition> props = new ArrayList<Proposition>(propNet.getPropositions());
        List<Proposition> result = buildOrdering(propNet.getInitProposition(), new HashSet<Proposition>());
        PRINT("Ordering of propositions of size: " + result.size());
        PRINT("Total propositions: " + propNet.getPropositions().size());
        PRINT("Total base propositions: " + propNet.getBasePropositions().size());
        PRINT("Total goal propositions: " + propNet.getGoalPropositions().size());
        int validNodes = 0;
        int foundValidNodes = 0;
        for (Proposition prop : propNet.getPropositions()) {
            if (propNet.getInputPropositions().get(prop.getName()) == null 
                && propNet.getBasePropositions().get(prop.getName()) == null) {
                if (result.contains(prop)) {
                    foundValidNodes++;
                } else {
                    PRINT(prop.getName().toString() + " inputs: " + prop.getInputs().size() + " outputs: " + prop.getOutputs().size());
                }
                validNodes++;
            }
        }
        PRINT("Valid nodes: " + validNodes);
        PRINT("Found valid nodes: " + foundValidNodes);
        int numLegalProps = 0;
        for (Role role : roles) {
            numLegalProps += propNet.getLegalPropositions().get(role).size();
        }
        PRINT("Number of legal props: " + numLegalProps);
        return result;
    }

    // doesn't work but should recursively build ordering from start node
    // maybe possibly do it from the end node
    private List<Proposition> buildOrdering(Component currComp, Set<Proposition> seenProps)
    {
        List<Proposition> currOrdering = new ArrayList<Proposition>();
        if (currComp instanceof Proposition) {
            Proposition currProp = (Proposition)currComp;
            if (seenProps.contains(currProp)) {
                PRINT("Seen this prop already");
                return new ArrayList<Proposition>();
            } else {
                seenProps.add(currProp);
            }
            if (propNet.getInputPropositions().get(currProp.getName()) != null) { 
                //PRINT("Current proposition is an input proposition"); 
            } else if (propNet.getBasePropositions().get(currProp.getName()) != null) { 
                //PRINT("Current proposition is a base proposition"); 
            } else { 
                currOrdering.add(currProp); 
            }
            if (propsAreEqual(currProp, propNet.getTerminalProposition())) {
                PRINT("Reached final proposition");
            }
        }
        List<Component> outputComponents = new ArrayList<Component>(currComp.getOutputs());
        for (Component output : outputComponents) {
            List<Proposition> subOrdering = buildOrdering(output, seenProps);
            currOrdering.addAll(subOrdering);
        }
        return currOrdering;
    }

    private boolean propsAreEqual(Proposition p1, Proposition p2) {
        return p1.getName().toString().equals(p2.getName().toString());
    }


    /* Already implemented for you */
    @Override
    public List<Role> getRoles() {
        return roles;
    }

    /* Helper methods */

    /**
     * The Input propositions are indexed by (does ?player ?action).
     *
     * This translates a list of Moves (backed by a sentence that is simply ?action)
     * into GdlSentences that can be used to get Propositions from inputPropositions.
     * and accordingly set their values etc.  This is a naive implementation when coupled with
     * setting input values, feel free to change this for a more efficient implementation.
     *
     * @param moves
     * @return
     */
    private List<GdlSentence> toDoes(List<Move> moves)
    {
        List<GdlSentence> doeses = new ArrayList<GdlSentence>(moves.size());
        Map<Role, Integer> roleIndices = getRoleIndices();

        for (int i = 0; i < roles.size(); i++)
        {
            int index = roleIndices.get(roles.get(i));
            doeses.add(ProverQueryBuilder.toDoes(roles.get(i), moves.get(index)));
        }
        return doeses;
    }

    /**
     * Takes in a Legal Proposition and returns the appropriate corresponding Move
     * @param p
     * @return a PropNetMove
     */
    public static Move getMoveFromProposition(Proposition p) 
    {
        return new Move(p.getName().get(1));
    }

    /**
     * Helper method for parsing the value of a goal proposition
     * @param goalProposition
     * @return the integer value of the goal proposition
     */
    private int getGoalValue(Proposition goalProposition)
    {
        GdlRelation relation = (GdlRelation) goalProposition.getName();
        GdlConstant constant = (GdlConstant) relation.get(1);
        return Integer.parseInt(constant.toString());
    }

    /**
     * A Naive implementation that computes a PropNetMachineState
     * from the true BasePropositions.  This is correct but slower than more advanced implementations
     * You need not use this method!
     * @return PropNetMachineState
     */
    public MachineState getStateFromBase()
    {
        Set<GdlSentence> contents = new HashSet<GdlSentence>();
        for (Proposition p : propNet.getBasePropositions().values())
        {
            p.setValue(p.getSingleInput().getValue());
            if (p.getValue())
            {
                contents.add(p.getName());
            }

        }
        return new MachineState(contents);
    }
}