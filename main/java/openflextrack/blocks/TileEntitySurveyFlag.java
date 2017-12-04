package openflextrack.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import openflextrack.OFT;
import openflextrack.OFTRegistry;
import openflextrack.api.IRailType;
import openflextrack.api.ISleeperType;
import openflextrack.api.ITrackContainer;
import openflextrack.api.OFTCurve;
import openflextrack.api.util.Vec3f;
import openflextrack.packets.TileEntitySyncPacket;

/**
 * Survey flag tile entity. Handles flag linkage and dispatches synchronisation packets to clients if needed.
 * 
 * @author don_bruce
 */
public class TileEntitySurveyFlag extends TileEntityRotatable implements ITrackContainer {

	/** Path between this flag and the link. May be {@code null}. */
	public OFTCurve linkedCurve;


	public TileEntitySurveyFlag(){
		super();
	}


	/**
	 * Adds to the given map all fake tracks on the given curve.
	 * 
	 * @param curve - The linked {@link openflextrack.api.OFTCurve curve}.
	 * @param curvePos - The {@link net.minecraft.util.math.BlockPos BlockPos} the curve starts at.
	 * @param blockMap - A map containing all block positions occupied by the linked track.
	 * @param ballast - Number of ballast blocks to each side, excluding the center ballast block.
	 * @return {@code null} if successful, otherwise the BlockPos of the first obstructing block.
	 */
	private BlockPos addFakeTracksToMap(OFTCurve curve, BlockPos curvePos, Map<BlockPos, Byte> blockMap, final int ballast) {

		Vec3f currentPoint;
		float currentAngle;
		float currentSin;
		float currentCos;

		for(float f=0; f <= curve.pathLength; f = Math.min(f + 0.05F, curve.pathLength)){//FIXME @ZnDevelopment - Railbeds may be at slightly incorrect height under certain circumstances.
			currentPoint = curve.getCachedPointAt(f/curve.pathLength);
			currentAngle = curve.getCachedYawAngleAt(f/curve.pathLength);
			currentSin = (float) Math.sin(Math.toRadians(currentAngle));
			currentCos = (float) Math.cos(Math.toRadians(currentAngle));
			//Offset the current point slightly to account for the height of the ties.
			//We don't want to judge fake block height by the bottom of the ties,
			//rather we need to judge from the middle of them.
			currentPoint.y += 1/16F;

			for (int j = -(ballast+1); j <= ballast+1; ++j) {
				BlockPos placementPos = new BlockPos(Math.round(currentPoint.x - 0.5 + j*currentCos), currentPoint.y, Math.round(currentPoint.z - 0.5 + j*currentSin)).add(curvePos);

				if (placementPos.distanceSqToCenter(currentPoint.x+curvePos.getX(), currentPoint.y+curvePos.getY(), currentPoint.z+curvePos.getZ()) > Math.pow(ballast+1.0D, 2)){
					continue;
				}

				if(!worldObj.getBlockState(placementPos).getBlock().canPlaceBlockAt(worldObj, placementPos)){
					if(!(curvePos.equals(placementPos) || curvePos.add(curve.endPos).equals(placementPos))){
						return placementPos;
					}
				}

				if(blockMap.containsKey(placementPos)){
					continue;
				}

				if(currentPoint.y >= 0){
					blockMap.put(placementPos, (byte) (currentPoint.y % 1*16F));
				}else{
					//Going from top-down on a slope.  Invert Y.
					blockMap.put(placementPos, (byte) (16 + currentPoint.y % 1*16F));
				}

				//Double-check to see if there's a block already in the list below this one.
				//If so, we're on a slope and that block needs a height of 16.
				if(blockMap.containsKey(placementPos.down())){
					blockMap.put(placementPos.down(), (byte) 15);
				}
			}

			if(f == curve.pathLength){
				//Before we break off, check if we need to add 'spacers' for the beginning and end segments.
				//This is needed for diagonals to have fake tracks in the joins.
				//Do this for the start and end of this curve.
				if(curve.startAngle%90 != 0){
					BlockPos blocker = addSpacersToMap(curvePos, blockMap);
					if(blocker != null){
						return blocker;
					}
				}
				if(curve.endAngle%90 != 0){
					BlockPos blocker = addSpacersToMap(curvePos.add(curve.endPos), blockMap);
					if(blocker != null){
						return blocker;
					}
				}
				break;
			}
		}

		return null;
	}

	/**
	 * Checks in a cross-pattern around the given block position the four adjacent blocks
	 * whether a block can be placed at their respective positions.
	 * 
	 * @param posCenter - The {@link net.minecraft.util.math.BlockPos block position} to check around.
	 * @param blockMap - A map that all checked block positions will be added to.
	 * @return {@code null} if all blocks are clear (indicating success), or the block position of the obstructing block.
	 */
	private BlockPos addSpacersToMap(BlockPos posCenter, Map<BlockPos, Byte> blockMap){
		for(byte i=-1; i<=1; ++i){
			for(byte j=-1; j<=1; ++j){
				if((i == 0 || j == 0) && i!=j){
					BlockPos testPos = posCenter.add(i, 0, j);
					if(!worldObj.getBlockState(testPos).getBlock().canPlaceBlockAt(worldObj, testPos)){
						Block blockingBlock = worldObj.getBlockState(testPos).getBlock();
						if(blockingBlock.equals(OFTRegistry.trackStructure) || blockingBlock.equals(OFTRegistry.trackStructureFake)){
							continue;
						}
						return testPos;
					}else if(!blockMap.containsKey(testPos)){
						blockMap.put(testPos, (byte) 1);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Called to clear this flag's link as well as to notify the linked flag to clear its link.
	 */
	public void clearFlagLinking() {

		if (linkedCurve == null) {
			return;
		}

		TileEntity tile = worldObj.getTileEntity(linkedCurve.endPos.add(this.pos));
		linkedCurve = null;

		if (tile instanceof TileEntitySurveyFlag) {
			((TileEntitySurveyFlag) tile).clearFlagLinking();
		}

		OFT.OFTNet.sendToAll(new TileEntitySyncPacket(this));
	}

	@Override
	public BlockPos getBlockPos() {
		return this.pos;
	}

	@Override
	public OFTCurve getCurve() {
		return this.linkedCurve;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared(){
		return 65536.0D;
	}

	@Override
	public IRailType getRailType() {
		return DefaultRailType.DEFAULT_RAIL_TYPE;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox(){
		return INFINITE_EXTENT_AABB;
	}

	@Override
	public ISleeperType getSleeperType() {
		return DefaultSleeperType.DEFAULT_SLEEPER_TYPE;
	}

	@Override
	public boolean isHolographic() {
		return true;
	}

	/**
	 * Tries to link this flag with the flag at the given position. May override existing links.
	 * 
	 * @param flagPos - The other flag's {@link net.minecraft.util.math.BlockPos position}.
	 */
	public void linkToFlag(BlockPos flagPos) {

		/* Check whether new position has a survey flag tile. */
		TileEntity tile = worldObj.getTileEntity(flagPos); 
		if (tile instanceof TileEntitySurveyFlag) {

			TileEntitySurveyFlag tileFlag = (TileEntitySurveyFlag) tile;

			/* If a link exists, clear it. */
			if (linkedCurve != null) {
				tile = worldObj.getTileEntity(linkedCurve.endPos.add(this.pos));
				if (tile instanceof TileEntitySurveyFlag) {
					((TileEntitySurveyFlag) tile).clearFlagLinking();	
				}
			}

			/* Create new link and synchronise. */
			linkedCurve = new OFTCurve(flagPos.subtract(this.pos), rotation*45, tileFlag.rotation*45);
			OFT.OFTNet.sendToAll(new TileEntitySyncPacket(this));
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt){

		super.readFromNBT(nbt);

		int[] linkedFlagCoords = nbt.getIntArray("linkedFlagCoords");
		if(linkedFlagCoords.length != 0){
			linkedCurve = new OFTCurve(
					new BlockPos(linkedFlagCoords[0], linkedFlagCoords[1], linkedFlagCoords[2]).subtract(this.pos),
					this.rotation*45,
					nbt.getFloat("linkedFlagAngle"));
		}else{
			linkedCurve = null;
		}
	}

	/**
	 * Spawns dummy tracks based on flag linking.
	 * 
	 * @return {@code null} if successful, otherwise the {@link net.minecraft.util.math.BlockPos BlockPos} of the first obstructing block.
	 */
	public BlockPos spawnDummyTracks() {

		/*
		 * Make sure other tile entity is a flag.
		 */
		TileEntity tile = worldObj.getTileEntity(this.pos.add(linkedCurve.endPos));
		if (!(tile instanceof TileEntitySurveyFlag)) {
			return tile.getPos();
		}

		float width = getSleeperType().getWidth() - 1.0F;
		if (width <= 0.0F) {
			width = 0.0F;
		}

		final int ballast = MathHelper.ceiling_float_int( width / 2.0F );
		final OFTCurve thisFlagCurve = linkedCurve;
		final OFTCurve otherFlagCurve = ((TileEntitySurveyFlag) tile).linkedCurve;
		final Map<BlockPos, Byte> blockMap = new HashMap<BlockPos, Byte>();

		/*
		 * Need to see which end of the curve is higher.
		 * If we go top-down, the fake tracks are too high and ballast looks weird.
		 * On the other hand, if we went from the other direction we might miss ballast below the track.
		 * Steep hills tend to do this, so go in both directions just in case.
		 */
		BlockPos blockingBlock = addFakeTracksToMap(thisFlagCurve, this.pos, blockMap, ballast);
		if(blockingBlock != null){
			return blockingBlock;
		}
		blockingBlock = addFakeTracksToMap(otherFlagCurve, this.pos.add(linkedCurve.endPos), blockMap, ballast);
		if(blockingBlock != null){
			return blockingBlock;
		}

		/*
		 * Finally create fake tracks.
		 */
		BlockTrackFake.toggleMainTrackBreakage(false);
		{
			/* Set block states at selected positions. */
			IBlockState defState = OFTRegistry.trackStructureFake.getDefaultState();

			for (BlockPos placementPos : blockMap.keySet()) {
				worldObj.setBlockState(placementPos, defState.withProperty(BlockTrackFake.height, (int) blockMap.get(placementPos)));			
			}

			/* Set block states and tile entities at start and end. */
			worldObj.setBlockState(this.pos, OFTRegistry.trackStructure.getDefaultState());
			worldObj.setBlockState(this.pos.add(thisFlagCurve.endPos), OFTRegistry.trackStructure.getDefaultState());
			TileEntityTrack startTile = new TileEntityTrack(thisFlagCurve);
			TileEntityTrack endTile = new TileEntityTrack(otherFlagCurve);
			startTile.setFakeTracks(new ArrayList<BlockPos>(blockMap.keySet()));
			endTile.setFakeTracks(new ArrayList<BlockPos>(blockMap.keySet()));
			worldObj.setTileEntity(this.pos, startTile);
			worldObj.setTileEntity(this.pos.add(thisFlagCurve.endPos), endTile);
		}
		BlockTrackFake.toggleMainTrackBreakage(true);

		return null;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		super.writeToNBT(nbt);
		if(linkedCurve != null){
			nbt.setIntArray("linkedFlagCoords", new int[]{
					linkedCurve.endPos.getX() + this.pos.getX(),
					linkedCurve.endPos.getY() + this.pos.getY(),
					linkedCurve.endPos.getZ() + this.pos.getZ()}
					);
			nbt.setFloat("linkedFlagAngle", linkedCurve.endAngle);
		}
		return nbt;
	}
}