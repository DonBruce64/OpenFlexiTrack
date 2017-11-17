package openflextrack.api;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * An interface that allows custom rail types.
 * 
 * @author Leshuwa Kaiheiwa
 */
public interface IRailType {

	/**
	 * Return an array of <b>10</b> float values, containing (in order) following vertices;<br>
	 * <br>
	 * <b>Rail Base:</b><br>
	 * > InnerX<br>
	 * > OuterX<br>
	 * > LowerY<br>
	 * > UpperY<br>
	 * <br>
	 * <b>Rail Web:</b><br>
	 * > InnerX<br>
	 * > OuterX<br>
	 * <br>
	 * <b>Rail Head:</b><br>
	 * > InnerX<br>
	 * > OuterX<br>
	 * > LowerY<br>
	 * > UpperY<br>
	 */
	@SideOnly(Side.CLIENT)
	float[] getRailVertices();

	/**
	 * Return a texture for the rails.
	 */
	@SideOnly(Side.CLIENT)
	ResourceLocation getTexture();
}