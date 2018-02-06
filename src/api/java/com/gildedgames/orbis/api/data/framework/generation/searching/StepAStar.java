package com.gildedgames.orbis.api.data.framework.generation.searching;

import com.gildedgames.orbis.api.OrbisAPI;

import java.util.HashSet;
import java.util.PriorityQueue;

public class StepAStar<T extends Node>
{
	private final ISearchProblem<T> problem;

	private final PriorityQueue<T> queue = new PriorityQueue<>();

	private final HashSet<T> visitedStates = new HashSet<>();

	private final double hWeight;

	private boolean terminated;

	private T currentState;

	private int statesExpanded = 0;

	public StepAStar(ISearchProblem<T> problem, double hWeight)
	{
		this.queue.add(problem.start());
		this.problem = problem;
		this.hWeight = hWeight;
	}

	public void step()
	{
		if (this.terminated)
		{
			return;
		}
		if (this.queue.isEmpty() || this.statesExpanded > 5000) // NOTE: This is temporary
		{
			this.currentState = null;
			this.terminated = true;
			return;
		}
		this.currentState = this.queue.poll();
		if (this.problem.isGoal(this.currentState))
		{
			this.terminated = true;
			return;
		}

		if (this.problem.shouldTerminate(this.currentState))
		{
			this.terminated = true;
			//this.currentState = null;
			return;
		}
		if (this.problem.contains(this.visitedStates, this.currentState))
		{
			this.step();
			return;
		}
//		OrbisAPI.LOGGER.info(this.currentState.getG());
//		OrbisAPI.LOGGER.info(this.currentState.getH());
//		OrbisAPI.LOGGER.info(this.currentState.getF());

		this.visitedStates.add(this.currentState);
		this.statesExpanded += 1;

		for (T state : this.problem.successors(this.currentState))
		{
			state.setG(this.problem.costBetween(this.currentState, state) + this.currentState.getG());
			state.setH(this.hWeight * this.problem.heuristic(state));
			this.queue.add(state);
		}
	}

	public boolean isTerminated()
	{
		return this.terminated;
	}

	public T currentState()
	{
		if(this.currentState == null)
			OrbisAPI.LOGGER.info("?");
		return this.currentState;
	}
}
