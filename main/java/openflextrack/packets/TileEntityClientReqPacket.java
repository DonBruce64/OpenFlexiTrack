package openflextrack.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import openflextrack.OFT;

/**
 * Data packet sent by clients to request a {@link openflextrack.packets.TileEntitySyncPacket synchronisation packet}.
 */
public class TileEntityClientReqPacket implements IMessage {
	private int x;
	private int y;
	private int z;

	public TileEntityClientReqPacket() {}

	public TileEntityClientReqPacket(TileEntity tile){
		this.x = tile.getPos().getX();
		this.y = tile.getPos().getY();
		this.z = tile.getPos().getZ();
	}

	@Override
	public void fromBytes(ByteBuf buf){
		this.x=buf.readInt();
		this.y=buf.readInt();
		this.z=buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf){
		buf.writeInt(this.x);
		buf.writeInt(this.y);
		buf.writeInt(this.z);
	}

	public static class Handler implements IMessageHandler<TileEntityClientReqPacket, IMessage> {
		@Override
		public IMessage onMessage(final TileEntityClientReqPacket message, final MessageContext ctx){
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(new Runnable(){
				@Override
				public void run(){
					TileEntity tile = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(new BlockPos(message.x, message.y, message.z));
					if(tile != null){
						OFT.OFTNet.sendTo(new TileEntitySyncPacket(tile), ctx.getServerHandler().playerEntity);
					}
				}
			});
			return null;
		}
	}	
}
