package openflextrack.util;

import net.minecraft.util.math.BlockPos;

/**
 * Extended {@link net.minecraft.util.math.BlockPos BlockPos} type which contains a dimension value.
 * 
 * @author Leshuwa Kaiheiwa
 */
public class BlockPosDim extends BlockPos {
	
	/** Dimension of the given block position. */
	public final int dim;

	public BlockPosDim(BlockPos pos, int dim) {
		
		super(pos);
		this.dim = dim;
	}
}