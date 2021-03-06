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

    private void PRINT(String str) { System.out.println(str); }
    private boolean DEBUG = true;
    private void LOG(String str) { if (DEBUG) System.out.println(str); }

    private PropNet propNet;
    private List<Role> roles;
    
    private Proposition[] props; // all propositions
    private Component[] comps; // all components
    private Proposition[] bases; // all base props
    private Proposition[] inputs; // all input props

    /**
     * Initializes the PropNetStateMachine. You should compute the topological
     * ordering here. Additionally you may compute the initial state here, at
     * your discretion.
     */
    @Override
    public void initialize(List<Gdl> description) 
    {
        try {
            // set propnet and roles
            propNet = OptimizingPropNetFactory.create(description);
            roles = propNet.getRoles();
            // set prop[] array
            props = new Proposition[propNet.getPropositions().size()];
            propNet.getPropositions().toArray(props);
            // set comp[] array
            comps = new Component[propNet.getComponents().size()];
            propNet.getComponents().toArray(comps);
            // set bases[] array
            bases = new Proposition[propNet.getBasePropositions().values().size()];
            propNet.getBasePropositions().values().toArray(bases);
            // set inputs[] array
            inputs = new Proposition[propNet.getInputPropositions().values().size()];
            propNet.getInputPropositions().values().toArray(inputs);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }	

/* ----------------------| Public Functions | --------------------- */

    /**
     * Computes if the state is terminal. Should return the value
     * of the terminal proposition for the state.
     *
     * Wrapper function for propterminalp
     */
    @Override
    public boolean isTerminal(MachineState state) 
    {
    	return propterminalp(state);
    }

    /**
     * Computes the goal for a role in the current state.
     * Should return the value of the goal proposition that
     * is true for that role. If there is not exactly one goal
     * proposition true for that role, then you should throw a
     * GoalDefinitionException because the goal is ill-defined.
     *
     * propreward(role, state) wrapper
     */
    @Override
    public int getGoal(MachineState state, Role role) throws GoalDefinitionException 
    {
    	return propreward(role, state);    
    }

    /**
     * Returns initial state using default state from base function
	 * 	
     * TODO: Test if we need to clear propnet
     */
    @Override
    public MachineState getInitialState() 
    {
    	propNet.getInitProposition().setValue(true);
    	MachineState initialState = getStateFromBase();
    	propNet.getInitProposition().setValue(false);
        return initialState;
    }

    /**
     * Computes all possible actions for role.
     *
     * propactions(role) wrapper
     */
    @Override
    public List<Move> findActions(Role role) throws MoveDefinitionException 
    {
        return propactions(role);
    }

    /**
     * Computes the legal moves for role in state.
	 *
     * proplegals(role, state) wrapper
     */
    @Override
    public List<Move> getLegalMoves(MachineState state, Role role)  throws MoveDefinitionException 
    {
        return proplegals(role, state);
    }

    /**
     * Computes the next state given state and the list of moves.
     *
     * Wrapper function propnext(state, moves)
     */
    @Override
    public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException 
    {   
        return propnext(moves, state);  
    }

    /* Already implemented for you */
    @Override
    public List<Role> getRoles() 
    {
        return roles;
    }

/* ----------------------| Factoring Functions | --------------------- */


/* ----------------------| Functions from Ch 10 | --------------------------  */

    /* markbases(state) */
    private void markbases(MachineState state)
    {
    	clearpropnet();
        Set<Proposition> stateProps = getStateBaseProps(state);
        for (Proposition prop : stateProps) {
        	prop.setValue(true);
        }
    }

    /* markactions(moves) */
    private void markactions(List<Move> moves)
    {
        Set<Proposition> inputProps = getInputsForMoves(moves);
        for (Proposition prop : inputs) {
        	if (inputProps.contains(prop)) prop.setValue(true);
        	else prop.setValue(false);
        }
    }

    /* clearpropnet() */
    private void clearpropnet()
    {
        for (Proposition prop : bases) {
            prop.setValue(false);
        }
    }

    /* propmarkp(comp) */
    private boolean propmarkp(Component comp)
    {   
        Component.CmpType type = comp.getType();
        // handle components
        if (type == Component.CmpType.NOT)             		return propmarknegation(comp);
        else if (type == Component.CmpType.AND)             return propmarkconjunction(comp);
        else if (type == Component.CmpType.OR)              return propmarkdisjunction(comp);
        // immediately return the value of base, input, and constants
        else if (type == Component.CmpType.BASE_PROP)       return comp.getValue();
        else if (type == Component.CmpType.INPUT_PROP)      return comp.getValue();
        else if (type == Component.CmpType.CONSTANT)        return comp.getValue();
        else if (type == Component.CmpType.INIT_PROP)       return comp.getValue();
        // return propmark() for the view, legal, and goal props' single inputs
        else if (type == Component.CmpType.VIEW_PROP)       return propmarkp(comp.getSingleInput());
        else if (type == Component.CmpType.LEGAL_PROP)      return propmarkp(comp.getSingleInput());
        else if (type == Component.CmpType.GOAL_PROP)       return propmarkp(comp.getSingleInput());
        else if (type == Component.CmpType.TERMINAL_PROP)   return propmarkp(comp.getSingleInput());
        // technically we should never get to a transition within propmarkp
        else if (type == Component.CmpType.TRANSITION) 		LOG("Fuck what to do with transitions?");
    	// default value
        return false;
    }
    
    /* propmarknegation(comp) */
    private boolean propmarknegation(Component comp) 
    { 
    	return !propmarkp(comp.getSingleInput()); 
    }

    /* propmarkconjunction(comp) */
    private boolean propmarkconjunction(Component comp)
    {
        for (Component input : comp.getInputs()) {
            if (!propmarkp(input)) return false;
        }
        return true;
    }

    /* propmarkdisjunction(comp) */
    private boolean propmarkdisjunction(Component comp)
    {
        for (Component input : comp.getInputs()) {
            if (propmarkp(input)) return true;
        }
        return false;
    }

    /* proplegals(role, state) 
     *
     * returns list of legal moves for a role in a state
	 * called in getLegalMoves(MachineState state, Role role)
     */
    private List<Move> proplegals(Role role, MachineState state) throws MoveDefinitionException
    {
        markbases(state);
        Set<Proposition> legals = propNet.getLegalPropositions().get(role);
        List<Move> actions = new ArrayList<Move>(legals.size());
        for (Proposition legal : legals) {
        	if (propmarkp(legal)) actions.add(getMoveFromProposition(legal));
        } 
        return actions;
    }

    private List<Move> propactions(Role role) throws MoveDefinitionException
    {
        List<Move> actions = new ArrayList<Move>(inputs.length);
        Map<Proposition, Proposition> legalInputMap = propNet.getLegalInputMap();
        for (Proposition input : inputs) {	
        	actions.add(getMoveFromProposition(input));
        }
        return actions;
    }

    /* propnext(move, state) 
     *
	 * returns set of Gdl sentences which can be used to initialize a state
	 * called in getNextState(MachineState state, List<Move> moves)
     */
    private MachineState propnext(List<Move> moves, MachineState state) throws TransitionDefinitionException
    {
        markactions(moves);
        markbases(state);
       	Map<GdlSentence, Proposition> basePropMap = propNet.getBasePropositions();
        Set<GdlSentence> nextStateContents = new HashSet<GdlSentence>();
        for (GdlSentence s : basePropMap.keySet()) {
        	Proposition currProp = basePropMap.get(s);
        	if (propmarkp(currProp.getSingleInput().getSingleInput())) 
        		nextStateContents.add(s);
        } 
        return new MachineState(nextStateContents);
    }

    /* propreward (role,state,propnet) 
     *
     * Gets the value of the true goal prop for the role in state
     * If no value found, throw a GoalDefinitonException
	 * called in getGoal(MachineState state, Role role)
     */
    private int propreward(Role role, MachineState state) throws GoalDefinitionException
    {
        markbases(state);
        Set<Proposition> rewards = propNet.getGoalPropositions().get(role);
        for (Proposition reward : rewards) {
        	if (propmarkp(reward)) return getGoalValue(reward);
        }
        throw new GoalDefinitionException(state, role);
    }

    /* propterminalp(state,propnet)
     *
	 * called in isTerminal(MachineState state)
     */
    private boolean propterminalp(MachineState state)
    {  	
    	markbases(state);
    	return propmarkp(propNet.getTerminalProposition());
    }

/* ----------------------| Helper Functions | --------------------- */

    /**
     * Returns set of input propositions for a list of moves
     */
    private Set<Proposition> getInputsForMoves(List<Move> moves)
    {
        List<GdlSentence> doeses = toDoes(moves);
        Set<Proposition> inputProps = new HashSet<Proposition>(doeses.size());
        for (GdlSentence s : doeses) {
            inputProps.add(propNet.getInputPropositions().get(s));
        }
        return inputProps;
    }

    /**
     * Returns set of base propositions for a Machine state
     */
    private Set<Proposition> getStateBaseProps(MachineState state)
    {
        Set<GdlSentence> contents = state.getContents();
        Set<Proposition> stateProps = new HashSet<Proposition>(contents.size());
        for (GdlSentence s : contents) {
            stateProps.add(propNet.getBasePropositions().get(s));
        }
        return stateProps;
    }

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
        for (Proposition p : bases)
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