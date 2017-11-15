package openflextrack.util;

/**
 * Three-dimensional vector holding 32-bit floating point coordinates.
 * 
 * @author Leshuwa Kaiheiwa
 */
public class Vec3f {

	public float x;
	public float y;
	public float z;

	/**
	 * Construct a new Vec3f from the given coordinates.
	 */
	public Vec3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Casts given double arguments down and passes them to {@link #Vec3f(float, float, float)}.
	 */
	public Vec3f(double x, double y, double z) {
		this( (float)x, (float)y, (float)z );
	}
	
	
	/**
	 * Returns the distance between the given Vec3f and this vector.
	 * 
	 * @param vec - The vector that will be subtracted from this vector.
	 */
	public double sqDistTo(Vec3f vec) {
		
		float x = this.x - vec.x;
		float y = this.y - vec.y;
		float z = this.z - vec.z;
		
		return (x*x) + (y*y) + (z*z);
	}
}