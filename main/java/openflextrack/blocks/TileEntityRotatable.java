package openflextrack.blocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import openflextrack.OFT;
import openflextrack.packets.TileEntityClientRequestDataPacket;

public abstract class TileEntityRotatable extends TileEntity {

	/** Rotation value, ranges from {@code 0} to {@code 7}, inclusively. */
	public byte rotation;

	@Override
	public void validate(){
		super.validate();
		if(worldObj.isRemote){
			OFT.OFTNet.sendToServer(new TileEntityClientRequestDataPacket(this));
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt){
		super.readFromNBT(nbt);
		this.rotation = nbt.getByte("rotation");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		super.writeToNBT(nbt);
		nbt.setByte("rotation", rotation);
		return nbt;
	}
}