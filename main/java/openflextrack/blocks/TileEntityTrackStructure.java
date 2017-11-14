package openflextrack.blocks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import openflextrack.OFTCurve;

/**
 * Track block tile entity. Handles fake child blocks.
 * 
 * @author don_bruce
 */
public class TileEntityTrackStructure extends TileEntityRotatable {

	/** {@code true} after this track has tried to connect to another track segment. */
	@SideOnly(Side.CLIENT) public boolean hasTriedToConnectToOtherSegment;

	/** The track this track is connected to. */
	@SideOnly(Side.CLIENT) public TileEntityTrackStructure connectedTrack;

	/** The {@link openflextrack.OFTCurve curve} of this track. May be {@code null}. */
	public OFTCurve curve;

	/** A {@link java.util.List List} holding all fake tracks' {@link net.minecraft.util.math.BlockPos block positions}. */
	private List<BlockPos> fakeTracks = new ArrayList<BlockPos>();


	public TileEntityTrackStructure() {
		super();
	}

	public TileEntityTrackStructure(OFTCurve curve) {
		this.curve = curve;
	}


	/**
	 * Getter for {@link #fakeTracks}.
	 */
	public List<BlockPos> getFakeTracks(){
		return this.fakeTracks;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared(){
		return 65536.0D;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox(){
		return INFINITE_EXTENT_AABB;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		/* Read curve data. */
		int[] endCoords = nbt.getIntArray("curveEndPoint");
		if (endCoords.length != 0) {
			curve = new OFTCurve(new BlockPos(endCoords[0], endCoords[1], endCoords[2]), nbt.getFloat("curveStartAngle"), nbt.getFloat("curveEndAngle"));
		}

		/* Read fake track positions. */
		this.fakeTracks.clear();
		NBTTagCompound nbt0;
		NBTTagList tagList = nbt.getTagList("fakeTrackCoords", 10);
		int tagCount = tagList.tagCount();

		for (int i = 0; i < tagCount; ++i) {
			nbt0 = tagList.getCompoundTagAt(i);
			fakeTracks.add(new BlockPos(nbt0.getInteger("x"), nbt0.getInteger("y"), nbt0.getInteger("z")));
		}
	}


	/**
	 * Invalidates this tile entity and removes all fake tracks.
	 */
	public void removeFakeTracks(){

		this.invalidate();

		BlockTrackStructureFake.toggleMainTrackBreakage(false);
		{
			for (BlockPos fakePos : fakeTracks) {
				worldObj.setBlockToAir(fakePos);
			}
		}
		BlockTrackStructureFake.toggleMainTrackBreakage(true);
	}

	/**
	 * Set {@link #fakeTracks} to the given list of take track blocks.
	 */
	public void setFakeTracks(List<BlockPos> fakeTracks){
		this.fakeTracks = fakeTracks;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		/* Write curve if existent, or invalidate tile entity. */
		if (curve != null) {
			nbt.setFloat("curveStartAngle", curve.startAngle);
			nbt.setFloat("curveEndAngle", curve.endAngle);
			nbt.setIntArray("curveEndPoint", new int[]{curve.endPos.getX(), curve.endPos.getY(), curve.endPos.getZ()});
		} else {
			this.invalidate();
		}

		/* Write fake track positions. */
		NBTTagCompound nbt0;
		NBTTagList tagList = new NBTTagList();

		for (BlockPos pos : this.fakeTracks) {
			nbt0 = new NBTTagCompound();
			nbt0.setInteger("x", pos.getX());
			nbt0.setInteger("y", pos.getY());
			nbt0.setInteger("z", pos.getZ());
			tagList.appendTag(nbt0);
		}

		nbt.setTag("fakeTrackCoords", tagList);

		return nbt;
	}
}