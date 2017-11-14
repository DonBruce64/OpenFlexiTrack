package openflextrack;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

/**
 * Main mod file. Handles Forge's initialisation events and other general mod stuff.
 * 
 * @author don_bruce
 */
@Mod(modid = OFT.MODID, name = OFT.MODNAME, version = OFT.MODVER)
public class OFT {

	public static final String MODID = "oft";
	public static final String MODNAME = "Open Flexi-Track";
	public static final String MODVER = "1.0.0";

	@Instance(value = OFT.MODID)
	public static OFT instance;

	@SidedProxy(clientSide="openflextrack.ClientProxy", serverSide="openflextrack.CommonProxy")
	public static CommonProxy proxy;

	public static final SimpleNetworkWrapper OFTNet = NetworkRegistry.INSTANCE.newSimpleChannel("OFTNet");

	/**
	 * Called during Forge's pre-initialisation stage to prepare mod initialisation.<br>
	 * <br>
	 * Runs client-sided registration as well as mod metadata population.
	 */
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit(event);
		initModMetadata(event.getModMetadata());
	}

	/**
	 * Called during Forge's main initialisation stage to initialise the mod.<br>
	 * <br>
	 * Runs registration of general mod content, e.g. items and blocks.
	 */
	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}

	/**
	 * Called to initialise the given mod metadata container with this mod's data.
	 * 
	 * @param meta - The {@link net.minecraftforge.fml.common.ModMetadata mod's metadata} reference to populate.
	 */
	private static void initModMetadata(ModMetadata meta) {
		//TODO I've heard reports of this crashing Linux machines. Should probably put it into a mcmeta file or whatever it's supposed to go into.
		//TODO Verify whether merely modifying the mod metadata is causing trouble, or whether there is something peculiar that is causing known issue.
		meta.name = MODNAME;
		meta.modId = MODID;
		meta.version = MODVER;
		meta.description = "An Open-Source Flexible Track System.";
		meta.authorList.add("don_bruce");
	}
}
