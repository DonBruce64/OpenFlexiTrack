package openflextrack;

import java.lang.reflect.Field;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import openflextrack.blocks.BlockSurveyFlag;
import openflextrack.blocks.BlockTrackStructure;
import openflextrack.blocks.BlockTrackStructureFake;
import openflextrack.packets.ChatPacket;
import openflextrack.packets.TileEntityClientRequestDataPacket;
import openflextrack.packets.TileEntitySyncPacket;

/**Main registry class.  This class should be referenced by any class looking for
 * MTS items or blocks.  Adding new items and blocks is a simple as adding them
 * as a field; the init method automatically registers all items and blocks in the class
 * and orders them according to the order in which they were declared.
 * This calls the {@link PackParserSystem} to get the custom vehicles from there.
 * 
 * @author don_bruce
 */
public class OFTRegistry{
	public static final OFTRegistry instance = new OFTRegistry();
	
	public static final Item ties = new Item();
	public static final Item rails = new Item();
	public static final Item track = new Item();
	public static final Block trackStructure = new BlockTrackStructure();
	public static final Block trackStructureFake = new BlockTrackStructureFake();
	public static final Block surveyFlag = new BlockSurveyFlag();
		
	/**All run-time things go here.**/
	public void init(){
		registerItem(ties, "ties");
		registerItem(rails, "rails");
		registerItem(track, "track");
		registerBlock(trackStructure, true);
		registerBlock(trackStructureFake, false);
		registerBlock(surveyFlag, true);
		
		int packetNumber = 0;
		OFT.OFTNet.registerMessage(ChatPacket.Handler.class, ChatPacket.class, ++packetNumber, Side.CLIENT);
		OFT.OFTNet.registerMessage(TileEntityClientRequestDataPacket.Handler.class, TileEntityClientRequestDataPacket.class, ++packetNumber, Side.SERVER);
		OFT.OFTNet.registerMessage(TileEntitySyncPacket.Handler.class, TileEntitySyncPacket.class, ++packetNumber, Side.CLIENT);
		OFT.OFTNet.registerMessage(TileEntitySyncPacket.Handler.class, TileEntitySyncPacket.class, ++packetNumber, Side.SERVER);
	}

	/**
	 * Registers the given item.
	 */
	private static void registerItem(Item item, String itemName){
		GameRegistry.register(item.setRegistryName(itemName).setUnlocalizedName(itemName));
	}
	
	/**x
	 * Registers the given block.
	 * Also adds the respective TileEntity if the block has one.
	 */
	private static void registerBlock(Block block, boolean registerItemBlock){
		String name = block.getClass().getSimpleName().toLowerCase().substring(5);
		if(block.getRegistryName() == null){
			GameRegistry.register(block.setRegistryName(name).setUnlocalizedName(name));
			if(block instanceof ITileEntityProvider){
				Class<? extends TileEntity> tileEntityClass = ((ITileEntityProvider) block).createNewTileEntity(null, 0).getClass();
				GameRegistry.registerTileEntity(tileEntityClass, tileEntityClass.getSimpleName());
			}
		}
		if(registerItemBlock && Item.getItemFromBlock(block) == null){
			GameRegistry.register(new ItemBlock(block).setRegistryName(name));
		}
	}
}
