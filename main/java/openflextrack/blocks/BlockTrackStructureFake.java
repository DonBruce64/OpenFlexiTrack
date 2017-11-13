package openflextrack.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import openflextrack.OFTRegistry;

public class BlockTrackStructureFake extends Block{
	public static final PropertyInteger height = PropertyInteger.create("height", 0, 15);
	private static final AxisAlignedBB heightBoxes[] = initHeightBoxes();
	private static boolean shouldTryToBreakTrackWhenBroken = true;
	private static BlockPos firstBrokenBlockPos;

	public BlockTrackStructureFake(){
		super(Material.IRON);
		this.setHardness(5.0F);
		this.setResistance(10.0F);
		this.setDefaultState(this.blockState.getBaseState().withProperty(height, 15));
		this.fullBlock = false;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player){
		BlockPos masterPos = getMasterPos(world, pos);
		if(masterPos != null){
			return world.getBlockState(masterPos).getBlock().getPickBlock(world.getBlockState(masterPos), target, world, masterPos, player);
		}
		return null;
	}	

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state){
		if(shouldTryToBreakTrackWhenBroken){
			BlockPos masterPos = getMasterPos(world, pos);
			if(masterPos != null){
				firstBrokenBlockPos = pos;
				world.setBlockToAir(masterPos);
				firstBrokenBlockPos = null;
			}
		}
		super.breakBlock(world, pos, state);
	}

	public BlockPos getMasterPos(World world, final BlockPos thisPos){
		List<BlockPos> testedBlocks = new ArrayList<BlockPos>();
		List<BlockPos> blocksToTest = new ArrayList<BlockPos>();
		testedBlocks.add(thisPos);
		blocksToTest.add(thisPos);

		while(blocksToTest.size() > 0){
			if(blocksToTest.size() > 0){
				BlockPos testingPos = blocksToTest.get(0);
				for(EnumFacing searchOffset : EnumFacing.VALUES){
					if(testedBlocks.contains(testingPos.offset(searchOffset))){
						//Block already tested from another block.  Don't test it again.
						continue;
					}else if(Math.sqrt(testingPos.offset(searchOffset).distanceSq(thisPos)) > 150){
						//Block is too far to possibly be the master TE for this block.
						continue;
					}
					if(world.getTileEntity(testingPos.offset(searchOffset)) instanceof TileEntityTrackStructure){
						//Found a track TE.  See if it's the parent for this fake track block.
						if(((TileEntityTrackStructure) world.getTileEntity(testingPos.offset(searchOffset))).getFakeTracks().contains(thisPos)){
							return testingPos.offset(searchOffset);
						}
					}else if(world.getBlockState(testingPos.offset(searchOffset)).getBlock().equals(OFTRegistry.trackStructureFake)){
						//Found another fake track.  Check to see if this has been tested and add to lists if not so.
						if(!testedBlocks.contains(testingPos.offset(searchOffset))){
							//First make sure block hasn't been tested already.
							if(!blocksToTest.contains(testingPos.offset(searchOffset))){
								//If the blocks to test from don't contain this block, add it now.
								blocksToTest.add(testingPos.offset(searchOffset));
							}
						}
					}
				}
				//End of the facing loop for this block.  Set block as tested and remove from the toTestFrom list.
				blocksToTest.remove(testingPos);
				testedBlocks.add(testingPos);
			}
		}
		return null;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune){
		return null;
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, height);
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(height);
	}

	//Depreciated, but correct so say master modders.
	@Override
	@Deprecated
	public IBlockState getStateFromMeta(int meta){
		return this.getDefaultState().withProperty(height, meta);
	}

	@Override
	@Deprecated
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}

	@Override
	@Deprecated
	public boolean isFullCube(IBlockState state){
		return false;
	}

	@Override
	@Deprecated
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		return heightBoxes[state.getValue(height)];
	}

	public static void enableMainTrackBreakage(){
		shouldTryToBreakTrackWhenBroken = true;
	}

	public static void disableMainTrackBreakage(){
		shouldTryToBreakTrackWhenBroken = false;
	}

	public static BlockPos getLastHitFakeTrack(){
		return firstBrokenBlockPos;
	}

	private static AxisAlignedBB[] initHeightBoxes(){
		AxisAlignedBB[] heightBoxes = new AxisAlignedBB[16];
		for(byte i=0; i<16; ++i){
			heightBoxes[i] = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, (i+1)/16F, 1.0F);
		}
		return heightBoxes;
	}
}
