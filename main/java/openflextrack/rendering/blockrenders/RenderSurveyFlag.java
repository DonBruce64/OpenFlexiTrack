package openflextrack.rendering.blockrenders;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import openflextrack.OFT;
import openflextrack.blocks.TileEntitySurveyFlag;
import openflextrack.rendering.blockmodels.ModelSurveyFlag;

public class RenderSurveyFlag extends TileEntitySpecialRenderer<TileEntitySurveyFlag>{
	private static final ModelSurveyFlag model = new ModelSurveyFlag();
	private static final ResourceLocation texture = new ResourceLocation(OFT.MODID, "textures/blockmodels/surveyflag.png");

	public RenderSurveyFlag(){}

	@Override
	public void renderTileEntityAt(TileEntitySurveyFlag tileFlag, double x, double y, double z, float partialTicks, int destroyStage){
		super.renderTileEntityAt(tileFlag, x, y, z, partialTicks, destroyStage);
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);

		GL11.glPushMatrix();
		GL11.glTranslatef(0.5F, 0, 0.5F);
		GL11.glRotatef(180 - tileFlag.rotation*45, 0, 1, 0);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		model.render();
		GL11.glPopMatrix();

		if(tileFlag.linkedCurve != null){
			TileEntitySurveyFlag otherEnd = (TileEntitySurveyFlag) tileFlag.getWorld().getTileEntity(tileFlag.getPos().add(tileFlag.linkedCurve.endPos));
			//Make sure not to render if the other end has done so.
			if(otherEnd != null){
				//Try to keep the same flag rendering if possible.
				//If the flag isn't null, render that one instead.
				boolean renderFromOtherEnd = otherEnd.getPos().getX() != tileFlag.getPos().getX() ? otherEnd.getPos().getX() > tileFlag.getPos().getX() : otherEnd.getPos().getZ() > tileFlag.getPos().getZ(); 
				if(renderFromOtherEnd){
					GL11.glPopMatrix();
					this.renderTileEntityAt(otherEnd, x + otherEnd.getPos().getX() - tileFlag.getPos().getX(), y + otherEnd.getPos().getY() - tileFlag.getPos().getY(), z + otherEnd.getPos().getZ() - tileFlag.getPos().getZ(), partialTicks, destroyStage);
					return;
				}
			}
			RenderTrack.renderTrackSegmentFromCurve(tileFlag.getWorld(), tileFlag.getPos(), tileFlag.linkedCurve, true, null, null);
		}
		GL11.glPopMatrix();
	}
}
