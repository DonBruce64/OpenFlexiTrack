package openflextrack;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**Contains registration methods used by {@link OFTRegistry} and methods overridden by ClientProxy. 
 * See the latter for more info on overridden methods.
 * 
 * @author don_bruce
 */
public class CommonProxy{
	public void preInit(FMLPreInitializationEvent event){}
	
	public void init(FMLInitializationEvent event){
		OFTRegistry.instance.init();
	}
}
