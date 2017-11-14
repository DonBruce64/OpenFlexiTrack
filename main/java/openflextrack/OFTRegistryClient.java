package openflextrack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import openflextrack.blocks.TileEntitySurveyFlag;
import openflextrack.blocks.TileEntityTrackStructure;
import openflextrack.rendering.blockrenders.RenderSurveyFlag;
import openflextrack.rendering.blockrenders.RenderTrack;

/**
 * Registers client-sided things, such as tile entity renderers and item models.
 * 
 * @author don_bruce
 */
@SideOnly(Side.CLIENT)
public class OFTRegistryClient {

	/**
	 * Called during Forge's pre-initialisation stage to register
	 * {@link net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer TESRs}.
	 */
	public static void preInit(){
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySurveyFlag.class, new RenderSurveyFlag());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTrackStructure.class, new RenderTrack());
	}

	/**
	 * Called during Forge's main initialisation stage to register item models.
	 */
	public static void init(){
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		mesher.register(OFTRegistry.ties, 0, new ModelResourceLocation(OFT.MODID + ":" + "ties", "inventory"));
		mesher.register(OFTRegistry.rails, 0, new ModelResourceLocation(OFT.MODID + ":" + "rails", "inventory"));
		mesher.register(OFTRegistry.track, 0, new ModelResourceLocation(OFT.MODID + ":" + "track", "inventory"));
		mesher.register(Item.getItemFromBlock(OFTRegistry.surveyFlag), 0, new ModelResourceLocation(OFT.MODID + ":" + "surveyflag", "inventory"));
	}
}
