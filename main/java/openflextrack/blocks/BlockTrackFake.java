package openflextrack.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import openflextrack.OFTRegistry;
import trackapi.lib.ITrackBlock;
import trackapi.lib.Gauges;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Optional.Interface(iface = "trackapi.lib.ITrackBlock", modid = "trackapi")
public class BlockTrackFake extends Block implements ITrackBlock {

	/** Height property of this block, used with {@link net.minecraft.block.state.IBlockState block states}. */
	public static final PropertyInteger height = PropertyInteger.create("height", 0, 15);

	/** Array holding all bounding boxes depending on {@link #height}. */
	private static final AxisAlignedBB heightBoxes[] = initHeightBoxes();

	/** {@code true} if the main track should be broken if this fake block is being broken. */
	private static boolean tryBreakTrackWhenBroken = true;

	/** Reference to the first broken block's {@link net.minecraft.util.math.BlockPos BlockPos}. */
	private static BlockPos firstBrokenBlockPos;
	
	private static Cache<String, BlockPos> masterPositions = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();
	private static String masterPosKey(World world, BlockPos pos) {
		return world.provider.getDimension()  + ":" + pos;
	}


	public BlockTrackFake(){
		super(Material.IRON);
		this.setHardness(5.0F);
		this.setResistance(10.0F);
		this.setDefaultState(this.blockState.getBaseState().withProperty(height, 15));
		this.fullBlock = false;
	}


	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state){

		if(tryBreakTrackWhenBroken){

			BlockPos masterPos = getMasterPos(world, pos);
			if (masterPos != null) {
				firstBrokenBlockPos = pos;
				world.setBlockToAir(masterPos);
				firstBrokenBlockPos = null;
			}
		}
		masterPositions.put(masterPosKey(world, pos),  null);
		super.breakBlock(world, pos, state);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, height);
	}

	@Override
	@Deprecated
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return heightBoxes[state.getValue(height)];
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}

	/**
	 * Getter for {@link #firstBrokenBlockPos}.
	 */
	public static BlockPos getLastHitFakeTrack(){
		return firstBrokenBlockPos;
	}

	/**
	 * Tries to search for the position of the given fake block's master block.
	 * 
	 * @param world - The {@link net.minecraft.world.World World} object the block is in.
	 * @param thisPos - The block's {@link net.minecraft.util.math.BlockPos position}.
	 * @return The master block's position, or {@code null} if none was found.
	 */
	public static BlockPos getMasterPos(World world, final BlockPos thisPos) {
		String key = masterPosKey(world, thisPos);
		BlockPos cached = masterPositions.getIfPresent(key);
		if (cached != null) {
			return cached;
		}

		List<BlockPos> testedBlocks = new ArrayList<BlockPos>();
		List<BlockPos> blocksToTest = new ArrayList<BlockPos>();
		testedBlocks.add(thisPos);
		blocksToTest.add(thisPos);

		/* Run while the list isn't empty. */
		while (!blocksToTest.isEmpty()) {

			BlockPos testingPos = blocksToTest.get(0);
			for (EnumFacing searchOffset : EnumFacing.VALUES) {

				if (testedBlocks.contains(testingPos.offset(searchOffset))) {
					/* Block already tested from another block. Don't test it again. */
					continue;
				}
				else if (testingPos.offset(searchOffset).distanceSq(thisPos) > 150*150) {
					/* Block is too far to possibly be the master TE for this block. */
					continue;
				}

				if (world.getTileEntity(testingPos.offset(searchOffset)) instanceof TileEntityTrack) {

					/* Found a track TE. See if it's the parent for this fake track block. */
					TileEntity tile = world.getTileEntity(testingPos.offset(searchOffset));
					if (tile instanceof TileEntityTrack &&
							((TileEntityTrack) tile).getFakeTracks().contains(thisPos)) {
						masterPositions.put(key, testingPos.offset(searchOffset));
						return testingPos.offset(searchOffset);
					}
				}
				else if (world.getBlockState(testingPos.offset(searchOffset)).getBlock().equals(OFTRegistry.trackStructureFake)) {

					/* Found another fake track. Check to see if this has been tested and add to lists if not so. */
					if (!testedBlocks.contains(testingPos.offset(searchOffset))) {
						/* First make sure block hasn't been tested already. */
						if (!blocksToTest.contains(testingPos.offset(searchOffset))) {
							/* If the blocks to test from don't contain this block, add it now. */
							blocksToTest.add(testingPos.offset(searchOffset));
						}
					}
				}
			}

			/* End of the facing loop for this block. Set block as tested and remove from the toTestFrom list. */
			blocksToTest.remove(testingPos);
			testedBlocks.add(testingPos);
		}

		return null;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(height);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player){

		BlockPos masterPos = getMasterPos(world, pos);
		if (masterPos != null) {
			IBlockState stateMaster = world.getBlockState(masterPos);
			return stateMaster.getBlock().getPickBlock(stateMaster, target, world, masterPos, player);
		}

		return null;
	}

	//Deprecated, but correct so say master modders.
	@Override
	@Deprecated
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(height, meta);
	}

	/**
	 * Initialises an array of default bounding boxes for each metadata and returns it.
	 */
	private static AxisAlignedBB[] initHeightBoxes() {
		AxisAlignedBB[] heightBoxes = new AxisAlignedBB[16];
		for(byte i=0; i<16; ++i){
			heightBoxes[i] = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, (i+1)/16F, 1.0F);
		}
		return heightBoxes;
	}

	@Override
	@Deprecated
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	@Deprecated
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	/**
	 * Enable or disable main track breakage. Effectively sets the {@link #tryBreakTrackWhenBroken} flag.
	 */
	public static void toggleMainTrackBreakage(boolean enable) {
		tryBreakTrackWhenBroken = enable;
	}


	@Override
	public Vec3d getNextPosition(World world, BlockPos pos, Vec3d currentPosition, Vec3d motion) {
		BlockPos master = getMasterPos(world, pos);
		if (master == null) {
			return null;
		}
		return OFTRegistry.trackStructure.getNextPosition(world, master, currentPosition, motion);
	}


	@Override
	public double getTrackGauge(World world, BlockPos pos) {
		BlockPos master = getMasterPos(world, pos);
		if (master == null) {
			return Gauges.STANDARD;
		}
		return OFTRegistry.trackStructure.getTrackGauge(world, master);
	}
}
