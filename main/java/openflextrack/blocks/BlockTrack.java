package openflextrack.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import openflextrack.OFTRegistry;

public class BlockTrack extends BlockRotateable {

	/** Default block collision box. */
	private static final AxisAlignedBB blockBox = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);


	public BlockTrack(){
		super(Material.IRON);
		this.setHardness(5.0F);
		this.setResistance(10.0F);
	}


	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {

		/*
		 * On server worlds before breaking the track, make sure the connection exists and count the length (in meters).
		 * Then drop track items.
		 */
		if (!world.isRemote) {

			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileEntityTrack) {

				TileEntityTrack track = (TileEntityTrack) tile;
				if (track.curve != null) {

					tile = world.getTileEntity(pos.add(track.curve.endPos));
					if (tile instanceof TileEntityTrack) {

						/* Compute number of items depending on track length. */
						int numberTracks = MathHelper.ceiling_float_int(track.curve.pathLength);
						while (numberTracks > 0) {

							// Prepare ItemStack.
							int tracksInItem = Math.min(numberTracks, 64);
							ItemStack itemStack = new ItemStack(OFTRegistry.track, tracksInItem, this.getMetaFromState(state));
							EntityItem entityItem;

							// Instantiate EntityItem depending on last (fake) position.
							BlockPos lastFake = BlockTrackFake.getLastHitFakeTrack();
							if (lastFake != null) {
								entityItem = new EntityItem(world, lastFake.getX(), lastFake.getY(), lastFake.getZ(), itemStack);
							}
							else {
								entityItem = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), itemStack);
							}

							// Spawn EntityItem, reduce items.
							world.spawnEntityInWorld(entityItem);
							numberTracks -= tracksInItem;
						}

						/* Remove other track tile. */
						track.removeFakeTracks();
						super.breakBlock(world, pos, state);
						world.setBlockToAir(((TileEntityTrack) tile).getPos());
						return;
					}

				}
			}
		}

		super.breakBlock(world, pos, state);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		return new TileEntityTrack();
	}

	@Override
	@Deprecated
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		return blockBox;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player){
		//TODO Add track sub-types here as properties and return meta for item damage.
		return new ItemStack(OFTRegistry.track);
	}
}
