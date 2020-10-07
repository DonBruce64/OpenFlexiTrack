package openflextrack.blocks;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import openflextrack.api.IRailType;
import openflextrack.api.ISleeperType;
import openflextrack.api.ITrackContainer;
import openflextrack.api.OFTCurve;

/**
 * Track block tile entity. Handles fake child blocks.
 * 
 * @author don_bruce
 */
public class TileEntityTrack extends TileEntityRotatable implements ITrackContainer {

	/** {@code true} after this track has tried to connect to another track segment. */
	@SideOnly(Side.CLIENT) public boolean hasTriedToConnectToOtherSegment;

	/** The track this track is connected to. */
	@SideOnly(Side.CLIENT) public TileEntityTrack connectedTrack;

	/** The {@link openflextrack.api.OFTCurve curve} of this track. May be {@code null}. */
	@Nullable public OFTCurve curve;

	/** A {@link java.util.List List} holding all fake tracks' {@link net.minecraft.util.math.BlockPos block positions}. */
	private final List<BlockPos> fakeTracks = new ArrayList<BlockPos>();


	public TileEntityTrack() {
		super();
	}

	public TileEntityTrack(OFTCurve curve) {
		this.curve = curve;
	}

	@Override
	public OFTCurve getCurve() {
		return this.curve;
	}

	/**
	 * Getter for {@link #fakeTracks}.
	 */
	public List<BlockPos> getFakeTracks(){
		return this.fakeTracks;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public IRailType getRailType() {
		return DefaultRailType.DEFAULT_RAIL_TYPE;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	@Override
	public ISleeperType getSleeperType() {
		return DefaultSleeperType.DEFAULT_SLEEPER_TYPE;
	}

	@Override
	public boolean isHolographic() {
		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		/* Read curve data. */
		OFTCurve curve = OFTCurve.readFromNBT(nbt);
		if (curve != null) {
			this.curve = curve;
		}

		/* Read fake track positions. */
		this.fakeTracks.clear();
		NBTTagCompound nbt0;
		NBTTagList tagList = nbt.getTagList("fakeTrackCoords", 10);
		int tagCount = tagList.tagCount();

		for (int i = 0; i < tagCount; ++i) {
			nbt0 = tagList.getCompoundTagAt(i);
			this.fakeTracks.add(new BlockPos(nbt0.getInteger("x"), nbt0.getInteger("y"), nbt0.getInteger("z")));
		}
	}


	/**
	 * Invalidates this tile entity and removes all fake tracks.
	 */
	public void removeFakeTracks(){

		this.invalidate();

		BlockTrackFake.toggleMainTrackBreakage(false);
		{
			for (BlockPos fakePos : fakeTracks) {
				worldObj.setBlockToAir(fakePos);
			}
		}
		BlockTrackFake.toggleMainTrackBreakage(true);
	}

	/**
	 * Clears {@link #fakeTracks} and sets its contents to the objects in the given list.
	 */
	public void setFakeTracks(List<BlockPos> fakeTracks){
		this.fakeTracks.clear();
		this.fakeTracks.addAll(fakeTracks);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		/* Write curve if existent, or invalidate tile entity. */
		if (this.curve != null) {
			this.curve.writeToNBT(nbt);
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