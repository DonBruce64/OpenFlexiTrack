package openflextrack.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Abstract base class for blocks with rotation.
 * 
 * @author don_bruce
 */
public abstract class BlockRotateable extends BlockContainer{

	public BlockRotateable(Material material){
		super(material);
		this.fullBlock = false;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack){

		super.onBlockPlacedBy(world, pos, state, entity, stack);

		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileEntityRotatable) {

			float yaw = entity.rotationYaw;
			while(yaw < 0){
				yaw += 360;
			}

			((TileEntityRotatable) tile).rotation = (byte) (Math.round(yaw%360/45) % 8);
		}
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
}
