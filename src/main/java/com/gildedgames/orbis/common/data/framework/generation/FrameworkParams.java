package com.gildedgames.orbis.common.data.framework.generation;

public class FrameworkParams
{

	public float repulsion;

	public float repulsion()
	{
		return this.repulsion;
	}

	public float stiffness;

	public float stiffness()
	{
		return this.stiffness;
	}

	public float naturalLength;

	public float naturalLength()
	{
		return this.naturalLength;
	}

	public float collisionEscape;

	public float collisionEscape()
	{
		return this.collisionEscape;
	}

	public int nodeDistance;

	public int nodeDistance()
	{
		return this.nodeDistance;
	}

	public float c;

	public float C()
	{
		return this.c;
	}

	public int fdgdMaxIterations;

	public int fdgdMaxIterations()
	{
		return this.fdgdMaxIterations;
	}

	public int iterationsToEscape;

	public int iterationsToEscape()
	{
		return this.iterationsToEscape;
	}

	public float toEscapeEquilibrium;

	public float toEscapeEquilibrium()
	{
		return this.toEscapeEquilibrium;
	}

	public float acceptEquilibrium;

	public float acceptEquilibrium()
	{
		return this.acceptEquilibrium;
	}

	public float acceptEquilibriumEsc;

	public float acceptEquilibriumEsc()
	{
		return this.acceptEquilibriumEsc;
	}

	public float graphGrowth;

	public float graphGrowth()
	{
		return this.graphGrowth;
	}

	public float heuristcWeight;

	public float heuristicWeight()
	{
		return this.heuristcWeight;
	}

	public int pathwaysBoundingBox;

	public int pathwaysBoundingBox()
	{
		return pathwaysBoundingBox;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Repulsion ");
		builder.append(this.repulsion);
		builder.append(" Stiffness ");
		builder.append(this.stiffness);
		builder.append(" Natural Length ");
		builder.append(this.naturalLength);
		builder.append(" Collision Escape ");
		builder.append(this.collisionEscape);
		builder.append(System.lineSeparator());
		builder.append("Node Distance ");
		builder.append(this.nodeDistance);
		builder.append(" C ");
		builder.append(this.c);
		builder.append(" FDGD Max Iterations ");
		builder.append(this.fdgdMaxIterations);
		builder.append(" Iterations to Escape ");
		builder.append(this.iterationsToEscape);
		builder.append(System.lineSeparator());
		builder.append("To escape Equilibrium ");
		builder.append(this.toEscapeEquilibrium);
		builder.append(" Accept Equilibruim ");
		builder.append(this.acceptEquilibrium);
		builder.append(" Accept Equilibrium Escape ");
		builder.append(this.acceptEquilibriumEsc);
		builder.append(" Graph Growth");
		builder.append(this.graphGrowth);
		builder.append(System.lineSeparator());
		builder.append("Heuristic Weight ");
		builder.append(this.heuristcWeight);
		builder.append(" Pathways Bounding Box");
		builder.append(this.pathwaysBoundingBox);
		return builder.toString();
	}
}
