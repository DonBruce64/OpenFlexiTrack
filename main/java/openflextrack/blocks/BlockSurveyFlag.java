package openflextrack.blocks;

import java.util.HashMap;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import openflextrack.OFT;
import openflextrack.OFTRegistry;
import openflextrack.packets.ChatPacket;
import openflextrack.util.BlockPosDim;

/**
 * Survey flag block. Handles flag linkage.
 * 
 * @author don_bruce
 */
public class BlockSurveyFlag extends BlockRotateable {

	/** Default block bounding box. */
	private static final AxisAlignedBB blockBox = new AxisAlignedBB(0.4375F, 0.0F, 0.4375F, 0.5625F, 1.0F, 0.5625F);

	/** A map to keep track of initial block positions, used on server worlds. */
	private static final HashMap<EntityPlayer, BlockPosDim> firstPosition = new HashMap<EntityPlayer, BlockPosDim>();


	public BlockSurveyFlag() {
		super(Material.WOOD);
		this.setHardness(0.0F);
	}


	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state){

		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileEntitySurveyFlag) {
			((TileEntitySurveyFlag) tile).clearFlagLinking();
		}

		super.breakBlock(world, pos, state);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		return new TileEntitySurveyFlag();
	}

	@Override
	@Deprecated
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		return blockBox;
	}

	/**
	 * Counts the amount of items in the given player inventory.
	 * 
	 * @param inv - {@link net.minecraft.entity.player.InventoryPlayer Player inventory} to count in.
	 * @param item - {@link net.minecraft.item.Item Item type} to remove.
	 * @param meta - Item metadata; set to {@code -1} to disregard item metadata.
	 * @return The number of items of given type.
	 */
	private static int getItemsPlayerHas(InventoryPlayer inv, Item item, int meta){

		int qty = 0;
		for (ItemStack stack : inv.mainInventory) {

			if(stack == null){
				continue;
			}

			if(stack.getItem().equals(item)){
				if(stack.getItemDamage() == meta || meta == -1){
					qty += stack.stackSize;
				}
			}
		}

		return qty;
	}

	/**
	 * Tries to connect two flags either by creating a link with fake tracks,
	 * or by actually applying tracks to an existing link.
	 * 
	 * @param world - The {@link net.minecraft.world.World world} reference.
	 * @param pos - The {@link net.minecraft.util.math.BlockPos position} of the first flag.
	 * @param player - The {@link net.minecraft.entity.player.EntityPlayer player} who is trying to create the link.
	 */
	private static void linkFlags(World world, BlockPos pos, EntityPlayer player){

		/*
		 * Don't run on client worlds.
		 */
		if (world.isRemote) {
			return;
		}

		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileEntitySurveyFlag)) {
			return;
		}

		TileEntitySurveyFlag tileFlag = (TileEntitySurveyFlag) tile;

		/*
		 * If the player holds track items in their hands, try creating the track. 
		 */
		if (!player.isSneaking() && player.getHeldItemMainhand() != null &&
				OFTRegistry.track.equals(player.getHeldItemMainhand().getItem())) {

			/* If there is no link, notify the player about failure and don't do anything. */
			if (tileFlag.linkedCurve == null) {
				OFT.OFTNet.sendTo(new ChatPacket("interact.flag.failure.nolink"), (EntityPlayerMP) player);
				return;
			}

			/* If the player doesn't have enough items, notify the player and don't do anything. */
			final int trackLength = MathHelper.ceiling_float_int(tileFlag.linkedCurve.pathLength);
			if (!player.capabilities.isCreativeMode) {
				if (getItemsPlayerHas(player.inventory, OFTRegistry.track, -1) < trackLength) {//TODO The item metadata in this method call (and a few lines below) could be a hook for other mods to use.
					OFT.OFTNet.sendTo(new ChatPacket("interact.flag.failure.materials", " " + String.valueOf(trackLength)), (EntityPlayerMP) player);
					return;
				}
			}

			/* Tries to actually spawn the track. If blockingPos is non-null, indicates there is something occupying construction. */
			BlockPos blockingPos = tileFlag.spawnDummyTracks();
			if (blockingPos != null) {
				OFT.OFTNet.sendTo(new ChatPacket("interact.flag.failure.blockage", " X:" + blockingPos.getX() + " Y:" + blockingPos.getY() + " Z:" + blockingPos.getZ()), (EntityPlayerMP) player);
			}
			else if (!player.capabilities.isCreativeMode) {
				removeItemsFromPlayer(player.inventory, OFTRegistry.track, -1, trackLength);
			}

			return;
		}

		/*
		 * If there is a position for the given player known, try making a connection between two points.
		 */
		BlockPosDim posDim = firstPosition.get(player);
		if (posDim != null) {

			/* Make sure dimensions match. */
			if(posDim.dim != world.provider.getDimension()) {
				OFT.OFTNet.sendTo(new ChatPacket("interact.flag.failure.dimension"), (EntityPlayerMP) player);
				resetMaps(player);
				return;
			}

			/* If positions match, clear the position off the cache. */
			if(posDim.equals(pos)){
				OFT.OFTNet.sendTo(new ChatPacket("interact.flag.info.clear"), (EntityPlayerMP) player);
				resetMaps(player);
				return;
			}

			/* If distance is greater than 128 blocks, don't connect. */
			if( posDim.distanceSq(pos) > 128*128 ){
				OFT.OFTNet.sendTo(new ChatPacket("interact.flag.failure.distance"), (EntityPlayerMP) player);
				resetMaps(player);
				return;
			}

			/* Make sure flag has not been removed since linking, otherwise clear cache entry. */
			TileEntity tile2 = world.getTileEntity(posDim); 
			if (tile2 instanceof TileEntitySurveyFlag) {

				TileEntitySurveyFlag firstFlag = (TileEntitySurveyFlag) tile2;
				TileEntitySurveyFlag secondFlag = tileFlag;//I know this is redundant, but it enhances readability.
				firstFlag.linkToFlag(pos);
				secondFlag.linkToFlag(posDim);
				OFT.OFTNet.sendTo(new ChatPacket("interact.flag.info.link"), (EntityPlayerMP) player);
				resetMaps(player);
			}
			else {

				OFT.OFTNet.sendTo(new ChatPacket("interact.flag.info.clear"), (EntityPlayerMP) player);
				resetMaps(player);
			}
		}
		else {

			/*
			 * Otherwise set the new position in the cache and notify the player about it.
			 */
			firstPosition.put(player, new BlockPosDim(pos, world.provider.getDimension()));
			OFT.OFTNet.sendTo(new ChatPacket("interact.flag.info.set"), (EntityPlayerMP) player);
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			@Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ){

		linkFlags(world, pos, player);
		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack){

		super.onBlockPlacedBy(world, pos, state, entity, stack);
		if(entity instanceof EntityPlayer){
			linkFlags(world, pos, (EntityPlayer) entity);
		}
	}

	/**
	 * Removes the given amount of items from the given player inventory. Scans through multiple inventory slots if needed.
	 * 
	 * @param inv - {@link net.minecraft.entity.player.InventoryPlayer Player inventory} to interact with.
	 * @param item - {@link net.minecraft.item.Item Item type} to remove.
	 * @param meta - Item metadata; set to {@code -1} to disregard item metadata.
	 * @param qty - Number of items to remove.
	 */
	private static void removeItemsFromPlayer(InventoryPlayer inv, Item item, int meta, int qty){

		for(int i=0; i<inv.mainInventory.length; ++i){

			ItemStack stack = inv.mainInventory[i];
			if (stack == null) {
				continue;
			}

			if(stack.getItem().equals(item)){
				if(stack.getItemDamage() == meta || meta == -1){
					if(stack.stackSize <= qty){
						qty -= stack.stackSize;
						inv.removeStackFromSlot(i);
					}else{
						stack.stackSize -= qty;
						inv.setInventorySlotContents(i, stack);
						return;
					}
				}
			}
		}
	}

	/**
	 * Called to reset entries in {@link #firstPosition} and {@link #firstDimension} for the given player reference. 
	 */
	private static final void resetMaps(EntityPlayer player){
		firstPosition.remove(player);
	}
}
