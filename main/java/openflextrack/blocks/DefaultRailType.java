package openflextrack.blocks;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import openflextrack.OFT;
import openflextrack.api.IRailType;

/**
 * Default rail type class.
 */
public class DefaultRailType implements IRailType {

	/** Public reference to the default rail type. */
	public static final DefaultRailType DEFAULT_RAIL_TYPE = new DefaultRailType();

	/** Default track shape vertices. */
	@SideOnly(Side.CLIENT) private static float[] vertices;
	/** Default track texture. */
	@SideOnly(Side.CLIENT) private static ResourceLocation railTexture;


	@Override
	@SideOnly(Side.CLIENT)
	public float[] getRailVertices() {
		if (vertices == null) {
			vertices = new float[] {
					/* Bottom (base) */
					11.5F/16F,
					16.5F/16F,
					0F/16F,
					1F/16F,

					/* Middle (web) */
					13.5F/16F,
					14.5F/16F,

					/* Upper (head) */
					13F/16F,
					15F/16F,
					3F/16F,
					4F/16F
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
}