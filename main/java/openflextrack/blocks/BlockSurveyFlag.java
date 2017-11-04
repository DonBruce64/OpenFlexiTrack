package openflextrack.blocks;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import openflextrack.OFT;
import openflextrack.OFTRegistry;
import openflextrack.packets.ChatPacket;

public class BlockSurveyFlag extends BlockRotateable{
	private static final AxisAlignedBB blockBox = new AxisAlignedBB(0.4375F, 0.0F, 0.4375F, 0.5625F, 1.0F, 0.5625F);
	private static final Map<EntityPlayer, BlockPos> firstPosition = new HashMap<EntityPlayer, BlockPos>();
	private static final Map<EntityPlayer, Integer> firstDimension = new HashMap<EntityPlayer, Integer>();
	
	public BlockSurveyFlag(){
		super(Material.WOOD);
		this.setHardness(2.0F);
		this.setResistance(10.0F);
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack){
		super.onBlockPlacedBy(world, pos, state, entity, stack);
		if(entity instanceof EntityPlayer){
			linkFlags(world, pos, (EntityPlayer) entity);
		}
	}
	
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ){
		linkFlags(world, pos, player);
		return true;
	}
	
	private void linkFlags(World world, BlockPos pos, EntityPlayer player){
		if(!world.isRemote){
			TileEntitySurveyFlag tile = (TileEntitySurveyFlag) world.getTileEntity(pos);
			if(!player.isSneaking() && player.getHeldItemMainhand() != null){
				if(OFTRegistry.track.equals(player.getHeldItemMainhand().getItem())){
					if(tile.linkedCurve != null){
						final int trackLength = Math.round(tile.linkedCurve.pathLength);
						if(!player.capabilities.isCreativeMode){
							if(getQtyOfItemPlayerHas(player, OFTRegistry.track, -1) < trackLength){
								OFT.OFTNet.sendTo(new ChatPacket("interact.flag.failure.materials", " " + String.valueOf((int) Math.round(tile.linkedCurve.pathLength))), (EntityPlayerMP) player);
								return;
							}
						}
						BlockPos blockingPos = tile.spawnDummyTracks();
						if(blockingPos != null){
							OFT.OFTNet.sendTo(new ChatPacket("interact.flag.failure.blockage", " X:" + blockingPos.getX() + " Y:" + blockingPos.getY() + " Z:" + blockingPos.getZ()), (EntityPlayerMP) player);
						}else{
							if(!player.capabilities.isCreativeMode){
								removeQtyOfItemsFromPlayer(player, OFTRegistry.track, -1, trackLength);
							}
						}
					}else{
						OFT.OFTNet.sendTo(new ChatPacket("interact.flag.failure.nolink"), (EntityPlayerMP) player);
					}
					return;
				}
			}
			if(firstPosition.containsKey(player)){
				if(firstDimension.get(player) != world.provider.getDimension()){
					OFT.OFTNet.sendTo(new ChatPacket("interact.flag.failure.dimension"), (EntityPlayerMP) player);
					resetMaps(player);
				}else if(firstPosition.get(player).equals(pos)){
					OFT.OFTNet.sendTo(new ChatPacket("interact.flag.info.clear"), (EntityPlayerMP) player);
					resetMaps(player);
				}else if(Math.sqrt(firstPosition.get(player).distanceSq(pos)) > 128){
					OFT.OFTNet.sendTo(new ChatPacket("interact.flag.failure.distance"), (EntityPlayerMP) player);
					resetMaps(player);
				}else{
					//Make sure flag has not been removed since linking.
					if(world.getTileEntity(firstPosition.get(player)) instanceof TileEntitySurveyFlag){
						TileEntitySurveyFlag firstFlag = (TileEntitySurveyFlag) world.getTileEntity(firstPosition.get(player));
						TileEntitySurveyFlag secondFlag = (TileEntitySurveyFlag) world.getTileEntity(pos);
						firstFlag.linkToFlag(pos);
						secondFlag.linkToFlag(firstPosition.get(player));
						OFT.OFTNet.sendTo(new ChatPacket("interact.flag.info.link"), (EntityPlayerMP) player);
						resetMaps(player);
					}else{
						OFT.OFTNet.sendTo(new ChatPacket("interact.flag.info.clear"), (EntityPlayerMP) player);
						resetMaps(player);
					}
				}
			}else{
				firstPosition.put(player, pos);
				firstDimension.put(player, world.provider.getDimension());
				OFT.OFTNet.sendTo(new ChatPacket("interact.flag.info.set"), (EntityPlayerMP) player);
			}
		}
	}
	
	private int getQtyOfItemPlayerHas(EntityPlayer player, Item item, int meta){
		int qty = 0;
		for(ItemStack stack : player.inventory.mainInventory){
			if(stack != null){
				if(stack.getItem().equals(item)){
					if(stack.getItemDamage() == meta || meta == -1){
						qty+=stack.stackSize;
					}
				}
			}
		}
		return qty;
	}
	
	private void removeQtyOfItemsFromPlayer(EntityPlayer player, Item item, int meta, int amountToRemove){
		for(int i=0; i<player.inventory.mainInventory.length; ++i){
			ItemStack stack = player.inventory.mainInventory[i];
			if(stack != null){
				if(stack.getItem().equals(item)){
					if(stack.getItemDamage() == meta || meta == -1){
						if(stack.stackSize <= amountToRemove){
							amountToRemove -= stack.stackSize;
							player.inventory.removeStackFromSlot(i);
						}else{
							stack.stackSize -= amountToRemove;
							player.inventory.setInventorySlotContents(i, stack);
							return;
						}
					}
				}
			}
		}
	}
	
	private static final void resetMaps(EntityPlayer player){
		firstPosition.remove(player);
		firstDimension.remove(player);
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state){
		((TileEntitySurveyFlag) world.getTileEntity(pos)).clearFlagLinking();
		super.breakBlock(world, pos, state);
	}
	
	
	@Override
	@SuppressWarnings("deprecation")
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		return blockBox;
	}

	@Override
	public TileEntityRotatable getTileEntity(){
		return new TileEntitySurveyFlag();
	}
}
