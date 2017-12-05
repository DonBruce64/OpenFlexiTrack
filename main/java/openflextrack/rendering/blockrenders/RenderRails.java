package openflextrack.rendering.blockrenders;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import openflextrack.api.IRailType;
import openflextrack.api.util.Vec3f;

/**
 * Helper class to handle rail rendering in
 * {@link openflextrack.rendering.blockrenders.RenderTrack#renderTrackFromCurve(net.minecraft.world.World, net.minecraft.util.math.BlockPos, openflextrack.OFTCurve, boolean, openflextrack.blocks.TileEntityTrack, openflextrack.blocks.TileEntityTrack) renderTrackSegmentFromCurve()}.
 */
public class RenderRails {

	/**
	 * Draws the end caps of rails.
	 * 
	 * @param texPoint - Position to render the caps at.
	 * @param vertices - Rail shape vertices, as returned by {@link openflextrack.api.IRailType#getRailVertices() getRailVertices()}.
	 * @param texScale - {@link openflextrack.api.IRailType#getTextureScale() Texture scale} of the rail segment.
	 * @param holographic - {@code true} if the track is a hologram.
	 */
	private static void drawRailEndCaps(float[] texPoint, Vec3f[][] vertices, float texScale, boolean holographic) {

		/* Backup face mode. */
		final int glFaceMode = GL11.glGetInteger(GL11.GL_FRONT_FACE);

		for (byte b = 1; b > -2; b-=2)
		{
			GL11.glPushMatrix();
			GL11.glScalef(texPoint[4], 1.0F, texPoint[3]);

			/* If we're rendering the second cap, invert front face mode and scale.
			 * This will mirror the cap without the need to render vertices in different order. */
			if (b == -1) {
				GL11.glScalef(-1, 1, -1);
				if (glFaceMode == GL11.GL_CCW) {
					GL11.glFrontFace(GL11.GL_CW);
				}
				else {
					GL11.glFrontFace(GL11.GL_CCW);
				}
			}

			/* Actually render face. */
			GL11.glBegin(GL11.GL_QUADS);
			{
				if (!holographic) {
					OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, texPoint[6]%65536, texPoint[6]/65536);
				}

				for (Vec3f[] verts : vertices)
				{
					/* The fourth vertex (index 3; bottom left of texture)
					 * defines x offset and minumum y value,
					 * and is the first point of the rectangle. */
					final float xOff = (verts[3].x-verts[0].x);
					final float yMin = verts[3].z;

					GL11.glTexCoord2d(0.0F, yMin + (verts[0].y-verts[3].y)*texScale);
					GL11.glVertex3d(verts[3].x, verts[3].y, verts[3].x);

					GL11.glTexCoord2d((verts[3].x-verts[2].x), yMin - (verts[3].y-verts[2].y)*texScale + (verts[1].y-verts[2].y)*texScale);
					GL11.glVertex3d(verts[2].x, verts[2].y, verts[2].x);

					GL11.glTexCoord2d(xOff + (verts[0].x-verts[1].x), yMin + (verts[0].y-verts[1].y)*texScale);
					GL11.glVertex3d(verts[1].x, verts[1].y, verts[1].x);

					GL11.glTexCoord2d(xOff, yMin);
					GL11.glVertex3d(verts[0].x, verts[0].y, verts[0].x);
				}
			}
			GL11.glEnd();
			GL11.glPopMatrix();
		}

		/* Reset face mode. */
		GL11.glFrontFace(glFaceMode);
	}

	/**
	 * Draws a rail segment with the given coordinates.
	 * 
	 * @param texPoints
	 * @param w1 - X-coordinate of first vertex.
	 * @param w2 - X-coordinate of second vertex.
	 * @param h1 - Y-coordinate of first vertex.
	 * @param h2 - Y-coordinate of second vertex.
	 * @param t1 - Y-texture offset of first vertex.
	 * @param texScale - {@link openflextrack.api.IRailType#getTextureScale() Texture scale} of the rail segment.
	 * @param holographic - {@code true} if the track is holographic.
	 */
	private static void drawRailSegment(List<float[]> texPoints, float w1, float w2, float h1, float h2, float t1, float texScale, boolean holographic) {

		/* Backup face mode. */
		final int glFaceMode = GL11.glGetInteger(GL11.GL_FRONT_FACE);
		final float t2 = t1 + (Math.abs(w2-w1) + Math.abs(h2-h1)) * texScale;

		for (byte b = 1; b > -2; b-=2)
		{
			GL11.glPushMatrix();

			/* If we're rendering the second rail, invert front face mode and scale.
			 * This will mirror the rail without the need to render vertices in different order. */
			if (b == -1) {
				if (glFaceMode == GL11.GL_CCW) {
					GL11.glFrontFace(GL11.GL_CW);
				}
				else {
					GL11.glFrontFace(GL11.GL_CCW);
				}
			}

			/* Actually render the rail. */
			GL11.glBegin(GL11.GL_QUAD_STRIP);
			{
				for (float[] point : texPoints) {

					if (!holographic) {
						OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, point[6]%65536, point[6]/65536);
					}

					GL11.glTexCoord2d(point[5], t2);
					GL11.glNormal3f(0, 1, 0);
					GL11.glVertex3d(point[0] + b*w1*point[4], point[1] + h1, point[2] + b*w1*point[3]);

					GL11.glTexCoord2d(point[5], t1);
					GL11.glNormal3f(0, 1, 0);
					GL11.glVertex3d(point[0] + b*w2*point[4], point[1] + h2, point[2] + b*w2*point[3]);
				}
			}
			GL11.glEnd();
			GL11.glPopMatrix();
		}

		/* Reset face mode. */
		GL11.glFrontFace(glFaceMode);
	}

	/**
	 * Render rails along the given points.
	 * 
	 * @param railType - {@link openflextrack.api.IRailType Type} of the rendered rails.
	 * @param texPoints - The points of the rails to render along.
	 * @param holographic - {@code true} if the rails are rendered as holograms.
	 * @param renderStartRail - {@code true} to render an extra rail at the curve's start.
	 * @param renderEndRail - {@code true} to render an extra rail at the curve's end.
	 */
	public static void render(IRailType railType, ArrayList<float[]> texPoints, boolean holographic, boolean renderStartRail, boolean renderEndRail) {

		/*
		 * Render rails from given vertices.
		 */
		final float texScale = railType.getTextureScale();
		Vec3f[][] vertices = railType.getRailVertices();
		Minecraft.getMinecraft().getTextureManager().bindTexture(railType.getTexture());
		GL11.glPushMatrix();
		{
			for (Vec3f[] verts : vertices)
			{
				for (int i = 0; i < verts.length; ++i)
				{
					Vec3f nextVec = (i+1 < verts.length ? verts[i+1] : verts[0]);
					drawRailSegment(
							texPoints,
							verts[i].x, nextVec.x,
							verts[i].y, nextVec.y,
							verts[i].z, texScale,
							holographic);
				}
			}
		}
		GL11.glPopMatrix();

		/* 
		 * Finally, render end caps on rails if there's no connection.
		 * 
		 * Note that if another rail connects with this one, the cap will still be rendered.
		 * Not a big deal in the grand scheme of things.
		 */
		if (!renderStartRail) {
			float[] texPoint = texPoints.get(0);
			GL11.glPushMatrix();
			GL11.glTranslatef(texPoint[0], texPoint[1], texPoint[2]);
			drawRailEndCaps(texPoint, vertices, texScale, holographic);
			GL11.glPopMatrix();
		}

		if (!renderEndRail) {
			float[] texPoint = texPoints.get(texPoints.size() - 1);
			GL11.glPushMatrix();
			GL11.glTranslatef(texPoint[0], texPoint[1], texPoint[2]);
			GL11.glRotatef(180, 0, 1, 0);
			drawRailEndCaps(texPoint, vertices, texScale, holographic);
			GL11.glPopMatrix();
		}
	}
}