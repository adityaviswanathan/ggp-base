package org.ggp.base.player.gamer.statemachine.sample;

import org.ggp.base.apps.player.detail.DetailPanel;
import org.ggp.base.apps.player.detail.SimpleDetailPanel;
import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.implementation.propnet.SamplePropNetStateMachine;

//our imports
import java.util.Collections;
import java.util.List;
import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.Move;

import org.ggp.base.util.propnet.factory.OptimizingPropNetFactory;

public final class PropNetGamer extends StateMachineGamer
{

    private SamplePropNetStateMachine sharedStateMachine = null;

    @Override
    public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
    {
        // initialize shared PropNetStateMachine
        sharedStateMachine = (SamplePropNetStateMachine)getStateMachine();
    }

    /**
     * Selects the default move as the first legal move, and then waits
     * while the Human sets their move. This is done via the HumanDetailPanel.
     */
    @Override
    public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
    {
        long start = System.currentTimeMillis();

        List<Move> moves = getStateMachine().findLegals(getRole(), getCurrentState());

        Move selection = moves.get(0);

        long stop = System.currentTimeMillis();

        notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
        return selection;
    }

    /** This will currently return "SampleGamer"
     * If you are working on : public abstract class MyGamer extends SampleGamer
     * Then this function would return "MyGamer"
     */
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    // This is the default State Machine
    @Override
    public StateMachine getInitialStateMachine() {
        return new SamplePropNetStateMachine();
    }

    // This is the default Sample Panel
    @Override
    public DetailPanel getDetailPanel() {
        return new SimpleDetailPanel();
    }



    @Override
    public void stateMachineStop() {
        // Sample gamers do no special cleanup when the match ends normally.
    }

    @Override
    public void stateMachineAbort() {
        // Sample gamers do no special cleanup when the match ends abruptly.
    }

    @Override
    public void preview(Game g, long timeout) throws GamePreviewException {
        // Sample gamers do no game previewing.
    }
}