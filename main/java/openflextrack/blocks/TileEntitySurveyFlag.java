package openflextrack.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import openflextrack.OFT;
import openflextrack.OFTCurve;
import openflextrack.OFTRegistry;
import openflextrack.packets.TileEntitySyncPacket;

public class TileEntitySurveyFlag extends TileEntityRotatable{
	public OFTCurve linkedCurve;
		
	public TileEntitySurveyFlag(){
		super();
	}
	
	public void linkToFlag(BlockPos linkedFlagPos){
		if(linkedCurve != null){
			((TileEntitySurveyFlag) worldObj.getTileEntity(linkedCurve.endPos.add(this.pos))).clearFlagLinking();
		}
		TileEntitySurveyFlag linkedFlag = ((TileEntitySurveyFlag) worldObj.getTileEntity(linkedFlagPos));
		linkedCurve = new OFTCurve(linkedFlagPos.subtract(this.pos), rotation*45, linkedFlag.rotation*45);
		OFT.OFTNet.sendToAll(new TileEntitySyncPacket(this));
	}
	
	public void clearFlagLinking(){
		if(linkedCurve != null){
			TileEntitySurveyFlag linkedFlag = ((TileEntitySurveyFlag) worldObj.getTileEntity(linkedCurve.endPos.add(this.pos)));
			linkedCurve = null;
			if(linkedFlag != null){
				linkedFlag.clearFlagLinking();
			}
			OFT.OFTNet.sendToAll(new TileEntitySyncPacket(this));
		}
	}
	
	/**
	 * Spawns dummy tracks based on flag linking.  Returns null if successful or
	 * the coordinates of the location where an existing block is if not.
	 */
	public BlockPos spawnDummyTracks(){
		final OFTCurve thisFlagCurve = linkedCurve;
		final OFTCurve otherFlagCurve = ((TileEntitySurveyFlag) worldObj.getTileEntity(this.pos.add(linkedCurve.endPos))).linkedCurve;
		final boolean isOtherFlagAboveThisOne = thisFlagCurve.endPos.getY() >= 0;
		final Map<BlockPos, Byte> blockMap = new HashMap<BlockPos, Byte>();
		
		//Need to see which end of the curve is higher.
		//If we go top-down, the fake tracks are too high and ballast looks weird.
		//On the other hand, if we went from the other direction we might miss ballast below the track.
		//Steep hills tend to do this, so go in both directions just in case.
		BlockPos blockingBlock = addFakeTracksToMap(thisFlagCurve, blockMap, this.pos);
		if(blockingBlock != null){
			return blockingBlock;
		}
		blockingBlock = addFakeTracksToMap(otherFlagCurve, blockMap, this.pos.add(linkedCurve.endPos));
		if(blockingBlock != null){
			return blockingBlock;
		}
		
		BlockTrackStructureFake.disableMainTrackBreakage();
		for(BlockPos placementPos : blockMap.keySet()){
			worldObj.setBlockState(placementPos, OFTRegistry.trackStructureFake.getDefaultState().withProperty(BlockTrackStructureFake.height, (int) blockMap.get(placementPos)));			
		}
		
		worldObj.setBlockState(this.pos, OFTRegistry.trackStructure.getDefaultState());
		worldObj.setBlockState(this.pos.add(thisFlagCurve.endPos), OFTRegistry.trackStructure.getDefaultState());
		TileEntityTrackStructure startTile = new TileEntityTrackStructure(thisFlagCurve);
		TileEntityTrackStructure endTile = new TileEntityTrackStructure(otherFlagCurve);
		startTile.setFakeTracks(new ArrayList<BlockPos>(blockMap.keySet()));
		endTile.setFakeTracks(new ArrayList<BlockPos>(blockMap.keySet()));
		worldObj.setTileEntity(this.pos, startTile);
		worldObj.setTileEntity(this.pos.add(thisFlagCurve.endPos), endTile);
		BlockTrackStructureFake.enableMainTrackBreakage();
		return null;
	}
	
	private BlockPos addFakeTracksToMap(OFTCurve curve, Map<BlockPos, Byte> blockMap, BlockPos curveOffset){
		float[] currentPoint;		
		float currentAngle;
		float currentSin;
		float currentCos;
		
		for(float f=0; f <= curve.pathLength; f = Math.min(f + 0.05F, curve.pathLength)){
			currentPoint = curve.getCachedPointAt(f/curve.pathLength);
			currentAngle = curve.getCachedYawAngleAt(f/curve.pathLength);
			currentSin = (float) Math.sin(Math.toRadians(currentAngle));
			currentCos = (float) Math.cos(Math.toRadians(currentAngle));
			//Offset the current point slightly to account for the height of the ties.
			//We don't want to judge fake block height by the bottom of the ties,
			//rather we need to judge from the middle of them.
			currentPoint[1] += 1/16F;

			for(byte j=-1; j<=1; ++j){
				BlockPos placementPos = new BlockPos(Math.round(currentPoint[0] - 0.5 + j*currentCos), currentPoint[1], Math.round(currentPoint[2] - 0.5 + j*currentSin)).add(curveOffset);
				if(!worldObj.getBlockState(placementPos).getBlock().canPlaceBlockAt(worldObj, placementPos)){
					if(!(curveOffset.equals(placementPos) || curveOffset.add(curve.endPos).equals(placementPos))){
						return placementPos;
					}
				}
				boolean isBlockInList = false;
				if(blockMap.containsKey(placementPos)){
					isBlockInList = true;
					break;
				}
				if(!isBlockInList){
					System.out.println(currentPoint[1]%1*16F);
					if(currentPoint[1] >= 0){						
						blockMap.put(placementPos, (byte) (currentPoint[1]%1*16F));
					}else{
						//Going from top-down on a slope.  Invert Y.
						blockMap.put(placementPos, (byte) (16 + currentPoint[1]%1*16F));
					}
					//Double-check to see if there's a block already in the list below this one.
					//If so, we're on a slope and that block needs a height of 16.
					if(blockMap.containsKey(placementPos.down())){
						blockMap.put(placementPos.down(), (byte) 15);
					}
				}
			}
			if(f == curve.pathLength){
				//Before we break off, check if we need to add 'spacers' for the beginning and end segments.
				//This is needed for diagonals to have fake tracks in the joins.
				//Do this for the start and end of this curve.
				if(curve.startAngle%90 != 0){
					BlockPos blocker = addSpacersToMap(curveOffset, blockMap);
					if(blocker != null){
						return blocker;
					}
				}
				if(curve.endAngle%90 != 0){
					BlockPos blocker = addSpacersToMap(curveOffset.add(curve.endPos), blockMap);
					if(blocker != null){
						return blocker;
					}
				}
				break;
			}
		}
		return null;
	}
	
	private BlockPos addSpacersToMap(BlockPos posToCheckAround, Map<BlockPos, Byte> blockMap){
		for(byte i=-1; i<=1; ++i){
			for(byte j=-1; j<=1; ++j){
				if((i == 0 || j == 0) && i!=j){
					BlockPos testPos = posToCheckAround.add(i, 0, j);
					if(!worldObj.getBlockState(testPos).getBlock().canPlaceBlockAt(worldObj, testPos)){
						Block blockingBlock = worldObj.getBlockState(testPos).getBlock();
						if(blockingBlock.equals(OFTRegistry.trackStructure) || blockingBlock.equals(OFTRegistry.trackStructureFake)){
							continue;
						}else{
							return testPos;
						}
					}else if(!blockMap.containsKey(testPos)){
						blockMap.put(testPos, (byte) 1);
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox(){
		return INFINITE_EXTENT_AABB;
	}
	
	@Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared(){
        return 65536.0D;
    }
	
	@Override
    public void readFromNBT(NBTTagCompound tagCompound){
        super.readFromNBT(tagCompound);
        int[] linkedFlagCoords = tagCompound.getIntArray("linkedFlagCoords");
        if(tagCompound.getIntArray("linkedFlagCoords").length != 0){
        	linkedCurve = new OFTCurve(new BlockPos(linkedFlagCoords[0], linkedFlagCoords[1], linkedFlagCoords[2]).subtract(this.pos), this.rotation*45, tagCompound.getFloat("linkedFlagAngle"));
        }else{
        	linkedCurve = null;
        }
    }
    
	@Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound){
        super.writeToNBT(tagCompound);
        if(linkedCurve != null){
        	tagCompound.setIntArray("linkedFlagCoords", new int[]{linkedCurve.endPos.getX() + this.pos.getX(), linkedCurve.endPos.getY() + this.pos.getY(), linkedCurve.endPos.getZ() + this.pos.getZ()});
        	tagCompound.setFloat("linkedFlagAngle", linkedCurve.endAngle);
        }
        return tagCompound;
    }
}
