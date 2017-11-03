package openflextrack;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**Class responsible for performing client-only updates and operations.
 * Any version-updatable, client-based method should be put in here.
 * 
 * @author don_bruce
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy{
	
	@Override
	public void preInit(FMLPreInitializationEvent event){
		super.preInit(event);
		OFTRegistryClient.preInit();
	}
	
	@Override
	public void init(FMLInitializationEvent event){
		super.init(event);
		OFTRegistryClient.init();
	}
}
