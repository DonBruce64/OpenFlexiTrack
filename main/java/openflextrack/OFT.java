package openflextrack;

import java.io.File;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(modid = OFT.MODID, name = OFT.MODNAME, version = OFT.MODVER)
public class OFT {
	public static final String MODID="oft";
	public static final String MODNAME="Open Flexi-Track";
	public static final String MODVER="1.0.0";
	public static final String assetDir = System.getProperty("user.dir") + File.separator + OFT.MODID;

	@Instance(value = OFT.MODID)
	public static OFT instance;
	public static final SimpleNetworkWrapper OFTNet = NetworkRegistry.INSTANCE.newSimpleChannel("OFTNet");
	@SidedProxy(clientSide="openflextrack.ClientProxy", serverSide="openflextrack.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void PreInit(FMLPreInitializationEvent event){
		proxy.preInit(event);
		//TODO I've heard reports of this crashing Linux machines.  Should probably put it into a mcmeta file or whatever it's supposed to go into.
		//this.initModMetadata(event);
	}

	@EventHandler
	public void Init(FMLInitializationEvent event){
		proxy.init(event);
	}

	//	private void initModMetadata(FMLPreInitializationEvent event){
	//        ModMetadata meta = event.getModMetadata();
	//        meta.name = MODNAME;
	//        meta.description = "An Open-Source Flexible Track System.";
	//        meta.authorList.clear();
	//        meta.authorList.add("don_bruce");
	//        meta.logoFile = "";
	//        meta.url = "";
	//        meta.modId = MODID;
	//        meta.version = MODVER;
	//	}
}
