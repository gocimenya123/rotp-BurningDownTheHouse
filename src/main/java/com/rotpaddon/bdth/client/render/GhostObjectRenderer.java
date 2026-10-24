package com.rotpaddon.bdth.client.render;

import com.github.standobyte.jojo.client.ClientUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.rotpaddon.bdth.BdthAddon;
import com.rotpaddon.bdth.entity.GhostObjectEntity;
import com.rotpaddon.bdth.ghost.GhostObjectType;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

/**
 * Renders a ghost object: its cuboid shape, translucent, glowing, gently bobbing.
 * Drawn only for those who can see Stands, the same rule that shows Stand entities.
 */
public class GhostObjectRenderer extends EntityRenderer<GhostObjectEntity> {
    public static final ResourceLocation WHITE = new ResourceLocation(BdthAddon.MOD_ID, "textures/entity/ghost_white.png");

    public GhostObjectRenderer(EntityRendererManager manager) {
        super(manager);
        this.shadowRadius = 0;
    }

    @Override
    public void render(GhostObjectEntity entity, float yaw, float partialTick, MatrixStack matrix, 
            IRenderTypeBuffer buffer, int light) {
        GhostObjectType type = entity.getGhostType();
        if (type == null || !ClientUtil.canSeeStands()) {
            return;
        }
        float age = entity.tickCount + partialTick;
        matrix.pushPose();
        matrix.translate(0, 0.03 * MathHelper.sin(age * 0.1F), 0);
        matrix.mulPose(Vector3f.YP.rotationDegrees(180F - MathHelper.rotLerp(partialTick, entity.yRotO, entity.yRot)));
        float alpha = 0.55F + 0.1F * MathHelper.sin(age * 0.15F);
        GhostBoxRenderer.render(matrix, buffer.getBuffer(RenderType.entityTranslucent(WHITE)), type.getShape(), light, alpha, 1.15F);
        matrix.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(GhostObjectEntity entity) {
        return WHITE;
    }
}
