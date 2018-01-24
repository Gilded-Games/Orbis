package orbis_core.data.framework;

import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.data.framework.FrameworkAlgorithm;
import com.gildedgames.orbis.api.data.framework.FrameworkData;
import com.gildedgames.orbis.api.data.framework.Graph;
import com.gildedgames.orbis.api.data.framework.generation.fdgd_algorithms.ComputedParamFac;
import com.gildedgames.orbis.api.data.framework.generation.FDGDEdge;
import com.gildedgames.orbis.api.data.framework.generation.FDGDNode;
import com.gildedgames.orbis.common.OrbisCore;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import java.security.Key;
import java.util.Random;

/**
 * Visualises the Framework and pathway algorithms
 * for debugging purposes. Hold down space to run, 
 * use the arrow keys to look around and use e and w
 * to zoom in and out.
 * @author Emile
 *
 */
public class FrameworkDebug
{
	private FrameworkData toDebug = FrameworkDataset.randomFramework(new Random());

	private FrameworkAlgorithm algorithm;

	private static double left = -200, right = 200, bottom = -200, top = 200;

	private boolean showYellow = false, showPurple = true;

	public FrameworkDebug()
	{
		this.algorithm = new FrameworkAlgorithm(this.toDebug, null);
	}

	public static void main(String[] args)
	{
		try
		{
			Bootstrap.register();

			Display.setDisplayMode(new DisplayMode(1000, 800));
			Display.setTitle("Framework Debug");
			Display.create();
			final FrameworkDebug screen = new FrameworkDebug();
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glOrtho(left, right, bottom, top, 1, -1);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			while (!Display.isCloseRequested())
			{
				screen.update();
				Display.update();
				Display.sync(120);
			}
		}
		catch (final LWJGLException e)
		{
			e.printStackTrace();
			Display.destroy();
			System.exit(1);
		}
	}

	public void update()
	{
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// set the color of the quad (R,G,B,A)
		GL11.glColor3f(0.5f, 0.5f, 1.0f);

		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
			this.algorithm.step();
		if (Keyboard.isKeyDown(Keyboard.KEY_Z) && this.algorithm.getPhase() == FrameworkAlgorithm.Phase.FDGD)
			this.algorithm.step();
		while (Keyboard.next())
		{
			if (Keyboard.getEventKey() == Keyboard.KEY_Y)
			{
				if (Keyboard.getEventKeyState())
				{
					OrbisCore.LOGGER.info("Showing yellow");
					this.showYellow = !this.showYellow;
				}
			}
			else if (Keyboard.getEventKey() == Keyboard.KEY_F
					&& Keyboard.getEventKeyState())
			{
				OrbisCore.LOGGER.info("Starting computation");
				long startTime = System.currentTimeMillis();
				this.algorithm.computeFully();
				long endTime = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				OrbisCore.LOGGER.info("Full computation took " + (totalTime / 1000d) + "seconds");
			}
		}

		double dx = right - left;
		double dy = top - bottom;

		double ddx = 0.01 * dx;
		double ddy = 0.01 * dy;

		if (Keyboard.isKeyDown(Keyboard.KEY_UP))
		{
			bottom += ddy;
			top += ddy;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
		{
			bottom -= ddy;
			top -= ddy;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
		{
			right += ddx;
			left += ddx;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
		{
			right -= ddx;
			left -= ddx;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_E))
		{
			top -= ddy;
			bottom += ddy;
			right -= ddx;
			left += ddx;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_W))
		{
			top += ddy;
			bottom -= ddy;
			right += ddx;
			left -= ddx;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_R))
		{
			this.toDebug = FrameworkDataset.randomFramework(new Random());
			this.algorithm = new FrameworkAlgorithm(this.toDebug, null);
		}

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(left, right, bottom, top, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		final Graph<FDGDNode, FDGDEdge> graph = this.algorithm.getFDGDDebug();
		if (graph == null)
		{
			return;
		}
		graph.vertexSet().forEach(node -> glDrawRegion(node, 0.2f, 0.2f, 0.5f));
		graph.vertexSet().forEach(node -> glDrawRegion(node.getRegionForBlueprint(), 0.5f, 0.5f, 1.0f));
//		else
//		{
//			final List<FrameworkFragment> fragments = this.algorithm.getFragments();
//			for (final FrameworkFragment fragment : fragments)
//			{
//				glDrawRegion(fragment, 0.5f, 0.5f, 1.0f);
//			}
//
//			for (final PathwayNode node : this.algorithm.getPathfindingDebug())
//			{
//				glDrawRegion(node, 1.0f, 0.5f, 0.5f);
//				glDrawRegion(new Region(new BlockPos(node.endConnection.getX() - 1, 0, node.endConnection.getZ() - 1), new BlockPos(node.endConnection.getX() + 1, 0, node.endConnection.getZ() + 1)), 0.5f, 1.0f, 0.5f);
//			}
//		}
		if (showYellow)
			this.toDebug.getGraph().edgeSet().forEach(e -> glDrawEdge(this.algorithm._nodeMap.get(e.node1()), this.algorithm._nodeMap.get(e.node2()), false));
		if (showPurple)
			graph.edgeSet().forEach(e -> glDrawEdge(e, true));
		glDrawRegion(new Region(new BlockPos(-2, -2, -2), new BlockPos(2, 2, 2)), 1.0f, 1.0f, 0.5f);
	}

	public static void glDrawEdge(FDGDEdge edge, boolean c)
	{
		glDrawEdge(edge.node1(), edge.node2(), c);
		glDrawEntrance(edge);
	}

	public static void glDrawEdge(FDGDNode n1, FDGDNode n2, boolean c)
	{
		GL11.glBegin(GL11.GL_LINES);
		if (c)
			GL11.glColor3f(1.0f, 0.2f, 1.0f);
		else
			GL11.glColor3f(1.0f, 1.0f, 0.2f);
		float x1 = n1.getX(), z1 = n1.getZ();
		float x2 = n2.getX(), z2 = n2.getZ();
		//		GL11.glVertex2f(edge.entrance1X(), edge.entrance1Z());
		//		GL11.glVertex2f(edge.entrance2X(), edge.entrance2Z());
		GL11.glVertex2f(x1, z1);
		GL11.glVertex2f(x2, z2);
		GL11.glEnd();
		glDrawRegion(new Region(new BlockPos(x1 - 1, 0, z1 - 1), new BlockPos(x1 + 1, 0, z1 + 1)), 0.5f, 1.0f, 0.5f);
		glDrawRegion(new Region(new BlockPos(x2 - 1, 0, z2 - 1), new BlockPos(x2 + 1, 0, z2 + 1)), 0.5f, 1.0f, 0.5f);
	}

	public static void glDrawEntrance(FDGDEdge edge)
	{
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor3f(0.2f, 1.0f, 1.0f);
		float x1 = edge.entrance1X(), z1 = edge.entrance1Z();
		float x2 = edge.entrance2X(), z2 = edge.entrance2Z();
		GL11.glVertex2f(x1, z1);
		GL11.glVertex2f(x2, z2);
//		GL11.glVertex2f(x1, z1);
//		GL11.glVertex2f(x2, z2);
		GL11.glEnd();
//		glDrawRegion(new Region(new BlockPos(x1 - 1, 0, z1 - 1), new BlockPos(x1 + 1, 0, z1 + 1)), 0.5f, 1.0f, 0.5f);
//		glDrawRegion(new Region(new BlockPos(x2 - 1, 0, z2 - 1), new BlockPos(x2 + 1, 0, z2 + 1)), 0.5f, 1.0f, 0.5f);
	}

	public static void glDrawRegion(IRegion region, float r, float g, float b)
	{
		GL11.glColor3f(r, g, b);
		final BlockPos min = region.getMin();
		final BlockPos max = region.getMax();
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(min.getX(), min.getZ());
		GL11.glVertex2f(max.getX(), min.getZ());
		GL11.glVertex2f(max.getX(), max.getZ());
		GL11.glVertex2f(min.getX(), max.getZ());
		GL11.glEnd();
	}
}
