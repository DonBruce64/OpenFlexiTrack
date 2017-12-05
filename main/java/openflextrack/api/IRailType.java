package openflextrack.api;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import openflextrack.api.util.Vec3f;

/**
 * An interface that allows custom rail types.
 * 
 * @author Leshuwa Kaiheiwa
 */
public interface IRailType {

	/**
	 * Return a two-dimensional array, each sub-array holding four {@link openflextrack.api.util.Vec3f vertices} which contain following data;<br>
	 * <br>
	 * {@link openflextrack.api.util.Vec3f#x x} - Vertex X<br>
	 * {@link openflextrack.api.util.Vec3f#y y} - Vertex Y<br>
	 * {@link openflextrack.api.util.Vec3f#z z} - Texture Y<br>
	 * <br>
	 * <i>Please note:<br>
	 * 1. Vertex coordinates are described as seen when looking at the end cap of a rail,
	 * relative to the center of a track. This means that a rail shape can be drawn from
	 * several lines on a 2D Cartesian coordinate grid, where each line's start and end
	 * correspond to one vertex each.<br>
	 * 2. There is no texture X coordinate since it will be calculated dynamically
	 * during rendering routines to match patterns between rail sections.
	 * </i>
	 */
	@SideOnly(Side.CLIENT)
	Vec3f[][] getRailVertices();

	/**
	 * Return a texture for the rails.
	 */
	@SideOnly(Side.CLIENT)
	ResourceLocation getTexture();
	
	/**
	 * Return the value used to scale vertex positions to their respective
	 * {@link openflextrack.api.IRailType#getTexture() texture} coordinates.<br>
	 * <br>
	 * <i>
	 * Texture scale can be calculated from the number of vertices in one meter
	 * divided by the texture's Y-size.
	 * </i>
	 */
	@SideOnly(Side.CLIENT)
	float getTextureScale();
}