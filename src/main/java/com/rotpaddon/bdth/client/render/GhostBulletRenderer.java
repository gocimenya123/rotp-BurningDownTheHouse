package com.rotpaddon.bdth.client.render;

import com.github.standobyte.jojo.client.ClientUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.rotpaddon.bdth.entity.GhostBulletEntity;
import com.rotpaddon.bdth.ghost.GhostShape;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

/** A pale, glowing streak. Like every ghost, it is seen only by those who can see Stands. */
public class GhostBulletRenderer extends EntityRenderer<GhostBulletEntity> {
    private static final GhostShape SHAPE = GhostShape.builder()
            .glowBox(-0.03F, -0.03F, -0.25F, 0.03F, 0.03F, 0.25F, 0xCFF3FF)
            .build();

    public GhostBulletRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public void render(GhostBulletEntity bullet, float yaw, float partialTick, MatrixStack matrix, IRenderTypeBuffer buffer, int light) {
        if (!ClientUtil.canSeeStands()) {
            return;
        }
        matrix.pushPose();
        matrix.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTick, bullet.yRotO, bullet.yRot)));
        matrix.mulPose(Vector3f.XP.rotationDegrees(-MathHelper.lerp(partialTick, bullet.xRotO, bullet.xRot)));
        GhostBoxRenderer.render(matrix, buffer.getBuffer(RenderType.entityTranslucent(GhostObjectRenderer.WHITE)), SHAPE, light, 0.9F, 1F);
        matrix.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(GhostBulletEntity bullet) {
        return GhostObjectRenderer.WHITE;
    }
}
