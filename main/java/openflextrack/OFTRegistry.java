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
	
	public static final Item track = new Item();
	public static final Block trackStructure = new BlockTrackStructure();
	public static final Block trackStructureFake = new BlockTrackStructureFake();
	public static final Block surveyFlag = new BlockSurveyFlag();
		
	/**All run-time things go here.**/
	public void init(){
		initItems();
		initBlocks();
		initPackets();
	}

	private void initItems(){
		for(Field field : this.getClass().getFields()){
			if(field.getType().equals(Item.class)){
				try{
					Item item = (Item) field.get(Item.class);
					if(item.getUnlocalizedName().equals("item.null")){
						item.setUnlocalizedName(field.getName().toLowerCase());
					}
					registerItem(item);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	private void initBlocks(){
		for(Field field : this.getClass().getFields()){
			if(field.getType().equals(Block.class)){
				try{
					registerBlock((Block) field.get(Block.class));
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Registers the given item.
	 * @param item
	 */
	private static void registerItem(Item item){
		String registryName = item.getUnlocalizedName().split("\\.")[1].toLowerCase();
		GameRegistry.register(item.setRegistryName(registryName));
	}
	
	/**x
	 * Registers the given block and adds it to the creative tab list.
	 * Also adds the respective TileEntity if the block has one.
	 * @param block
	 */
	private static void registerBlock(Block block){
		String name = block.getClass().getSimpleName().toLowerCase().substring(5);
		GameRegistry.register(block.setRegistryName(name).setUnlocalizedName(name));
		if(block.getCreativeTabToDisplayOn() != null){
			GameRegistry.register(new ItemBlock(block).setRegistryName(name));
		}
		if(block instanceof ITileEntityProvider){
			Class<? extends TileEntity> tileEntityClass = ((ITileEntityProvider) block).createNewTileEntity(null, 0).getClass();
			GameRegistry.registerTileEntity(tileEntityClass, tileEntityClass.getSimpleName());
		}
	}
	
	private void initPackets(){
		int packetNumber = 0;
		OFT.OFTNet.registerMessage(ChatPacket.Handler.class, ChatPacket.class, ++packetNumber, Side.CLIENT);
		OFT.OFTNet.registerMessage(TileEntityClientRequestDataPacket.Handler.class, TileEntityClientRequestDataPacket.class, ++packetNumber, Side.SERVER);
		OFT.OFTNet.registerMessage(TileEntitySyncPacket.Handler.class, TileEntitySyncPacket.class, ++packetNumber, Side.CLIENT);
		OFT.OFTNet.registerMessage(TileEntitySyncPacket.Handler.class, TileEntitySyncPacket.class, ++packetNumber, Side.SERVER);
	}
}
