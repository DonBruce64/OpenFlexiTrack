package openflextrack.rendering.blockrenders;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import openflextrack.OFT;
import openflextrack.OFTCurve;
import openflextrack.blocks.TileEntityTrack;
import openflextrack.rendering.blockmodels.ModelTrackTie;
import openflextrack.util.Vec3f;

@SideOnly(Side.CLIENT)
public class RenderTrack extends TileEntitySpecialRenderer<TileEntityTrack> {

	private static final ModelTrackTie modelTie = new ModelTrackTie();
	private static final ResourceLocation tieTexture = new ResourceLocation(OFT.MODID, "textures/blockmodels/tie.png");
	private static final ResourceLocation railTexture = new ResourceLocation(OFT.MODID, "textures/blockmodels/rail.png");
	private static final ResourceLocation ballastTexture = new ResourceLocation(OFT.MODID, "textures/blocks/ballast.png");

	//These define what the rails look like.  Change ONLY if rail shapes need to change.
	private static final float bottomInnerX = 11.5F/16F;
	private static final float bottomOuterX = 16.5F/16F;
	private static final float bottomLowerY = 0F/16F;
	private static final float bottomUpperY = 1F/16F;

	private static final float middleInnerX = 13.5F/16F;
	private static final float middleOuterX = 14.5F/16F;

	private static final float upperInnerX = 13F/16F;
	private static final float upperOuterX = 15F/16F;
	private static final float upperLowerY = 3F/16F;
	private static final float upperUpperY = 4F/16F;

	/** Default distance between two ties. */
	private static final float tieOffset = 0.65F;


	public RenderTrack() {
		/* Empty constructor. */
	}


	/**
	 * Makes the given track try to connect to adjacent tracks.<br>
	 * If there is a track found, but no connection can't be formed because it hasn't initialised yet,
	 * will return {@code false} and wait for a call during the next tick to try again.
	 * 
	 * @param track - The {@link openflextrack.blocks.TileEntityTrack track} that should try to connect.
	 * @return {@code true} if a connection was successfully formed or if there is no track nearby.
	 */
	private static boolean connectToAdjacentTracks(TileEntityTrack track) {

		for (byte i=-1; i<=1; ++i) {
			for (byte j=-1; j<=1; ++j) {

				if (i == 0 && j == 0) {
					continue;
				}

				TileEntity tile = track.getWorld().getTileEntity(track.getPos().add(i, 0, j));
				if ( !(tile instanceof TileEntityTrack) ) {
					continue;
				}

				TileEntityTrack tileTrack = (TileEntityTrack) tile;
				if (tileTrack.curve != null) {

					if (tileTrack.curve.startAngle == (180 + track.curve.startAngle)%360) {

						/* Make sure we don't link to ourselves. Because players will find a way to make this happen. */
						if (!tileTrack.getPos().equals(track.getPos().add(track.curve.endPos))) {

							/*
							 * If the track we want to link to has already linked with us, stop the link.
							 * Double linkings cause double rendering and lots of errors.
							 */
							if (!track.equals(tileTrack.connectedTrack)) {
								track.connectedTrack = tileTrack;
							}

							return true;
						}
					}
				}
				else {
					/* Wait another tick for the curve of the found track to initialise. */
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Renders a ballast block.
	 */
	private static void drawBallastBox() {

		/*
		 * Sides
		 */
		GL11.glPushMatrix();
		GL11.glTranslatef(0.5F, 0, 0.5F);

		for (byte i=0; i<4; ++i)
		{
			GL11.glPushMatrix();

			GL11.glRotatef(90*i, 0, 1, 0);
			GL11.glTranslatef(-0.5F, 0, 0.5F);

			GL11.glBegin(GL11.GL_QUADS);
			{
				/* Side */
				GL11.glTexCoord2f(0, 0);
				GL11.glNormal3f(0, 0, 1);
				GL11.glVertex3d(0, 0.125F, 0);

				GL11.glTexCoord2f(0, 0.125F);
				GL11.glNormal3f(0, 0, 1);
				GL11.glVertex3d(0, -0.01, 0);

				GL11.glTexCoord2f(1, 0.125F);
				GL11.glNormal3f(0, 0, 1);
				GL11.glVertex3d(1, -0.01, 0);

				GL11.glTexCoord2f(1, 0);
				GL11.glNormal3f(0, 0, 1);
				GL11.glVertex3d(1, 0.125F, 0);

			}
			GL11.glEnd();

			GL11.glPopMatrix();
		}

		GL11.glPopMatrix();

		/*
		 * Top and bottom 
		 */
		GL11.glBegin(GL11.GL_QUADS);
		{
			/* Top */
			GL11.glTexCoord2f(1, 1);
			GL11.glNormal3f(0, 1, 0);
			GL11.glVertex3d(1, 0.125F, 1);

			GL11.glTexCoord2f(1, 0);
			GL11.glNormal3f(0, 1, 0);
			GL11.glVertex3d(1, 0.125F, 0);

			GL11.glTexCoord2f(0, 0);
			GL11.glNormal3f(0, 1, 0);
			GL11.glVertex3d(0, 0.125F, 0);

			GL11.glTexCoord2f(0, 1);
			GL11.glNormal3f(0, 1, 0);
			GL11.glVertex3d(0, 0.125F, 1);

			/* Bottom */
			GL11.glTexCoord2f(0, 0);
			GL11.glNormal3f(0, -1, 0);
			GL11.glVertex3d(0, -0.01, 1);

			GL11.glTexCoord2f(0, 1);
			GL11.glNormal3f(0, -1, 0);
			GL11.glVertex3d(0, -0.01, 0);

			GL11.glTexCoord2f(1, 1);
			GL11.glNormal3f(0, -1, 0);
			GL11.glVertex3d(1, -0.01, 0);

			GL11.glTexCoord2f(1, 0);
			GL11.glNormal3f(0, -1, 0);
			GL11.glVertex3d(1, -0.01, 1);
		}
		GL11.glEnd();
	}

	/**
	 * Draws the end caps of rails.
	 * 
	 * @param texPoint - Position to render the caps at.
	 * @param holographic - {@code true} if the track is a holographic model.
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

	//TODO DESC - Parameter descriptions. @don_bruce please fill these out. I have no clue what each parameter is supposed to do.
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
	 * Renders a tie.
	 * 
	 * @param brightness - The track's brightness.
	 * @param holographic - {@code true} if the track is holographic.
	 */
	private static void drawTie(float brightness, boolean holographic) {

		if(!holographic){
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightness%65536, brightness/65536);
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(tieTexture);
		GL11.glRotatef(180, 1, 0, 0);
		GL11.glTranslatef(0, 0, -0.1875F);
		modelTie.render();
	}

	/**
	 * Compares the given {@link net.minecraft.util.math.BlockPos block positions}
	 * and returns whether the first position's value is greater than the second position's value.<br>
	 * Considers values on Z only if X values are equal. Otherwise considers values on X by default.
	 * 
	 * @return {@code true} if the first block position on either axis is greater than the other block position.
	 * 
	 * @see #isTrackPrimary(TileEntityTrack, TileEntityTrack) isTrackPrimary()
	 */
	private static boolean isPositionPrimary(BlockPos pos1, BlockPos pos2){
		return pos1.getX() != pos2.getX() ? pos1.getX() > pos2.getX() : pos1.getZ() > pos2.getZ();
	}

	/**
	 * Tests if a track is primary. Used for ordering of rendering.
	 * 
	 * @return {@code true} if the first track is of greater value than the other.
	 * 
	 * @see #isPositionPrimary(BlockPos, BlockPos) isPositionPrimary()
	 */
	private static boolean isTrackPrimary(TileEntityTrack track1, TileEntityTrack track2){
		return isPositionPrimary(track1.getPos(), track2.getPos());
	}

	@Override
	public void renderTileEntityAt(TileEntityTrack tileTrack, double x, double y, double z, float partialTicks, int destroyStage) {

		super.renderTileEntityAt(tileTrack, x, y, z, partialTicks, destroyStage);

		/*
		 * If there's nothing to render, don't render.
		 */
		if (tileTrack.curve == null) {
			return;
		}

		/*
		 * Sometimes the TE's don't break evenly. Make sure this doesn't happen and we try to render a flag here.
		 */
		TileEntity tile = tileTrack.getWorld().getTileEntity(tileTrack.getPos().add(tileTrack.curve.endPos));
		if( !(tile instanceof TileEntityTrack) ) {
			return;
		}
		TileEntityTrack otherEnd = (TileEntityTrack) tile;

		/* Quick check to see if connection is still valid. */
		if (tileTrack.connectedTrack != null && tileTrack.connectedTrack.isInvalid()) {
			tileTrack.connectedTrack = null;
			tileTrack.hasTriedToConnectToOtherSegment = false;
		}

		/* If this Tile Entity is not connected, and has not tried to connect, do so now. */
		if (!tileTrack.hasTriedToConnectToOtherSegment) {
			tileTrack.hasTriedToConnectToOtherSegment = connectToAdjacentTracks(tileTrack);
		}

		/*
		 * Try to keep the same track rendering if possible. If the track isn't null, render that one instead.
		 */
		if (isTrackPrimary(otherEnd, tileTrack)) {
			this.renderTileEntityAt(otherEnd, x + otherEnd.getPos().getX() - tileTrack.getPos().getX(), y + otherEnd.getPos().getY() - tileTrack.getPos().getY(), z + otherEnd.getPos().getZ() - tileTrack.getPos().getZ(), partialTicks, destroyStage);
			return;
		}

		/*
		 * Otherwise render this track.
		 */
		GL11.glPushMatrix();
		{
			/* Translate render position. */
			GL11.glTranslated(x, y, z);

			/* Render the finished track, including track beds and rails. */
			GL11.glPushMatrix();
			{
				renderTrackSegmentFromCurve(tileTrack.getWorld(), tileTrack.getPos(), tileTrack.curve, false, tileTrack.connectedTrack, otherEnd.connectedTrack);
			}
			GL11.glPopMatrix();

			/* Render ballast at start and end (block) position of track, respectively. */
			Minecraft.getMinecraft().getTextureManager().bindTexture(ballastTexture);
			GL11.glPushMatrix();
			{
				int lightValue = tileTrack.getWorld().getCombinedLight(tileTrack.getPos(), 0);
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightValue%65536, lightValue/65536);
				drawBallastBox();

				GL11.glTranslatef(tileTrack.curve.endPos.getX(), tileTrack.curve.endPos.getY(), tileTrack.curve.endPos.getZ());
				lightValue = tileTrack.getWorld().getCombinedLight(tileTrack.getPos().add(tileTrack.curve.endPos), 0);
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightValue%65536, lightValue/65536);
				drawBallastBox();
			}
			GL11.glPopMatrix();
		}
		GL11.glPopMatrix();
	}


	/**
	 * Statically renders a track.<br>
	 * <br>
	 * This method may be called from anywhere, not just from within the track's TESR.
	 * 
	 * @param world - {@link net.minecraft.world.World World} object.
	 * @param pos - Starting {@link net.minecraft.util.math.BlockPos position} for the curve.
	 * @param curve - The {@link openflextrack.OFTCurve curve} to render.
	 * @param holographic - {@code true} if the segment is holographic.
	 * @param connectedStart - The {@link openflextrack.blocks.TileEntityTrack track} connected to the start of the rendered track.
	 * @param connectedEnd - The track connected to the end of the rendered track.
	 */
	public static void renderTrackSegmentFromCurve(World world, BlockPos pos, OFTCurve curve, boolean holographic,
			TileEntityTrack connectedStart, TileEntityTrack connectedEnd) {

		/* 
		 * First get information about what connectors need rendering.
		 * 
		 * The idea here is to only have the connections rendered by one track.
		 * In this case, the track that has the highest X or Z is responsible for rendering connections to other tracks.
		 * If a track is only half-rendered (other end is out of sight) it doesn't count.
		 */

		/*
		 * Retrieve connector info on track adjacent to start.
		 */
		boolean renderStartTie = false;
		boolean renderStartRail = false;
		boolean renderStartRailExtra = false;
		TileEntityTrack connectedStartOtherEnd = null;

		if (connectedStart != null && connectedStart.curve != null) {

			if (isPositionPrimary( connectedStart.getPos(), pos.add(curve.endPos) )) {

				/* Connector is the primary for this track and needs to render. */
				TileEntity tile = world.getTileEntity(connectedStart.getPos().add(connectedStart.curve.endPos));
				if (tile instanceof TileEntityTrack) {

					connectedStartOtherEnd = (TileEntityTrack) tile;

					/* If other end is primary, it will be the master render and start of the rendering curve. */
					if (isTrackPrimary(connectedStartOtherEnd, connectedStart)) {

						renderStartRailExtra = true;

						if (connectedStartOtherEnd.curve != null) {
							if (connectedStartOtherEnd.curve.pathLength%tieOffset + curve.pathLength%tieOffset > tieOffset/2) {
								renderStartTie = true;
							}	
						}
					}
					/* This end is the primary. Just add an extra rail segment to the end of this curve. */
					else {
						renderStartRail = true;
					}
				}
			}
		}

		/*
		 * Retrieve connector info on track adjacent to end.
		 */
		boolean renderEndTie = false;
		boolean renderEndRail = false;
		boolean renderEndRailExtra = false;
		TileEntityTrack connectedEndOtherEnd = null;

		if (connectedEnd != null && connectedEnd.curve != null) {

			if (isPositionPrimary( connectedEnd.getPos(), pos.add(curve.endPos) )) {

				/* Connector is the primary for this track and needs to render. */
				TileEntity tile = world.getTileEntity(connectedEnd.getPos().add(connectedEnd.curve.endPos));
				if (tile instanceof TileEntityTrack) {

					connectedEndOtherEnd = (TileEntityTrack) tile;

					/* If other end is primary, it will be the master render and start of the rendering curve. */
					if (isTrackPrimary(connectedEndOtherEnd, connectedEnd)) {

						renderEndRailExtra = true;

						if (connectedEndOtherEnd.curve != null) {
							if (connectedEndOtherEnd.curve.pathLength%tieOffset + curve.pathLength%tieOffset > tieOffset/2) {
								renderEndTie = true;
							}
						}
					}
					/* This end is the primary. Just add an extra rail segment to the end of this curve. */
					else {
						renderEndRail = true;
					}
				}
			}
		}

		/*
		 * Initialise a few extra fields, most importantly the texture coordinates.
		 */
		/* ArrayList containing texture coordinates. */
		ArrayList<float[]> texPoints = new ArrayList<float[]>();
		/* Texture offset. */
		float textureOffset = 0.0F;

		/* Cached values. */
		Vec3f currPoint;
		float currentAngle;

		/*
		 * If there is a track connected to the curve's start..
		 */
		if (connectedStart != null) {

			BlockPos posSrt = connectedStart.getPos();

			/* Get an extra start rail segment if needed. */
			if (renderStartRailExtra && connectedStartOtherEnd != null && connectedStartOtherEnd.curve != null) {

				/* Get the remainder of what rails have not been rendered and add that point. */
				OFTCurve curveSOE = connectedStartOtherEnd.curve;
				float lastPointOnCurve = (curveSOE.pathLength - (curveSOE.pathLength%tieOffset))  /  curveSOE.pathLength;

				Vec3f lastPointSOE = curveSOE.getCachedPointAt(lastPointOnCurve);
				BlockPos posSOE = connectedStartOtherEnd.getPos();
				currPoint = new Vec3f(
						lastPointSOE.x + posSOE.getX() - pos.getX(),
						lastPointSOE.y + posSOE.getY() - pos.getY(),
						lastPointSOE.z + posSOE.getZ() - pos.getZ()
						);

				currentAngle = curveSOE.getCachedYawAngleAt(lastPointOnCurve);
				textureOffset = (float) -(
						Math.hypot(
								currPoint.x - posSrt.getX(),
								currPoint.z - posSrt.getZ()
								) +
						Math.hypot(
								posSrt.getX(),
								posSrt.getZ()
								)
						);

				texPoints.add(new float[]{
						currPoint.x,
						currPoint.y + 0.1875F,
						currPoint.z,
						(float) Math.sin(Math.toRadians(currentAngle)),
						(float) Math.cos(Math.toRadians(currentAngle)),
						textureOffset,
						world.getCombinedLight(new BlockPos(
								(int) Math.ceil(currPoint.x),
								(int) Math.ceil(currPoint.y),
								(int) Math.ceil(currPoint.z)
								).add(pos), 0)
				});
			}

			/* Get a start tie if needed. */
			if ( connectedStart.curve != null && (renderStartTie || (renderStartRail && !renderStartRailExtra)) ) {

				currPoint = new Vec3f(
						posSrt.getX() - pos.getX() + 0.5F,
						posSrt.getY() - pos.getY(),
						posSrt.getZ() - pos.getZ() + 0.5F
						);

				currentAngle = (connectedStart.curve.getCachedYawAngleAt(0) + 180)  %  360;
				textureOffset = (float) -Math.hypot(currPoint.x, currPoint.z);

				texPoints.add(new float[]{
						currPoint.x,
						currPoint.y + 0.1875F,
						currPoint.z,
						(float) Math.sin(Math.toRadians(currentAngle)),
						(float) Math.cos(Math.toRadians(currentAngle)),
						textureOffset,
						world.getCombinedLight(new BlockPos(
								(int) Math.ceil(currPoint.x),
								(int) Math.ceil(currPoint.y),
								(int) Math.ceil(currPoint.z)
								).add(pos), 0)
				});
			}
		}

		/*
		 * Get regular ties and rails.
		 */
		for (float f=0; f <= curve.pathLength; f += tieOffset) {

			currPoint = curve.getCachedPointAt(f/curve.pathLength);
			currentAngle = curve.getCachedYawAngleAt(f/curve.pathLength);

			if (f == 0){
				textureOffset = 0;
			}
			else {
				float[] texPoint = texPoints.get(texPoints.size() - 1);
				textureOffset += (float) Math.hypot(
						currPoint.x - texPoint[0],
						currPoint.z - texPoint[2]
						);
			}

			texPoints.add(new float[]{
					currPoint.x,
					currPoint.y + 0.1875F,
					currPoint.z,
					(float) Math.sin(Math.toRadians(currentAngle)),
					(float) Math.cos(Math.toRadians(currentAngle)),
					textureOffset,
					world.getCombinedLight(new BlockPos(
							(int) Math.ceil(currPoint.x),
							(int) Math.ceil(currPoint.y),
							(int) Math.ceil(currPoint.z)
							).add(pos), 0)
			});
		}

		/*
		 * If there is a track connected to the curve's end..
		 */
		if (connectedEnd != null && connectedEnd.curve != null) {

			/* Get an end tie if needed. */
			if (renderEndTie || (renderEndRail && !renderEndRailExtra)) {

				BlockPos posEnd = connectedEnd.getPos();
				currPoint = new Vec3f(
						posEnd.getX() - pos.getX() + 0.5F,
						posEnd.getY() - pos.getY(),
						posEnd.getZ() - pos.getZ() + 0.5F
						);

				float[] texPoint = texPoints.get(texPoints.size() - 1);
				currentAngle = connectedEnd.curve.startAngle;
				textureOffset += (float) Math.hypot(
						currPoint.x - texPoint[0],
						currPoint.z - texPoint[2]);

				texPoints.add(new float[]{
						currPoint.x,
						currPoint.y + 0.1875F,
						currPoint.z,
						(float) Math.sin(Math.toRadians(currentAngle)),
						(float) Math.cos(Math.toRadians(currentAngle)),
						textureOffset,
						world.getCombinedLight(new BlockPos(
								(int) Math.ceil(currPoint.x),
								(int) Math.ceil(currPoint.y),
								(int) Math.ceil(currPoint.z)
								).add(pos), 0)
				});			
			}
		}

		/*
		 * If there is a track connected to the curve's end and that track has another track connected to it..
		 */
		if (connectedEndOtherEnd != null && connectedEndOtherEnd.curve != null) {

			/* Get an extra end rail segment if needed. */
			if (renderEndRailExtra) {

				/* Get the remainder of what rails have not been rendered and add that point. */
				OFTCurve curveEOE = connectedEndOtherEnd.curve;
				float lastPointOnCurve = (curveEOE.pathLength - (curveEOE.pathLength%tieOffset))/curveEOE.pathLength;

				Vec3f lastPointEOE = curveEOE.getCachedPointAt(lastPointOnCurve);
				BlockPos posEOE = connectedEndOtherEnd.getPos();

				currPoint = new Vec3f(
						lastPointEOE.x + posEOE.getX() - pos.getX(),
						lastPointEOE.y + posEOE.getY() - pos.getY(),
						lastPointEOE.z + posEOE.getZ() - pos.getZ()
						);

				float[] texPoint = texPoints.get(texPoints.size() - 1);
				currentAngle = (curveEOE.getCachedYawAngleAt(lastPointOnCurve) + 180)  %  360;
				textureOffset += (float) Math.hypot(
						currPoint.x - texPoint[0],
						currPoint.z - texPoint[2]);

				texPoints.add(new float[]{
						currPoint.x,
						currPoint.y + 0.1875F,
						currPoint.z,
						(float) Math.sin(Math.toRadians(currentAngle)),
						(float) Math.cos(Math.toRadians(currentAngle)),
						textureOffset,
						world.getCombinedLight(new BlockPos(
								(int) Math.ceil(currPoint.x),
								(int) Math.ceil(currPoint.y),
								(int) Math.ceil(currPoint.z)
								).add(pos), 0)
				});
			}
		}

		/*
		 * Now that we have all the points, it's time to render.
		 */
		/* If holographic rendering is enabled, enable OpenGL blend. */
		if (holographic) {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glColor4f(0, 1, 0, 0.25F);
		}

		/* First, render any connector ties as they cause trouble in the main loop. */
		if (renderStartTie) {
			float[] texPoint = texPoints.get(1);
			GL11.glPushMatrix();
			GL11.glTranslatef(texPoint[0], texPoint[1] - 0.1875F, texPoint[2]);
			GL11.glRotatef(-curve.startAngle, 0, 1, 0);
			drawTie(texPoint[6], holographic);
			GL11.glPopMatrix();
		}

		if (renderEndTie && connectedEnd != null && connectedEnd.curve != null) {
			float[] texPoint = texPoints.get(texPoints.size() - 2);
			GL11.glPushMatrix();
			GL11.glTranslatef(texPoint[0], texPoint[1] - 0.1875F, texPoint[2]);
			GL11.glRotatef(-connectedEnd.curve.startAngle, 0, 1, 0);
			drawTie(texPoint[6], holographic);
			GL11.glPopMatrix();
		}

		/* Render ties, making sure to avoid the start and end connectors. */
		byte startIndex = (byte) (renderStartTie ? 2 : (renderStartRail || renderStartRailExtra ? 1 : 0));
		byte offsetIndex = (byte) (renderEndTie ? 2 : ((renderEndRail || renderEndRailExtra) ? 1 : 0));

		for (short i = startIndex; i < texPoints.size() - offsetIndex; ++i) {
			float[] texPoint = texPoints.get(i);
			float pathPos = (i - startIndex)*tieOffset/curve.pathLength;

			GL11.glPushMatrix();
			GL11.glTranslatef(texPoint[0], texPoint[1] - 0.1875F, texPoint[2]);
			GL11.glRotatef(-curve.getCachedYawAngleAt(pathPos), 0, 1, 0);
			GL11.glRotatef(curve.getCachedPitchAngleAt(pathPos), 1, 0, 0);
			drawTie(texPoint[6], holographic);
			GL11.glPopMatrix();
		}

		/* 
		 * Render rails.
		 * These use all the points so no special logic is required.
		 */
		GL11.glPushMatrix();
		Minecraft.getMinecraft().getTextureManager().bindTexture(railTexture);
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

		/* If holographic rendering is enabled, disable OpenGL blend again. */
		if (holographic) {
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glColor4f(1, 1, 1, 1);
		}
	}
}