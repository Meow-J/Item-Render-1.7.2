package itemrender.client;


import itemrender.client.rendering.FBOHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.io.File;

@SideOnly(Side.CLIENT)
public class KeybindRenderEntity {

    /**
     * Key descriptions
     */
    private final String desc;
    /**
     * Default key values
     */
    private final int keyValue;

    public FBOHelper fbo;
    private String filenameSuffix = "";
    public final KeyBinding key;

    public KeybindRenderEntity(int textureSize, String filename_suffix, int keyval, String des) {
        fbo = new FBOHelper(textureSize);
        filenameSuffix = filename_suffix;
        keyValue = keyval;
        desc = des;
        key = new KeyBinding(desc, keyValue, "Item Render");
        ClientRegistry.registerKeyBinding(key);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (FMLClientHandler.instance().isGUIOpen(GuiChat.class))
            return;
        if (key.isPressed()) {
            Minecraft minecraft = FMLClientHandler.instance().getClient();
            if (minecraft.pointedEntity != null) {
                EntityLivingBase current = (EntityLivingBase) minecraft.pointedEntity;
                fbo.begin();

                AxisAlignedBB aabb = current.getEntityBoundingBox();
                double minX = aabb.minX - current.posX;
                double maxX = aabb.maxX - current.posX;
                double minY = aabb.minY - current.posY;
                double maxY = aabb.maxY - current.posY;
                double minZ = aabb.minZ - current.posZ;
                double maxZ = aabb.maxZ - current.posZ;

                double minBound = Math.min(minX, Math.min(minY, minZ));
                double maxBound = Math.max(maxX, Math.max(maxY, maxZ));

                double boundLimit = Math.max(Math.abs(minBound), Math.abs(maxBound));

                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glPushMatrix();
                GL11.glLoadIdentity();
                GL11.glOrtho(-boundLimit * 0.75, boundLimit * 0.75, -boundLimit * 1.25, boundLimit * 0.25, -100.0, 100.0);

                GL11.glMatrixMode(GL11.GL_MODELVIEW);

                renderEntity(current); // GuiInventory.func_110423_a(0, 0, 1, 1,
                // 1,
                // current);

                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glPopMatrix();

                fbo.end();

                fbo.saveToFile(new File(minecraft.mcDataDir, String.format("rendered/entity_%s%s.png", EntityList.getEntityString(current), filenameSuffix)));

                fbo.restoreTexture();
            }
        }
    }

    private void renderEntity(EntityLivingBase entity) {
        Minecraft minecraft = FMLClientHandler.instance().getClient();
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        GL11.glScalef((float) (-1), (float) 1, (float) 1);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        float f2 = entity.renderYawOffset;
        float f3 = entity.rotationYaw;
        float f4 = entity.rotationPitch;
        float f5 = entity.prevRotationYawHead;
        float f6 = entity.rotationYawHead;
        GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);

        GL11.glRotatef((float) Math.toDegrees(Math.asin(Math.tan(Math.toRadians(30)))), 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(-45, 0.0F, 1.0F, 0.0F);

        entity.renderYawOffset = (float) Math.atan((double) (1 / 40.0F)) * 20.0F;
        entity.rotationYaw = (float) Math.atan((double) (1 / 40.0F)) * 40.0F;
        entity.rotationPitch = -((float) Math.atan((double) (1 / 40.0F))) * 20.0F;
        entity.rotationYawHead = entity.rotationYaw;
        entity.prevRotationYawHead = entity.rotationYaw;
        GL11.glTranslated(0.0D, entity.getYOffset(), 0.0D);
        minecraft.getRenderManager().playerViewY = 180.0F;
        minecraft.getRenderManager().renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        entity.renderYawOffset = f2;
        entity.rotationYaw = f3;
        entity.rotationPitch = f4;
        entity.prevRotationYawHead = f5;
        entity.rotationYawHead = f6;
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}
