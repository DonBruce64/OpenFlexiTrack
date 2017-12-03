package openflextrack.rendering.blockrenders;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import openflextrack.OFTCurve;
import openflextrack.api.ISleeperType;
import openflextrack.blocks.TileEntityTrack;

/**
 * Helper class to handle tie rendering in
 * {@link openflextrack.rendering.blockrenders.RenderTrack#renderTrackSegmentFromCurve(net.minecraft.world.World, net.minecraft.util.math.BlockPos, openflextrack.OFTCurve, boolean, openflextrack.blocks.TileEntityTrack, openflextrack.blocks.TileEntityTrack) renderTrackSegmentFromCurve()}.
 */
public class RenderTies {

	/**
	 * Renders a tie.
	 * 
	 * @param sleeperType - {@link openflextrack.api.ISleeperType Type} of the rendered tie.
	 * @param width - The width between the rails' inner edges. 
	 * @param brightness - The track's brightness.
	 * @param holographic - {@code true} if the track is a hologram.
	 */
	private static void drawTie(ISleeperType sleeperType, float width, float brightness, boolean holographic) {

		if (!holographic) {
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightness%65536, brightness/65536);
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(sleeperType.getTexture());
		GL11.glTranslatef(0, 0, -0.1875F);
		sleeperType.render(width);
	}

	/**
	 * Render ties along the given points.
	 * 
	 * @param sleeperType - {@link openflextrack.api.ISleeperType Type} of the rendered ties.
	 * @param texPoints - The points of the rails to render along.
	 * @param holographic - {@code true} if the rails are rendered as holograms.
	 * @param renderStartTie - {@code true} to render an extra tie at the curve's start.
	 * @param renderEndTie - {@code true} to render an extra tie at the curve's end.
	 * @param startIndex - First index to start iterating through the given points from.
	 * @param offsetIndex - Upper limit of the point iteration, used as {@code texPoints.size() - offsetIndex}.
	 * @param connectedEnd - The {@link openflextrack.blocks.TileEntityTrack track} connected to the curve's end. May be {@code null}.
	 * @param curve - The {@link openflextrack.OFTCurve curve} that is rendered.
	 */
	public static void render(ISleeperType sleeperType, ArrayList<float[]> texPoints, boolean holographic, boolean renderStartTie, boolean renderEndTie,
			byte startIndex, byte offsetIndex, TileEntityTrack connectedEnd, OFTCurve curve) {

		/* First, render any connector ties as they cause trouble in the main loop. */
		if (renderStartTie) {
			float[] texPoint = texPoints.get(1);
			GL11.glPushMatrix();
			GL11.glTranslatef(texPoint[0], texPoint[1] - 0.1875F, texPoint[2]);
			GL11.glRotatef(-curve.startAngle, 0, 1, 0);
			drawTie(sleeperType, texPoint[7], texPoint[6], holographic);
			GL11.glPopMatrix();
		}

		if (renderEndTie && connectedEnd != null && connectedEnd.curve != null) {
			float[] texPoint = texPoints.get(texPoints.size() - 2);
			GL11.glPushMatrix();
			GL11.glTranslatef(texPoint[0], texPoint[1] - 0.1875F, texPoint[2]);
			GL11.glRotatef(-connectedEnd.curve.startAngle, 0, 1, 0);
			drawTie(sleeperType, texPoint[7], texPoint[6], holographic);
			GL11.glPopMatrix();
		}

		/* Render ties, making sure to avoid the start and end connectors. */
		for (short i = startIndex; i < texPoints.size() - offsetIndex; ++i) {
			float[] texPoint = texPoints.get(i);
			float pathPos = (i - startIndex)*sleeperType.getOffset() / curve.pathLength;

			GL11.glPushMatrix();
			GL11.glTranslatef(texPoint[0], texPoint[1] - 0.1875F, texPoint[2]);
			GL11.glRotatef(-curve.getCachedYawAngleAt(pathPos), 0, 1, 0);
			GL11.glRotatef(curve.getCachedPitchAngleAt(pathPos), 1, 0, 0);
			drawTie(sleeperType, texPoint[7], texPoint[6], holographic);
			GL11.glPopMatrix();
		}
	}
}