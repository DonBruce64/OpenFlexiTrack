package openflextrack.rendering.blockrenders;

import java.util.ArrayList;

import javax.annotation.Nullable;

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
import openflextrack.api.ISleeperType;
import openflextrack.api.ITrackContainer;
import openflextrack.api.OFTCurve;
import openflextrack.api.util.Vec3f;
import openflextrack.blocks.TileEntityTrack;

@SideOnly(Side.CLIENT)
public class RenderTrack extends TileEntitySpecialRenderer<TileEntityTrack> {
	
	//FIXME @ZnDevelopment PERFORMANCE - Benchmark rendering and eliminate possible bottlenecks. The new implementation takes a toll on FPS. Baaaad.

	private static final ResourceLocation ballastTexture = new ResourceLocation(OFT.MODID, "textures/blocks/ballast.png");


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
								tileTrack.connectedTrack = track;
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

			// Reset the other track as well, if existent.
			if (tileTrack.connectedTrack.connectedTrack == tileTrack) {
				tileTrack.connectedTrack.connectedTrack = null;
				tileTrack.connectedTrack.hasTriedToConnectToOtherSegment = false;
			}

			// Then reset this track.
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
				renderTrackFromCurve(tileTrack, tileTrack.connectedTrack, otherEnd.connectedTrack);
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
	 * @param trackContainer - The {@link openflextrack.api.ITrackContainer track} to render.
	 * @param connectedStart - The {@link openflextrack.blocks.TileEntityTrack track} connected to the start of the rendered track. May be {@code null}.
	 * @param connectedEnd - The track connected to the end of the rendered track. May be {@code null}.
	 */
	public static void renderTrackFromCurve(ITrackContainer trackContainer, @Nullable TileEntityTrack connectedStart, @Nullable TileEntityTrack connectedEnd) {

		/* Don't render if there's no curve. */
		OFTCurve curve = trackContainer.getCurve();
		if (curve == null) {
			return;
		}

		/*
		 * Read track container data.
		 */
		World world = trackContainer.getWorld();
		BlockPos pos = trackContainer.getBlockPos();
		ISleeperType sleeperType = trackContainer.getSleeperType();
		final float tieOffset = sleeperType.getOffset();
		final float tieWidth = sleeperType.getDefaultWidth();
		final boolean holographic = trackContainer.isHolographic();

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
				//FIXME @ZnDevelopment this right here is the source of the textures being wrong on connectors.
				//I have no clue what all these vectors, pos's, and such are about, but you need to find the distance from the center
				//of the current start rail to the center of the last point on the rail connected to the start.
				//Or the first of that point, should the rail starts be butted together.
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
								).add(pos), 0),
						tieWidth
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
								).add(pos), 0),
						tieWidth
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
							).add(pos), 0),
					tieWidth
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
								).add(pos), 0),
						tieWidth
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
								).add(pos), 0),
						tieWidth
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

		/* Render ties. */
		RenderTies.render(
				sleeperType,
				texPoints,
				holographic,
				renderStartTie,
				renderEndTie,
				(byte) (renderStartTie ? 2 : (renderStartRail || renderStartRailExtra ? 1 : 0)),
				(byte) (renderEndTie ? 2 : ((renderEndRail || renderEndRailExtra) ? 1 : 0)),
				connectedEnd,
				curve
				);

		/* Render rails. */
		RenderRails.render(
				trackContainer.getRailType(),
				texPoints,
				holographic,
				renderStartRail,
				renderEndRail
				);

		/* If holographic rendering is enabled, disable OpenGL blend again. */
		if (holographic) {
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glColor4f(1, 1, 1, 1);
		}
	}
}