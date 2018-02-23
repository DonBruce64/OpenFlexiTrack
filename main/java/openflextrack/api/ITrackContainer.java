package openflextrack.api;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Interface used to pass track data, such as position and world information as well as curve and skin data.
 * 
 * @author Leshuwa Kaiheiwa
 */
public interface ITrackContainer {
	/**
	 * Return the {@link openflextrack.api.OFTCurve curve} associated with this track, may be {@code null}.
	 */
	@Nullable
	OFTCurve getCurve();

	/**
	 * Return the {@link openflextrack.api.IRailType rail type} of this track.
	 */
	IRailType getRailType();

	/**
	 * Return the {@link openflextrack.api.ISleeperType sleeper type} of this track.
	 */
	ISleeperType getSleeperType();

	/**
	 * Return {@code true} if this track is a hologram.
	 */
	boolean isHolographic();
}