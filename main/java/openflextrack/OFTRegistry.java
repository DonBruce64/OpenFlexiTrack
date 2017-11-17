package openflextrack;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import openflextrack.blocks.BlockSurveyFlag;
import openflextrack.blocks.BlockTrack;
import openflextrack.blocks.BlockTrackFake;
import openflextrack.packets.ChatPacket;
import openflextrack.packets.TileEntityClientReqPacket;
import openflextrack.packets.TileEntitySyncPacket;

/**
 * Main registry class. This class should be referenced by any class looking for
 * OFT items or blocks.<br>
 * <br>
 * Adding new items and blocks is a simple as adding them as a field;
 * the {@link #init()} method automatically registers all items and blocks in the class
 * and orders them according to the order in which they were declared.
 * 
 * @author don_bruce
 */
public class OFTRegistry {

	/*
	 * Registry instance.
	 */
	public static final OFTRegistry instance = new OFTRegistry();

	/*
	 * Item fields.
	 */
	public static final Item ties = new Item();
	public static final Item rails = new Item();
	public static final Item track = new Item();

	/*
	 * Block fields.
	 */
	public static final Block trackStructure = new BlockTrack();
	public static final Block trackStructureFake = new BlockTrackFake();
	public static final Block surveyFlag = new BlockSurveyFlag();

	/**
	 * All run-time things go here.
	 */
	public void init() {

		registerItem(ties, "ties");
		registerItem(rails, "rails");
		registerItem(track, "track");
		registerBlock(trackStructure, false);
		registerBlock(trackStructureFake, false);
		registerBlock(surveyFlag, true);

		int packetNumber = 0;
		OFT.OFTNet.registerMessage(ChatPacket.Handler.class, ChatPacket.class, ++packetNumber, Side.CLIENT);
		OFT.OFTNet.registerMessage(TileEntityClientReqPacket.Handler.class, TileEntityClientReqPacket.class, ++packetNumber, Side.SERVER);
		OFT.OFTNet.registerMessage(TileEntitySyncPacket.Handler.class, TileEntitySyncPacket.class, ++packetNumber, Side.CLIENT);
		OFT.OFTNet.registerMessage(TileEntitySyncPacket.Handler.class, TileEntitySyncPacket.class, ++packetNumber, Side.SERVER);
	}

	/**
	 * Registers the given item with the given name and appends the mod ID before the item's name.
	 */
	private static void registerItem(Item item, String name) {//TODO REGISTRY - Register item names with "oft." -prefix to avoid name clash between mods.

		GameRegistry.register(item.setRegistryName(name).setUnlocalizedName(name));
	}

	/**
	 * Registers the given block.
	 * Also adds the respective TileEntity if the block has one.
	 * 
	 * @param registerItemBlock - {@code true} to also register the block as item.
	 */
	private static void registerBlock(Block block, boolean registerItemBlock) {//TODO REGISTRY - Register block names with "oft." -prefix to avoid name clash between mods.

		/* Determine block's unlocalised name. */
		String name = block.getClass().getSimpleName().toLowerCase().substring(5);

		/* Register block (and its TileEntity, if existent). */
		if (block.getRegistryName() == null) {
			GameRegistry.register(block.setRegistryName(name).setUnlocalizedName(name));

			if(block instanceof ITileEntityProvider){
				Class<? extends TileEntity> tileEntityClass = ((ITileEntityProvider) block).createNewTileEntity(null, 0).getClass();
				GameRegistry.registerTileEntity(tileEntityClass, tileEntityClass.getSimpleName());
			}
		}

		/* Register block's item if required. */
		if (registerItemBlock && Item.getItemFromBlock(block) == null) {
			GameRegistry.register(new ItemBlock(block).setRegistryName(name));
		}
	}
}
