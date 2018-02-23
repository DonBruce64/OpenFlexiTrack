package openflextrack.api.util;

/**
 * Three-dimensional vector holding 32-bit floating point coordinates.
 * 
 * @author Leshuwa Kaiheiwa
 */
public class Vec3f {

	/** Vector X coordinate */
	public float x;
	/** Vector Y coordinate */
	public float y;
	/** Vector Z coordinate */
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
	public double distTo(Vec3f vec) {
		
		float x = this.x - vec.x;
		float y = this.y - vec.y;
		float z = this.z - vec.z;
		
		return Math.sqrt( (x*x) + (y*y) + (z*z) );
	}
	
	public Vec3f add(Vec3f vec)  {
		return new Vec3f(this.x + vec.x, this.y + vec.y, this.z + vec.z);
	}
	
	public Vec3f sub(Vec3f vec)  {
		return new Vec3f(this.x - vec.x, this.y - vec.y, this.z - vec.z);
	}
}