package org.ggp.base.util.statemachine.implementation.propnet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.propnet.architecture.Component;
import org.ggp.base.util.propnet.architecture.PropNet;
import org.ggp.base.util.propnet.architecture.components.*;
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

    private PropNet propNet;
    private List<Role> roles;

    // private variables
    private List<Proposition> baseProps;
    private Set<Proposition> prevStateBaseProps;
    private Set<Proposition> prevStateInputProps;

    private boolean DEBUG = false;
    private void LOG(String str) { if (DEBUG) System.out.println(str); }

    private MachineState initialState = null;

    /**
     * Initializes the PropNetStateMachine. You should compute the topological
     * ordering here. Additionally you may compute the initial state here, at
     * your discretion.
     */
    @Override
    public void initialize(List<Gdl> description) {
        try {
            // initialize propnet and role list
            propNet = OptimizingPropNetFactory.create(description);
            roles = propNet.getRoles();

            baseProps = new ArrayList<Proposition>(propNet.getBasePropositions().values());

            prevStateBaseProps = new HashSet<Proposition>(); // base props true in the previous state
            prevStateInputProps = new HashSet<Proposition>(); // input props set true in the previous joint move

            List<Component> notComps = new ArrayList<Component>();
            Set<Proposition> basePropsSet = new HashSet<Proposition>(propNet.getBasePropositions().values());
            Set<Proposition> inputPropsSet = new HashSet<Proposition>(propNet.getInputPropositions().values());
            List<Component> allCompsList = new ArrayList<Component>(propNet.getComponents());
            for (Component comp : allCompsList) {
                comp.initComponent();
                if (comp instanceof And) { }
                else if (comp instanceof Or) { }
                else if (comp instanceof Not) 
                { 
                    notComps.add(comp); 
                }
                else if (comp instanceof Proposition) 
                {
                    Proposition prop = (Proposition)comp;
                    if (inputPropsSet.contains(prop)) {
                        prop.setIsInputProp();
                    } else if (basePropsSet.contains(prop)) {
                        prop.setIsBaseProp();
                    }
                }
            }

            for (Component not : notComps) {
                not.forwardPropagate(true);    
            }

            setInitialState();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setInitialState()
    {
        Proposition initProp = propNet.getInitProposition();
        initProp.forwardPropagate(true);
        // begin populating initial state contents
        Set<GdlSentence> initialStateContents = new HashSet<GdlSentence>();
        for (Proposition prop : baseProps) {
            // check for the transition value of the current base input
            if (prop.getSingleInput().getValue()) {
                initialStateContents.add(prop.getName());
            }
        }
        // create initial state from current true base props
        initialState = new MachineState(initialStateContents);
        initProp.forwardPropagate(false);   
    }

    private void markBasePropsForState(MachineState state)
    {
        // get propositions from state
        List<GdlSentence> currStateContents = new ArrayList<GdlSentence>(state.getContents());
        List<Proposition> currStateProps = new ArrayList<Proposition>();
        for (GdlSentence sent : currStateContents) {
            Proposition baseProp = propNet.getBasePropositions().get(sent);
            if (baseProp != null) {
                currStateProps.add(baseProp);
            } else {
                LOG("Error finding base prop");
            }
        }
        // old base propositions that should not be true any more
        Set<Proposition> outdatedBaseProps = new HashSet<Proposition>(prevStateBaseProps);
        outdatedBaseProps.removeAll(currStateProps);
        for (Proposition prop : outdatedBaseProps) {
            prop.beginForwardPropagation(false);
        }
        // mark appropriate base props
        Set<Proposition> currBases = new HashSet<Proposition>(currStateProps);
        currBases.removeAll(prevStateBaseProps);
        for (Proposition prop : currBases) {
            prop.beginForwardPropagation(true);
        }
        // store our currently true base props
        prevStateBaseProps = new HashSet<Proposition>(currStateProps);
    }

    private void markInputPropsForMoves(List<Move> moves)
    {
        // get props for a move
        List<GdlSentence> doeses = toDoes(moves);
        List<Proposition> currInputProps = new ArrayList<Proposition>();
        for (GdlSentence sent : doeses) {
            Proposition inputProp = propNet.getInputPropositions().get(sent);
            if (inputProp != null) {
                currInputProps.add(inputProp);
            } else {
                LOG("Error finding input prop");
            }
        }
        // old input propositions that should not be true any more
        Set<Proposition> outdatedInputProps = new HashSet<Proposition>(prevStateInputProps);
        outdatedInputProps.removeAll(currInputProps);
        for (Proposition prop : outdatedInputProps) {
            prop.beginForwardPropagation(false);
        }
        // forward propogate the appropriate value from all input propositions
        Set<Proposition> currInputs = new HashSet<Proposition>(currInputProps);
        currInputs.removeAll(prevStateInputProps);
        for (Proposition prop : currInputs) {
            prop.beginForwardPropagation(true);
        }
        // store our currently true input props
        prevStateInputProps = new HashSet<Proposition>(currInputProps);
    }


    /**
     * Computes if the state is terminal. Should return the value
     * of the terminal proposition for the state.
     */
    @Override
    public boolean isTerminal(MachineState state) {
        markBasePropsForState(state);
        return propNet.getTerminalProposition().getValue();
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
        markBasePropsForState(state);
        List<Proposition> goalProps = new ArrayList<Proposition>(propNet.getGoalPropositions().get(role));
        if (goalProps == null) {
            throw new GoalDefinitionException(state, role);
        }
        int numTrueGoalProps = 0;
        Proposition trueGoalProp = null;
        for (Proposition prop : goalProps) {
            if (prop.getValue()) {
                numTrueGoalProps++;
                trueGoalProp = prop;
            }
        }
        if (trueGoalProp != null && numTrueGoalProps == 1) {
            return getGoalValue(trueGoalProp);
        }
        throw new GoalDefinitionException(state, role);
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

        Set<Proposition> legalProps = propNet.getLegalPropositions().get(role);
        List<Move> actions = new ArrayList<Move>(legalProps.size());
        for (Proposition prop : legalProps) {
            actions.add(getMoveFromProposition(prop));
        }
        return actions;
    }

    /**
     * Computes the legal moves for role in state.
     */
    @Override
    public List<Move> getLegalMoves(MachineState state, Role role)
            throws MoveDefinitionException {
        
        // first mark base props for the state
        markBasePropsForState(state);

        // return list of valid moves for role in state
        Set<Proposition> legalProps = propNet.getLegalPropositions().get(role);
        List<Move> legalActions = new ArrayList<Move>(legalProps.size());
        for (Proposition prop : legalProps) {
            if (prop.getValue()) {
                legalActions.add(getMoveFromProposition(prop));
            }
        }
        return legalActions;
    }

    /**
     * Computes the next state given state and the list of moves.
     */
    @Override
    public MachineState getNextState(MachineState state, List<Move> moves)
            throws TransitionDefinitionException {

        // first mark base and input props for the state and moves
        markBasePropsForState(state);
        markInputPropsForMoves(moves);
        // get state from current bases
        // begin populating initial state contents
        Set<GdlSentence> stateContents = new HashSet<GdlSentence>();
        for (Proposition prop : baseProps) {
            // check for the transition value of the current base input
            if (prop.getSingleInput().getValue()) {
                stateContents.add(prop.getName());
            }
        }
        // create state from current true base props
        MachineState nextState = new MachineState(stateContents);
        return nextState;
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
}