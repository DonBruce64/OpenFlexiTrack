package openflextrack.blocks;

import org.lwjgl.opengl.GL11;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import openflextrack.OFT;
import openflextrack.api.ISleeperType;

/**
 * Default sleeper type class.
 */
public class DefaultSleeperType implements ISleeperType {

	/** Public reference to the default sleeper type. */
	public static final DefaultSleeperType DEFAULT_SLEEPER_TYPE = new DefaultSleeperType();

	/** Default track texture. */
	@SideOnly(Side.CLIENT) private static ResourceLocation tieTexture;

	/** Tie length. */
	private static final float length = 0.375F;
	private static final float height = 0.1875F;

	private static final float tieTopMinU = 0F;
	private static final float tieTopMaxU = 0.6875F;
	private static final float tieTopMinV = 0F;
	private static final float tieTopMaxV = 0.375F;

	private static final float tieSideMinU = tieTopMinU;
	private static final float tieSideMaxU = tieTopMaxU;
	private static final float tieSideMinV = tieTopMaxV;
	private static final float tieSideMaxV = 0.5625F;

	private static final float tieEndMinU = tieTopMinU;
	private static final float tieEndMaxU = 0.046875F;
	private static final float tieEndMinV = tieSideMaxV;
	private static final float tieEndMaxV = 0.75F;


	@Override
	public float getOffset() {
		return 0.65F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ResourceLocation getTexture() {
		if (tieTexture == null) {
			tieTexture = new ResourceLocation(OFT.MODID, "textures/blockmodels/tie.png");
		}
		return tieTexture;
	}

	@Override
	public float getWidth() {
		return 2.75F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void render(float width) {
		//Render ties using current texture but with dynamic width.
		//This will cause stretching of the texture, but it ensures uniformity for all sizes.
		GL11.glBegin(GL11.GL_QUADS);
		renderTieBottom(width);
		renderTieTop(width);
		renderTieFront(width);
		renderTieRear(width);
		renderTieLeft(width);
		renderTieRight(width);
		GL11.glEnd();
	}

	@SideOnly(Side.CLIENT)
	private static final void renderTieBottom(float width){
		GL11.glTexCoord2f(tieTopMinU, tieTopMinV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(width/2, 0, length/2);

		GL11.glTexCoord2f(tieTopMaxU, tieTopMinV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(-width/2, 0, length/2);

		GL11.glTexCoord2f(tieTopMaxU, tieTopMaxV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(-width/2, 0, -length/2);

		GL11.glTexCoord2f(tieTopMinU, tieTopMaxV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(width/2, 0, -length/2);
	}

	@SideOnly(Side.CLIENT)
	private static final void renderTieTop(float width){
		GL11.glTexCoord2f(tieTopMinU, tieTopMinV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(-width/2, height, length/2);

		GL11.glTexCoord2f(tieTopMaxU, tieTopMinV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(width/2, height, length/2);

		GL11.glTexCoord2f(tieTopMaxU, tieTopMaxV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(width/2, height, -length/2);

		GL11.glTexCoord2f(tieTopMinU, tieTopMaxV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(-width/2, height, -length/2);
	}

	@SideOnly(Side.CLIENT)
	private static final void renderTieFront(float width){
		GL11.glTexCoord2f(tieSideMinU, tieSideMinV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(-width/2, height, length/2);

		GL11.glTexCoord2f(tieSideMinU, tieSideMaxV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(-width/2, 0, length/2);

		GL11.glTexCoord2f(tieSideMaxU, tieSideMaxV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(width/2, 0, length/2);

		GL11.glTexCoord2f(tieSideMaxU, tieSideMinV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(width/2, height, length/2);
	}

	@SideOnly(Side.CLIENT)
	private static final void renderTieRear(float width){
		GL11.glTexCoord2f(tieSideMinU, tieSideMinV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(width/2, height, -length/2);

		GL11.glTexCoord2f(tieSideMinU, tieSideMaxV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(width/2, 0, -length/2);

		GL11.glTexCoord2f(tieSideMaxU, tieSideMaxV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(-width/2, 0, -length/2);

		GL11.glTexCoord2f(tieSideMaxU, tieSideMinV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(-width/2, height, -length/2);
	}

	@SideOnly(Side.CLIENT)
	private static final void renderTieLeft(float width){
		GL11.glTexCoord2f(tieEndMinU, tieEndMinV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(-width/2, height, -length/2);

		GL11.glTexCoord2f(tieEndMinU, tieEndMaxV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(-width/2, 0, -length/2);

		GL11.glTexCoord2f(tieEndMaxU, tieEndMaxV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(-width/2, 0, length/2);

		GL11.glTexCoord2f(tieEndMaxU, tieEndMinV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(-width/2, height, length/2);
	}

	@SideOnly(Side.CLIENT)
	private static final void renderTieRight(float width){
		GL11.glTexCoord2f(tieEndMinU, tieEndMinV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(width/2, height, length/2);

		GL11.glTexCoord2f(tieEndMinU, tieEndMaxV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(width/2, 0, length/2);

		GL11.glTexCoord2f(tieEndMaxU, tieEndMaxV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(width/2, 0, -length/2);

		GL11.glTexCoord2f(tieEndMaxU, tieEndMinV);
		GL11.glNormal3f(0, 1, 0);
		GL11.glVertex3f(width/2, height, -length/2);
	}
}