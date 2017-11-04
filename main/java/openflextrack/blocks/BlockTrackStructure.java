package openflextrack.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import openflextrack.OFTRegistry;

public class BlockTrackStructure extends BlockRotateable{
	private static final AxisAlignedBB blockBox = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
	
	public BlockTrackStructure(){
		super(Material.IRON);
		this.setHardness(5.0F);
		this.setResistance(10.0F);
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player){
		//TODO add track sub-types here as properties and return meta for item damage.
        return new ItemStack(OFTRegistry.track);
    }
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state){
		TileEntityTrackStructure track = (TileEntityTrackStructure) world.getTileEntity(pos);
		if(track != null){
			if(track.curve != null){
				if(!world.isRemote){
					TileEntityTrackStructure otherEnd = (TileEntityTrackStructure) world.getTileEntity(pos.add(track.curve.endPos));
					if(otherEnd != null){
						int numberTracks = (int) track.curve.pathLength;
						while(numberTracks > 0){
							int tracksInItem = Math.min(numberTracks, 64);
							if(BlockTrackStructureFake.getLastHitFakeTrack() != null){
								world.spawnEntityInWorld(new EntityItem(world, BlockTrackStructureFake.getLastHitFakeTrack().getX(), BlockTrackStructureFake.getLastHitFakeTrack().getY(), BlockTrackStructureFake.getLastHitFakeTrack().getZ(), new ItemStack(OFTRegistry.track, tracksInItem, this.getMetaFromState(state))));
							}else{
								world.spawnEntityInWorld(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(OFTRegistry.track, tracksInItem, this.getMetaFromState(state))));
							}
							numberTracks -= tracksInItem;
						}
						track.removeFakeTracks();
						super.breakBlock(world, pos, state);
						world.setBlockToAir(otherEnd.getPos());
						return;
					}
				}
			}
		}
		super.breakBlock(world, pos, state);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		return blockBox;
	}

	@Override
	public TileEntityRotatable getTileEntity(){
		return new TileEntityTrackStructure();
	}
}
