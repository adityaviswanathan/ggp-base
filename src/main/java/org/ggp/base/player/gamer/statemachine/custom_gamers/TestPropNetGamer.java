package org.ggp.base.player.gamer.statemachine.sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.lang.Math;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.implementation.propnet.*;
import org.ggp.base.util.statemachine.implementation.prover.*;
import org.ggp.base.util.gdl.grammar.*;
import org.ggp.base.util.statemachine.verifier.*;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.game.Game;

/**
 * Basic version of legal gamer that tests our propnet statemachine
 */
public final class TestPropNetGamer extends SampleGamer
{
    private StateMachine sharedStateMachine = null;

    // just initialize state machine
    @Override
    public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
    {
        sharedStateMachine = getStateMachine();

        Match currMatch = getMatch();
        if (currMatch != null) {
            Game currGame = currMatch.getGame();
            if (currGame != null) {
                List<Gdl> description = currGame.getRules();
                ProverStateMachine m = new ProverStateMachine();
                m.initialize(description);
                StateMachineVerifier.checkMachineConsistency(m, sharedStateMachine, 10000);
            }
        }

        //List<Gdl> description = 
        //ProverStateMachine m = new ProverStateMachine();
        //m.initialize(description);


        System.out.println("Initial state metagame: " + sharedStateMachine.findInits().toString());
    }

    // choose randome move from state machine
    @Override
    public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
    {
        long start = System.currentTimeMillis();

        List<Move> moves = getStateMachine().findLegals(getRole(), getCurrentState());

        System.out.println("Moves: " + moves);

        // Legal gamer always selects the 0-indexed move from the move list
        Move selection = (moves.get(0));
        
        long stop = System.currentTimeMillis();

        notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
        return selection;
    }
}