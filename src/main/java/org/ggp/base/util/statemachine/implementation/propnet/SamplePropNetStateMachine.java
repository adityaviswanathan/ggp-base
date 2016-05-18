package org.ggp.base.util.statemachine.implementation.propnet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.propnet.architecture.Component;
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

    private PropNet propNet;
    private List<Component> ordering;
    private List<Role> roles;

    private void PRINT(String str) { System.out.println(str); }
    private boolean DEBUG_FUNCTIONS = true; // flag for calling debugging functions
    private boolean DEBUG = false;
    private void LOG(String str) { if (DEBUG) System.out.println(str); }

    private Map<Component, Boolean> compConsistencyMap; // map that stores whether the last component was correct
    private Map<Proposition, Boolean> propValMap; // map that stores the most recent value of a proposition
    
    private Proposition[] props;
    private Component[] comps;

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
            ordering = getOrdering();

            compConsistencyMap = new HashMap<Component, Boolean>();
            propValMap = new HashMap<Proposition, Boolean>(); 
            props = new Proposition[propNet.getPropositions().size()];
            propNet.getPropositions().toArray(props);
            comps = new Component[propNet.getComponents().size()];
            propNet.getComponents().toArray(comps);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void verifyPropNet(PropNet pnet)
    {
        LOG("Begin verifying augmented propNet");

        int nAnds = 0;
        int nOrs = 0;
        int nNots = 0;
        int nUnsetProps = 0;
        Set<Component> components = pnet.getComponents();
        for (Component comp : components) {
            if (comp.getType() == Component.Type.AND) nAnds++;
            else if (comp.getType() == Component.Type.OR) nOrs++;
            else if (comp.getType() == Component.Type.NOT) nNots++;
            else if (!comp.isTypeSet() && comp instanceof Proposition) nUnsetProps++; 
            else if (!comp.isTypeSet() && comp instanceof Component) LOG("Component type unset");
        }

        LOG("Propnet has " + nAnds + " ands; " + nOrs + " ors; " + nNots + " nots; " + nUnsetProps + " unset propositions;");
    }

    private void clearpropnet(boolean resetCorrectness)
    {
        LOG("Begin clearing propositions");
        for (Proposition prop : props) {
            if (prop.getType() != Component.Type.CONSTANT) {
                prop.setValue(false);
            }
        }
        if (resetCorrectness) {
            for (Component comp : comps) {
                comp.setCorrectlyMarked(false);
            }
        }
        LOG("Done clearing propositions");
    }

    private boolean propmarkp(Component comp)
    {
        LOG("Calling propmarkp");
        //if (comp.isCorrectlyMarked()) return comp.getValue();
        if (comp.isCorrectlyMarked()) return comp.getMarking();

        Component.Type compType = comp.getType();
        if (compType == Component.Type.BASE_PROP) 
        { 
            return comp.getValue(); 
        }
        else if (compType == Component.Type.INPUT_PROP) 
        { 
            return comp.getValue(); 
        }
        else if (compType == Component.Type.NOT)
        {
            return !propmarkp(comp.getSingleInput());
        }
        else if (compType == Component.Type.AND) 
        { 
            Set<Component> inputs = comp.getInputs();
            for (Component input : inputs) {
                if (!propmarkp(input)) return false;
            }
            return true;
        }
        else if (compType == Component.Type.OR)
        {
            Set<Component> inputs = comp.getInputs();
            for (Component input : inputs) {
                if (propmarkp(input)) return true;
            }
            return false;   
        }
        else
        {
            if (comp.getInputs().size() != 0)
                return propmarkp(comp.getSingleInput());
            else
                return comp.getValue();
        }
    }

    private void markbases(MachineState state)
    {
        LOG("Begin marking bases");

        List<GdlSentence> currStateContents = new ArrayList<GdlSentence>(state.getContents());
        Set<Proposition> currStateProps = new HashSet<Proposition>();
        for (GdlSentence sent : currStateContents) {
            Proposition baseProp = propNet.getBasePropositions().get(sent);
            if (baseProp != null) currStateProps.add(baseProp);
        }    

        for (Proposition prop : propNet.getBasePropositions().values()) {
            if (currStateProps.contains(prop)) {
                if (!prop.getValue()) {
                    Set<Component> dependents = propNet.getDependents(prop);
                    if (dependents == null) {
                        LOG("ERROR NO DEPENDENTS (NIL)");
                    } else {
                        for (Component comp : dependents) {
                            comp.setCorrectlyMarked(false);
                        }
                    }
                }
                prop.setValue(true);
            } else {
                if (prop.getValue()) {
                    Set<Component> dependents = propNet.getDependents(prop);
                    if (dependents == null) {
                        LOG("ERROR NO DEPENDENTS (NIL)");
                    } else {
                        for (Component comp : dependents) {
                            comp.setCorrectlyMarked(false);
                        }
                    }
                }
                prop.setValue(false);
            }
        }    

        LOG("Done marking bases");
    }

    private void markactions(List<Move> moves)
    {
        LOG("Begin marking actions");
        
        List<GdlSentence> doeses = toDoes(moves);
        Set<Proposition> currInputProps = new HashSet<Proposition>();
        for (GdlSentence sent : doeses) {
            Proposition inputProp = propNet.getInputPropositions().get(sent);
            if (inputProp != null) {
                currInputProps.add(inputProp);
            } 
        }

        // clear all old inputs values but save their previous values to ensure we do not change any incorrectly
        Map<Proposition, Boolean> prevInputValues = new HashMap<Proposition, Boolean>();
        for (Proposition inputProp : propNet.getInputPropositions().values()) {
            prevInputValues.put(inputProp, inputProp.getValue());
            inputProp.setValue(false);
        }

        for (Proposition inputProp : propNet.getInputPropositions().values()) {
            if (currInputProps.contains(inputProp)) {
                if (prevInputValues.get(inputProp) == false) {
                    Set<Component> dependents = propNet.getDependents(inputProp);
                    if (dependents == null) {
                        LOG("ERROR NO DEPENDENTS (NIL)");
                    } else {
                        for (Component comp : dependents) {
                            comp.setCorrectlyMarked(false);
                        } 
                    }
                    
                }
                inputProp.setValue(true);
            }
        }

        LOG("Done marking actions");    
    }


    /**
     * Computes if the state is terminal. Should return the value
     * of the terminal proposition for the state.
     */
    @Override
    public boolean isTerminal(MachineState state) {
        LOG("Begin isTerminal");

        markbases(state);
        Set<Component> dependents = propNet.getDependents(propNet.getTerminalProposition());
        if (dependents == null) {
            LOG("ERROR NO DEPENDENTS (NIL)");
        }
        for (Component comp : dependents) {
            comp.setCorrectlyMarked(false);
        }

        LOG("End isTerminal");
        return propmarkp(propNet.getTerminalProposition());
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
        
        markbases(state);
        Set<Proposition> goalProps = propNet.getGoalPropositions().get(role);
        if (goalProps == null) {
            throw new GoalDefinitionException(state, role);            
        }
        int nTrueGoalProps = 0;
        Proposition trueGoalProp = null;

        for (Proposition prop : goalProps) {
            prop.setValue(propmarkp(prop));
            if (prop.getValue()) {
                nTrueGoalProps++;
                trueGoalProp = prop;
            }
        }

        if (trueGoalProp == null) {
            LOG("No true goal prop");
            throw new GoalDefinitionException(state, role);            
        } else if (nTrueGoalProps != 1) {
            LOG("Found " + nTrueGoalProps + " true goal props");
            throw new GoalDefinitionException(state, role);
        }

        return getGoalValue(trueGoalProp);
    }

    /**
     * Returns the initial state. The initial state can be computed
     * by only setting the truth value of the INIT proposition to true,
     * and then computing the resulting state.
     */
    @Override
    public MachineState getInitialState() {
        LOG("Begin getInitialState");
        
        clearpropnet(true);
        propNet.getInitProposition().setValue(true);

        for (Component comp : comps)
            compConsistencyMap.put(comp, (Boolean)comp.isCorrectlyMarked());

        for (Proposition prop : props)
            propValMap.put(prop, (Boolean)prop.getValue());

        LOG("End getInitialState");
        return getStateFromBase();
    }

    /**
     * Computes all possible actions for role.
     */
    @Override
    public List<Move> findActions(Role role)
            throws MoveDefinitionException {
        LOG("Begin findActions");

        Set<Proposition> legalProps = propNet.getLegalPropositions().get(role);
        List<Move> actions = new ArrayList<Move>(legalProps.size());
        for (Proposition prop : legalProps) {
            actions.add(getMoveFromProposition(prop));
        }

        LOG("End findActions");
        return actions;
    }

    /**
     * Computes the legal moves for role in state.
     */
    @Override
    public List<Move> getLegalMoves(MachineState state, Role role)
            throws MoveDefinitionException {
        LOG("Begin getLegalMoves");

        markbases(state);

        Set<Proposition> legalProps = propNet.getLegalPropositions().get(role);
        List<Move> actions = new ArrayList<Move>(legalProps.size());

        for (Proposition prop : legalProps) {
            Set<Component> dependents = propNet.getDependents(prop);
            if (dependents == null) {
                LOG("ERROR NO DEPENDENTS (NIL)");
            }
            for (Component comp : dependents) {
                comp.setCorrectlyMarked(false);
            }
            if (propmarkp(prop)) {
                actions.add(getMoveFromProposition(prop));
            }
        }

        LOG("End getLegalMoves");
        return actions;
    }

    /**
     * Computes the next state given state and the list of moves.
     */
    @Override
    public MachineState getNextState(MachineState state, List<Move> moves)
            throws TransitionDefinitionException {
        LOG("Begin getNextState");

        clearpropnet(false);
        markactions(moves);
        markbases(state);

        for (Component comp : ordering) {
            comp.setMarking(propmarkp(comp));
            comp.setCorrectlyMarked(true);

        }

        MachineState nextState = state.clone();
        Set<GdlSentence> contents = nextState.getContents();
        for (Proposition prop : propNet.getBasePropositions().values()) {
            boolean oldVal = prop.getValue();
            boolean newVal = prop.getSingleInput().getValue();
            prop.setValue(newVal);

            GdlSentence pSentence = prop.getName();
            if (oldVal && !newVal) {
                contents.remove(pSentence);
            } else if (!oldVal && newVal) {
                contents.add(pSentence);
            }
        }

        LOG("End getNextState");
        return nextState;
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
    public List<Component> getOrdering()
    {
        List<Component> order = new LinkedList<Component>();
        List<Component> components = new ArrayList<Component>(propNet.getComponents());
        List<Proposition> propositions = new ArrayList<Proposition>(propNet.getPropositions());
        Set<Component> excludedComps = new HashSet<Component>(); // bases should not be in ordering (leaving in inputs)
        Set<Component> validComps = new HashSet<Component>(); // all components that should be in ordering
        for (Component comp : components) {
            if (comp.getType() == Component.Type.BASE_PROP) excludedComps.add(comp);
            else validComps.add(comp);    
        }
        while (!validComps.isEmpty()) {
            List<Component> tempValidComps = new ArrayList<Component>(validComps);
            for (Component comp : validComps) {
                Set<Component> inputs = comp.getInputs();
                boolean compExplored = true;
                for (Component input : inputs) {
                    if (!excludedComps.contains(input)) {
                        compExplored = false;
                        break;
                    }
                }
                if (compExplored) {
                    order.add(comp); // add to topological ordering
                    excludedComps.add(comp);
                    tempValidComps.remove(comp);
                }
            }
            validComps = new HashSet<Component>(tempValidComps); 
        }
        return order;
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