package orbis_core.data.framework;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.core.world_objects.BlueprintRegion;
import com.gildedgames.orbis_api.data.framework.FrameworkAlgorithm;
import com.gildedgames.orbis_api.data.framework.FrameworkData;
import com.gildedgames.orbis_api.data.framework.Graph;
import com.gildedgames.orbis_api.data.framework.generation.FDGDEdge;
import com.gildedgames.orbis_api.data.framework.generation.FDGDNode;
import com.gildedgames.orbis_api.data.framework.generation.FailedToGenerateException;
import com.gildedgames.orbis_api.data.framework.generation.searching.PathwayNode;
import com.gildedgames.orbis_api.data.pathway.Entrance;
import com.gildedgames.orbis_api.data.region.IRegion;
import com.gildedgames.orbis_api.data.region.Region;
import com.gildedgames.orbis_api.util.RegionHelp;
import com.gildedgames.orbis_api.util.RotationHelp;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.List;
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
	private static double left = -200, right = 200, bottom = -200, top = 200;

	private FrameworkData toDebug = FrameworkDataset.randomFramework(new Random());

	private FrameworkAlgorithm algorithm;

	private boolean showYellow = false, showPurple = true, showEntrances = true;

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
				Display.sync(60);
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

		// setUsedData the color of the quad (R,G,B,A)
		GL11.glColor3f(0.5f, 0.5f, 1.0f);

		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
		{
			this.algorithm.step();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_Z) && this.algorithm.getPhase() == FrameworkAlgorithm.Phase.FDGD)
		{
			this.algorithm.step();
		}
		while (Keyboard.next())
		{
			if (Keyboard.getEventKey() == Keyboard.KEY_Y)
			{
				if (Keyboard.getEventKeyState())
				{
					this.showYellow = !this.showYellow;
				}
			}
			else if (Keyboard.getEventKey() == Keyboard.KEY_F
					&& Keyboard.getEventKeyState())
			{
				try
				{
					OrbisCore.LOGGER.info("Starting computation");
					long startTime = System.currentTimeMillis();
					this.algorithm.computeFully();
					long endTime = System.currentTimeMillis();
					long totalTime = endTime - startTime;
					OrbisCore.LOGGER.info("Full computation took " + (totalTime / 1000d) + "seconds");
				}
				catch (FailedToGenerateException e)
				{
					e.printStackTrace();
				}
			}
			else if (Keyboard.getEventKey() == Keyboard.KEY_P
					&& Keyboard.getEventKeyState())
			{
				this.showPurple = !this.showPurple;
			}
			else if (Keyboard.getEventKey() == Keyboard.KEY_S
					&& Keyboard.getEventKeyState())
			{
				this.algorithm.step();
			}
			else if (Keyboard.getEventKey() == Keyboard.KEY_N
					&& Keyboard.getEventKeyState())
			{
				//				this.algorithm.pat
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

		GL11.glPushMatrix();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(left, right, bottom, top, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		final Graph<FDGDNode, FDGDEdge> graph = this.algorithm.getFDGDDebug();
		if (graph == null)
		{
			return;
		}
		graph.vertexSet().forEach(node -> this.glDrawRegion(node, 0.2f, 0.2f, 0.5f));
		graph.vertexSet().forEach(this::glDrawNode);
		if (this.algorithm.getPhase() == FrameworkAlgorithm.Phase.PATHWAYS)
		{
			final List<BlueprintRegion> fragments = this.algorithm.getFragments();
			for (final BlueprintRegion fragment : fragments)
			{
				this.glDrawRegion(fragment, 0.5f, 0.5f, 1.0f);

				for (Entrance e : RotationHelp.getEntrances(fragment.getData(), fragment.getRotation(), RegionHelp.getCenter(fragment)))
				{
					float x = e.getBounds().getMin().getX(), z = e.getBounds().getMin().getZ();
					float maxX = e.getBounds().getMax().getX(), maxZ = e.getBounds().getMax().getZ();

					this.glDrawRegion(e.getBounds(), 1.0f, 0.5f, 0.5f);
				}
			}

			for (final PathwayNode node : this.algorithm.getPathfindingDebug())
			{
				this.glDrawRegion(node, 1.0f, 0.5f, 0.5f);
			}
		}
		if (this.showYellow)
		{
			this.toDebug.getGraph().edgeSet()
					.forEach(e -> this.glDrawEdge(this.algorithm._nodeMap.get(e.node1()), this.algorithm._nodeMap.get(e.node2()), false));
		}
		graph.edgeSet().forEach(e -> this.glDrawEdge(e, true));
		this.glDrawRegion(new Region(new BlockPos(-2, -2, -2), new BlockPos(2, 2, 2)), 1.0f, 1.0f, 0.5f);
		GL11.glPopMatrix();

		String text = "ABCD";
		int s = 256; //Take whatever size suits you.
		BufferedImage b = new BufferedImage(s, s, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = b.createGraphics();
		g.drawString(text, 0, 0);

		int co = b.getColorModel().getNumComponents();

		byte[] data = new byte[co * s * s];
		b.getRaster().getDataElements(0, 0, s, s, data);

		ByteBuffer pixels = BufferUtils.createByteBuffer(data.length);
		pixels.put(data);
		pixels.rewind();
		//		GL11.glTexImage2D(pixels);
		//		g.drawImage(b, );
	}

	public void glDrawEdge(FDGDEdge edge, boolean c)
	{
		if (this.showPurple)
		{
			this.glDrawEdge(edge.node1(), edge.node2(), c);
		}
		this.glDrawEntrance(edge);
	}

	public void glDrawEdge(FDGDNode n1, FDGDNode n2, boolean c)
	{
		GL11.glBegin(GL11.GL_LINES);
		if (c)
		{
			GL11.glColor3f(1.0f, 0.2f, 1.0f);
		}
		else
		{
			GL11.glColor3f(1.0f, 1.0f, 0.2f);
		}
		float x1 = n1.getX(), z1 = n1.getZ();
		float x2 = n2.getX(), z2 = n2.getZ();
		//		GL11.glVertex2f(edge.entrance1X(), edge.entrance1Z());
		//		GL11.glVertex2f(edge.entrance2X(), edge.entrance2Z());
		GL11.glVertex2f(x1, z1);
		GL11.glVertex2f(x2, z2);
		GL11.glEnd();
		this.glDrawRegion(new Region(new BlockPos(x1 - 1, 0, z1 - 1), new BlockPos(x1 + 1, 0, z1 + 1)), 0.5f, 1.0f, 0.5f);
		this.glDrawRegion(new Region(new BlockPos(x2 - 1, 0, z2 - 1), new BlockPos(x2 + 1, 0, z2 + 1)), 0.5f, 1.0f, 0.5f);
	}

	public void glDrawEntrance(FDGDEdge edge)
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

	public void glDrawNode(FDGDNode n)
	{
		this.glDrawRegion(n.getRegionForBlueprint(), 0.5f, 0.5f, 1.0f);
		if (this.showEntrances)
		{
			for (Entrance e : n.getEntrances(n.getRotation()))
			{
				float x = e.getBounds().getMin().getX(), z = e.getBounds().getMin().getZ();
				float maxX = e.getBounds().getMax().getX(), maxZ = e.getBounds().getMax().getZ();

				this.glDrawRegion(new Region(new BlockPos(x - 1, 0, z - 1), new BlockPos(maxX + 1, 0, maxZ + 1)), 1.0f, 0.5f, 0.5f);
			}
		}
	}

	public void glDrawRegion(IRegion region, float r, float g, float b)
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
