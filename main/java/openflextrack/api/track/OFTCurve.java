package openflextrack.api.track;

import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.hypot;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import net.minecraft.util.math.BlockPos;
import openflextrack.util.Vec3f;

/**
 * Curve class used for track paths.<br>
 * <br>
 * Start point is at origin {@code (0, 0, 0)}. Offset as needed.<br>
 * Needs an {@link #endPos end point}, a {@link #startAngle start angle}, and an {@link #endAngle end angle}.<br>
 * <br>
 * Note that this uses Minecraft's coordinate system where 0 degrees is north, not east.
 * 
 * @author don_bruce
 */
public class OFTCurve {

	/** Determines the number of cached curve segments. */
	public static final byte CACHED_CURVE_INCREMENTS = 16;

	/** Start point's angle around global Y. */
	public final float startAngle;
	/** End point's angle around global Y. */
	public final float endAngle;
	/** Length of overall curve path. */
	public final float pathLength;
	/** End point's block coordinates. */
	public final BlockPos endPos;

	/** Distance between curve points {@link #startPoint} and {@link #endPoint} divided by {@code 3.0D}. */
	private final float cpDist;
	/** Local start point coordinates. */
	private final Vec3f startPoint;
	/** Local end point coordinates. */
	private final Vec3f endPoint;
	/** Bezier curve's start point. */
	private final Vec3f cpStart;
	/** Bezier curve's end point. */
	private final Vec3f cpEnd;

	/** Array holding cached points on the curve path. */
	private final Vec3f[] cachedPathPoints;


	/**
	 * Initialise a new curve ending at the given position with given start and end angles (in degrees).<br>
	 * Assumes start position to be origin (see class type JavaDoc), and an angle of {@code 0°} to point to the north.
	 * 
	 * @param ep - {@link net.minecraft.util.math.BlockPos End position}.
	 * @param sa - Start angle, in degrees.
	 * @param ea - End angle, in degrees.
	 */
	public OFTCurve(BlockPos ep, float sa, float ea) {

		/* Populate fields. */
		this.startAngle = sa;
		this.endAngle = ea;
		this.endPos = ep;
		this.startPoint = new Vec3f(0.5F, 0, 0.5F);
		this.endPoint = new Vec3f(ep.getX() + 0.5F, ep.getY(), ep.getZ() + 0.5F);
		this.cpDist = (float) (endPoint.distTo(startPoint) / 3.0D);

		this.cpStart = new Vec3f(
				(float) (startPoint.x - sin(toRadians(startAngle))*cpDist),
				startPoint.y,
				(float) (startPoint.z + cos(toRadians(startAngle))*cpDist));

		this.cpEnd = new Vec3f(
				(float) (endPoint.x - sin(toRadians(endAngle))*cpDist),
				endPoint.y,
				(float) (endPoint.z + cos(toRadians(endAngle))*cpDist));

		/* Compute path length. */
		this.pathLength = this.getPathLength();
		this.cachedPathPoints = this.getCachedPathPoints(new Vec3f[ round(pathLength*CACHED_CURVE_INCREMENTS) + 1 ]);
	}


	/**
	 * Called to populate the given array of {@link openflextrack.util.Vec3f path points} with this curve's data.
	 * 
	 * @return The populated array.
	 */
	private Vec3f[] getCachedPathPoints(Vec3f[] points) {

		boolean skipX = (startPoint.x == endPoint.x),
				skipY = (startPoint.y == endPoint.y),
				skipZ = (startPoint.z == endPoint.z);

		for (int i = 0; i < points.length; ++i) {

			float t = (float)i / points.length;
			points[i] = new Vec3f(
					getCachedPathPointVal(skipX, startPoint.x, cpStart.x, cpEnd.x, endPoint.x, t),
					getCachedPathPointVal(skipY, startPoint.y, cpStart.y, cpEnd.y, endPoint.y, t),
					getCachedPathPointVal(skipZ, startPoint.z, cpStart.z, cpEnd.z, endPoint.z, t)
					);
		}

		return points;
	}

	/**
	 * Helper method to determine a path point's value on a given axis.
	 */
	private static final float getCachedPathPointVal(boolean skip, float startPoint, float cpStart, float cpEnd, float endPoint, float t) {
		if (skip) {
			return startPoint;
		}

		return (float) (pow(1-t, 3)*startPoint + 3*pow(1-t, 2)*t*cpStart + 3*(1-t)*pow(t, 2)*cpEnd + pow(t, 3)*endPoint);
	}

	/**
	 * Returns the pitch rotation for the path point closest to the given position.
	 */
	public float getCachedPitchAngleAt(float segment){

		int point = round(segment*pathLength*CACHED_CURVE_INCREMENTS);
		if (point + 1 >= cachedPathPoints.length) {
			point = cachedPathPoints.length - 2;
		}

		return (float) -toDegrees(atan(
				(cachedPathPoints[point + 1].y - cachedPathPoints[point].y)
				/
				hypot(cachedPathPoints[point + 1].x - cachedPathPoints[point].x, cachedPathPoints[point + 1].z - cachedPathPoints[point].z)
				));
	}

	/**
	 * Returns the cached path point closest to the given position. 
	 */
	public Vec3f getCachedPointAt(float segment) {

		return cachedPathPoints[ round(segment*pathLength*CACHED_CURVE_INCREMENTS) ];
	}

	/**
	 * Returns the rotation about Y-axis for the path point closest to the given position.
	 */
	public float getCachedYawAngleAt(float segment) {

		int point = round(segment*pathLength*CACHED_CURVE_INCREMENTS);
		if (point + 1 >= cachedPathPoints.length) {
			point = cachedPathPoints.length - 2;
		}

		return (float) (360 + toDegrees(atan2(
				cachedPathPoints[point].x - cachedPathPoints[point + 1].x,
				cachedPathPoints[point + 1].z - cachedPathPoints[point].z)
				)) % 360;
	}

	/**
	 * Returns the length of the curve path.
	 */
	private float getPathLength() {
		return (float) (
				(cpDist * 2.0D) +
				(cpStart.distTo(startPoint)) +
				(cpEnd.distTo(cpStart) / 2.0D) +
				(endPoint.distTo(cpEnd) / 2.0D)
				) / 2.0F;
	}
}