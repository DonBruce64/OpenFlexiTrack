package openflextrack.rendering.blockrenders;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import openflextrack.api.IRailType;

/**
 * Helper class to handle rail rendering in
 * {@link openflextrack.rendering.blockrenders.RenderTrack#renderTrackSegmentFromCurve(net.minecraft.world.World, net.minecraft.util.math.BlockPos, openflextrack.OFTCurve, boolean, openflextrack.blocks.TileEntityTrack, openflextrack.blocks.TileEntityTrack) renderTrackSegmentFromCurve()}.
 */
public class RenderRails {

	/**
	 * These define the shape of rails.
	 * Change only if rail shapes need to change.
	 */
	private static float
	bottomInnerX = 11.5F/16F,
	bottomOuterX = 16.5F/16F,
	bottomLowerY = 0F/16F,
	bottomUpperY = 1F/16F,

	middleInnerX = 13.5F/16F,
	middleOuterX = 14.5F/16F,

	upperInnerX = 13F/16F,
	upperOuterX = 15F/16F,
	upperLowerY = 3F/16F,
	upperUpperY = 4F/16F;


	/**
	 * Draws the end caps of rails.
	 * 
	 * @param texPoint - Position to render the caps at.
	 * @param holographic - {@code true} if the track is a hologram.
	 */
	private static void drawRailEndCaps(float[] texPoint, boolean holographic) {

		GL11.glPushMatrix();
		GL11.glBegin(GL11.GL_QUADS);
		{
			if (!holographic) {
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, texPoint[6]%65536, texPoint[6]/65536);
			}

			// TODO DESC - Description of code blocks
			GL11.glTexCoord2d(0F/19F, 6F/19F);
			GL11.glVertex3d(bottomOuterX*texPoint[4], bottomLowerY, bottomOuterX*texPoint[3]);
			GL11.glTexCoord2d(5F/19F, 6F/19F);
			GL11.glVertex3d(bottomInnerX*texPoint[4], bottomLowerY, bottomInnerX*texPoint[3]);
			GL11.glTexCoord2d(5F/19F, 7F/19F);
			GL11.glVertex3d(bottomInnerX*texPoint[4], bottomUpperY, bottomInnerX*texPoint[3]);
			GL11.glTexCoord2d(0F/19F, 7F/19F);
			GL11.glVertex3d(bottomOuterX*texPoint[4], bottomUpperY, bottomOuterX*texPoint[3]);

			GL11.glTexCoord2d(0F/19F, 7F/19F);
			GL11.glVertex3d(middleOuterX*texPoint[4], bottomUpperY, middleOuterX*texPoint[3]);
			GL11.glTexCoord2d(1F/19F, 7F/19F);
			GL11.glVertex3d(middleInnerX*texPoint[4], bottomUpperY, middleInnerX*texPoint[3]);
			GL11.glTexCoord2d(1F/19F, 9F/19F);
			GL11.glVertex3d(middleInnerX*texPoint[4], upperLowerY, middleInnerX*texPoint[3]);
			GL11.glTexCoord2d(0F/19F, 9F/19F);
			GL11.glVertex3d(middleOuterX*texPoint[4], upperLowerY, middleOuterX*texPoint[3]);

			GL11.glTexCoord2d(0F/19F, 9F/19F);
			GL11.glVertex3d(upperOuterX*texPoint[4], upperLowerY, upperOuterX*texPoint[3]);
			GL11.glTexCoord2d(2F/19F, 9F/19F);
			GL11.glVertex3d(upperInnerX*texPoint[4], upperLowerY, upperInnerX*texPoint[3]);
			GL11.glTexCoord2d(2F/19F, 10F/19F);
			GL11.glVertex3d(upperInnerX*texPoint[4], upperUpperY, upperInnerX*texPoint[3]);
			GL11.glTexCoord2d(0F/19F, 10F/19F);
			GL11.glVertex3d(upperOuterX*texPoint[4], upperUpperY, upperOuterX*texPoint[3]);

			GL11.glTexCoord2d(5F/19F, 6F/19F);
			GL11.glVertex3d(-bottomInnerX*texPoint[4], bottomLowerY, -bottomInnerX*texPoint[3]);
			GL11.glTexCoord2d(0F/19F, 6F/19F);
			GL11.glVertex3d(-bottomOuterX*texPoint[4], bottomLowerY, -bottomOuterX*texPoint[3]);
			GL11.glTexCoord2d(0F/19F, 7F/19F);
			GL11.glVertex3d(-bottomOuterX*texPoint[4], bottomUpperY, -bottomOuterX*texPoint[3]);
			GL11.glTexCoord2d(5F/19F, 7F/19F);
			GL11.glVertex3d(-bottomInnerX*texPoint[4], bottomUpperY, -bottomInnerX*texPoint[3]);

			GL11.glTexCoord2d(1F/19F, 7F/19F);
			GL11.glVertex3d(-middleInnerX*texPoint[4], bottomUpperY, -middleInnerX*texPoint[3]);
			GL11.glTexCoord2d(0F/19F, 7F/19F);
			GL11.glVertex3d(-middleOuterX*texPoint[4], bottomUpperY, -middleOuterX*texPoint[3]);
			GL11.glTexCoord2d(0F/19F, 9F/19F);
			GL11.glVertex3d(-middleOuterX*texPoint[4], upperLowerY, -middleOuterX*texPoint[3]);
			GL11.glTexCoord2d(1F/19F, 9F/19F);
			GL11.glVertex3d(-middleInnerX*texPoint[4], upperLowerY, -middleInnerX*texPoint[3]);

			GL11.glTexCoord2d(2F/19F, 9F/19F);
			GL11.glVertex3d(-upperInnerX*texPoint[4], upperLowerY, -upperInnerX*texPoint[3]);
			GL11.glTexCoord2d(0F/19F, 9F/19F);
			GL11.glVertex3d(-upperOuterX*texPoint[4], upperLowerY, -upperOuterX*texPoint[3]);
			GL11.glTexCoord2d(0F/19F, 10F/19F);
			GL11.glVertex3d(-upperOuterX*texPoint[4], upperUpperY, -upperOuterX*texPoint[3]);
			GL11.glTexCoord2d(2F/19F, 10F/19F);
			GL11.glVertex3d(-upperInnerX*texPoint[4], upperUpperY, -upperInnerX*texPoint[3]);
		}
		GL11.glEnd();
		GL11.glPopMatrix();
	}

	//TODO @don_bruce DESC - Parameter descriptions. Please fill these out. I have no clue what each parameter is supposed to do.
	/**
	 * Draws a rail segment with the given coordinates.
	 * 
	 * @param texPoints
	 * @param w1
	 * @param w2
	 * @param h1
	 * @param h2
	 * @param t1
	 * @param t2
	 * @param holographic - {@code true} if the track is holographic.
	 */
	private static void drawRailSegment(List<float[]> texPoints, float w1, float w2, float h1, float h2, float t1, float t2, boolean holographic) {

		// TODO DESC - Description of code blocks
		GL11.glBegin(GL11.GL_QUAD_STRIP);
		{
			for (float[] point : texPoints) {

				if (!holographic) {
					OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, point[6]%65536, point[6]/65536);
				}

				GL11.glTexCoord2d(point[5], t2);
				GL11.glNormal3f(0, 1, 0);
				GL11.glVertex3d(point[0] + w1*point[4], point[1] + h1, point[2] + w1*point[3]);

				GL11.glTexCoord2d(point[5], t1);
				GL11.glNormal3f(0, 1, 0);
				GL11.glVertex3d(point[0] + w2*point[4], point[1] + h2, point[2] + w2*point[3]);
			}
		}
		GL11.glEnd();

		GL11.glBegin(GL11.GL_QUAD_STRIP);
		{
			for (float[] point : texPoints) {

				if (!holographic) {
					OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, point[6]%65536, point[6]/65536);
				}

				GL11.glTexCoord2d(point[5], t1);
				GL11.glNormal3f(0, 1, 0);
				GL11.glVertex3d(point[0] - w2*point[4], point[1] + h2, point[2] - w2*point[3]);

				GL11.glTexCoord2d(point[5], t2);
				GL11.glNormal3f(0, 1, 0);
				GL11.glVertex3d(point[0] - w1*point[4], point[1] + h1, point[2] - w1*point[3]);
			}
		}
		GL11.glEnd();
	}

	/**
	 * Pushes the given rail type's vertices to the local vertex cache.
	 * 
	 * @param type - The {@link openflextrack.api.IRailType rail type} to read vertices from.
	 */
	private static void pushVertices(IRailType type) {

		/* Check vertex array length first. */
		final float[] vertices = type.getRailVertices();
		if (vertices.length != 10) {
			return;
		}

		/* Bottom (base) */
		bottomInnerX = vertices[0];
		bottomOuterX = vertices[1];
		bottomLowerY = vertices[2];
		bottomUpperY = vertices[3];

		/* Middle (web) */
		middleInnerX = vertices[4];
		middleOuterX = vertices[5];

		/* Upper (head) */
		upperInnerX = vertices[6];
		upperOuterX = vertices[7];
		upperLowerY = vertices[8];
		upperUpperY = vertices[9];
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

		pushVertices(railType);

		GL11.glPushMatrix();
		Minecraft.getMinecraft().getTextureManager().bindTexture(railType.getTexture());
		drawRailSegment(texPoints, bottomInnerX, bottomOuterX, bottomLowerY, bottomLowerY, 0.0F, 3F/19F, holographic);//Bottom
		drawRailSegment(texPoints, bottomOuterX, bottomOuterX, bottomLowerY, bottomUpperY, 3F/19F, 4F/19F, holographic);//Outer-bottom-side
		drawRailSegment(texPoints, bottomOuterX, middleOuterX, bottomUpperY, bottomUpperY, 4F/19F, 5.5F/19F, holographic);//Outer-bottom-top
		drawRailSegment(texPoints, middleOuterX, middleOuterX, bottomUpperY, upperLowerY, 6F/19F, 8F/19F, holographic);//Outer-middle
		drawRailSegment(texPoints, middleOuterX, upperOuterX, upperLowerY, upperLowerY, 8F/19F, 8.5F/19F, holographic);//Outer-top-under
		drawRailSegment(texPoints, upperOuterX, upperOuterX, upperLowerY, upperUpperY, 9F/19F, 10F/19F, holographic);//Outer-top-side
		drawRailSegment(texPoints, upperOuterX, upperInnerX, upperUpperY, upperUpperY, 10F/19F, 12F/19F, holographic);//Top
		drawRailSegment(texPoints, upperInnerX, upperInnerX, upperUpperY, upperLowerY, 12F/19F, 13F/19F, holographic);//Inner-top-side
		drawRailSegment(texPoints, upperInnerX, middleInnerX, upperLowerY, upperLowerY, 13F/19F, 13.5F/19F, holographic);//Inner-top-under
		drawRailSegment(texPoints, middleInnerX, middleInnerX, upperLowerY, bottomUpperY, 14F/19F, 16F/19F, holographic);//Inner-middle
		drawRailSegment(texPoints, middleInnerX, bottomInnerX, bottomUpperY, bottomUpperY, 16F/19F, 17.5F/19F, holographic);//Inner-bottom-top
		drawRailSegment(texPoints, bottomInnerX, bottomInnerX, bottomUpperY, bottomLowerY, 18F/19F, 19F/19F, holographic);//Inner-bottom-side
		GL11.glPopMatrix();

		/* 
		 * Finally, render a end cap on rails if there's no connection.
		 * 
		 * Note that if another rail connects with this one, the cap will still be rendered.
		 * Not a big deal in the grand scheme of things.
		 */
		if (!renderStartRail) {
			float[] texPoint = texPoints.get(0);
			GL11.glPushMatrix();
			GL11.glTranslatef(texPoint[0], texPoint[1], texPoint[2]);
			drawRailEndCaps(texPoint, holographic);
			GL11.glPopMatrix();
		}

		if (!renderEndRail) {
			float[] texPoint = texPoints.get(texPoints.size() - 1);
			GL11.glPushMatrix();
			GL11.glTranslatef(texPoint[0], texPoint[1], texPoint[2]);
			GL11.glRotatef(180, 0, 1, 0);
			drawRailEndCaps(texPoint, holographic);
			GL11.glPopMatrix();
		}
	}
}