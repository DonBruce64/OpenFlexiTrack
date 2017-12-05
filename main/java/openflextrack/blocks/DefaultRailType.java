package openflextrack.blocks;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import openflextrack.OFT;
import openflextrack.api.IRailType;
import openflextrack.api.util.Vec3f;

/**
 * Default rail type class.
 */
public class DefaultRailType implements IRailType {

	/** Public reference to the default rail type. */
	public static final DefaultRailType DEFAULT_RAIL_TYPE = new DefaultRailType();

	/** Default track shape vertices. */
	@SideOnly(Side.CLIENT) private static Vec3f[][] vertices;
	/** Default track texture. */
	@SideOnly(Side.CLIENT) private static ResourceLocation railTexture;


	@Override
	@SideOnly(Side.CLIENT)
	public Vec3f[][] getRailVertices() {
		if (vertices == null) {
			vertices = new Vec3f[][] {
				/* Head */ {
					new Vec3f(15.0F/16F, 4F/16F, 17F/19F), 	/* topOuterX, topUpperY */
					new Vec3f(13.0F/16F, 4F/16F, 16F/19F), 	/* topInnerX, topUpperY */
					new Vec3f(13.0F/16F, 3F/16F,  8F/19F), 	/* topInnerX, topLowerY */
					new Vec3f(15.0F/16F, 3F/16F, 13F/19F), 	/* topOuterX, topLowerY */
				},
				/* Web */ {
					new Vec3f(14.5F/16F, 3F/16F, 6F/19F), 	/* midOuterX, topLowerY */
					new Vec3f(13.5F/16F, 3F/16F, 6F/19F), 	/* midInnerX, topLowerY */
					new Vec3f(13.5F/16F, 1F/16F, 6F/19F), 	/* midInnerX, botUpperY */
					new Vec3f(14.5F/16F, 1F/16F, 6F/19F), 	/* midOuterX, botUpperY */
				},
				/* Base */ {
					new Vec3f(16.5F/16F, 1F/16F, 8F/19F), 	/* botOuterX, botUpperY */
					new Vec3f(11.5F/16F, 1F/16F, 5F/19F), 	/* botInnerX, botUpperY */
					new Vec3f(11.5F/16F, 0F/16F, 0F/19F), 	/* botInnerX, botLowerY */
					new Vec3f(16.5F/16F, 0F/16F, 5F/19F), 	/* botOuterX, botLowerY */
				}
			};
		}
		return vertices;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ResourceLocation getTexture() {
		if (railTexture == null) {
			railTexture = new ResourceLocation(OFT.MODID, "textures/blockmodels/rail.png");
		}
		return railTexture;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getTextureScale() {
		return 16F/19F;
	}
}