package openflextrack.blocks;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import openflextrack.OFT;
import openflextrack.api.ISleeperType;
import openflextrack.rendering.blockmodels.ModelTrackTie;

/**
 * Default sleeper type class.
 */
public class DefaultSleeperType implements ISleeperType {

	/** Public reference to the default sleeper type. */
	public static final DefaultSleeperType DEFAULT_SLEEPER_TYPE = new DefaultSleeperType();

	/** Default track texture. */
	@SideOnly(Side.CLIENT) private static ResourceLocation tieTexture;
	/** Default tie model. */
	@SideOnly(Side.CLIENT) private static ModelTrackTie modelTie;


	@Override
	public float getOffset() {
		return 0.65F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ResourceLocation getTexture() {
		if (tieTexture == null) {
			tieTexture = new ResourceLocation(OFT.MODID, "textures/blockmodels/tie.png");
		}
		return tieTexture;
	}

	@Override
	public void render(float width) {//TODO @don_bruce RENDER - Render ties of dynamic width here. May require more data from the rendering pipeline.
		if (modelTie == null) {
			modelTie = new ModelTrackTie();
		}
		modelTie.render();
	}
}