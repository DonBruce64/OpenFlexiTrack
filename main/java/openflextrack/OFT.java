package openflextrack;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
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
}
