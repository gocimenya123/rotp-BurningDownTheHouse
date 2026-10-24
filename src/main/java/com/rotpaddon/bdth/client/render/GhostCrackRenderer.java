package com.rotpaddon.bdth.client.render;

import java.util.Random;

import com.github.standobyte.jojo.client.ClientUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.rotpaddon.bdth.entity.GhostCrackEntity;
import com.rotpaddon.bdth.init.InitDimensions;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

/**
 * Draws the crack: a jagged vertical slit, black inside, with a pale glowing edge that slowly pulses.
 * Seen only by those who can see Stands, who see it but do not know what it is.
 */
public class GhostCrackRenderer extends EntityRenderer<GhostCrackEntity> {
    private static final int SEGMENTS = 12;
    private static final float HEIGHT = 2.0F;
    private static final int FULL_BRIGHT = LightTexture.pack(15, 15);

    public GhostCrackRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public void render(GhostCrackEntity crack, float yaw, float partialTick, MatrixStack matrix, IRenderTypeBuffer buffer, int light) {
        // The exit crack of a room belongs to the room itself: a guest who cannot see Stands still has to see the
        // way out, or leaving them in the room after the owner goes would lock them in.
        if (!ClientUtil.canSeeStands()
                && !(crack.isRoomExit() && crack.level.dimension() == InitDimensions.GHOST_ROOM)) {
            return;
        }
        // the shape of the crack is the same every frame: it depends only on the entity
        Random random = new Random(crack.getId() * 7919L + 13);
        float[] centre = new float[SEGMENTS + 1];
        float[] half = new float[SEGMENTS + 1];
        for (int i = 0; i <= SEGMENTS; i++) {
            centre[i] = (random.nextFloat() - 0.5F) * 0.22F;
            half[i] = 0.025F + 0.08F * MathHelper.sin((float) Math.PI * i / SEGMENTS) * (0.6F + 0.4F * random.nextFloat());
        }

        float pulse = 0.5F + 0.2F * MathHelper.sin((crack.tickCount + partialTick) * 0.12F);
        matrix.pushPose();
        // local +Z points out of the wall
        matrix.mulPose(Vector3f.YP.rotationDegrees(-MathHelper.rotLerp(partialTick, crack.yRotO, crack.yRot)));
        IVertexBuilder vb = buffer.getBuffer(RenderType.entityTranslucent(GhostObjectRenderer.WHITE));
        Matrix4f pose = matrix.last().pose();
        Matrix3f normal = matrix.last().normal();
        strip(vb, pose, normal, centre, half, 0.045F, 0.0F, 200, 235, 255, (int) (pulse * 255), FULL_BRIGHT);
        strip(vb, pose, normal, centre, half, 0.0F, 0.004F, 4, 4, 8, 250, light);
        matrix.popPose();
    }

    /** a vertical strip following the jagged centre line, grown by {@code grow} on each side */
    private static void strip(IVertexBuilder vb, Matrix4f pose, Matrix3f normal, float[] centre, float[] half, 
            float grow, float z, int r, int g, int b, int a, int light) {
        for (int i = 0; i < SEGMENTS; i++) {
            float y0 = HEIGHT * i / SEGMENTS;
            float y1 = HEIGHT * (i + 1) / SEGMENTS;
            float l0 = centre[i] - half[i] - grow, r0 = centre[i] + half[i] + grow;
            float l1 = centre[i + 1] - half[i + 1] - grow, r1 = centre[i + 1] + half[i + 1] + grow;
            vertex(vb, pose, normal, l0, y0, z, r, g, b, a, light);
            vertex(vb, pose, normal, r0, y0, z, r, g, b, a, light);
            vertex(vb, pose, normal, r1, y1, z, r, g, b, a, light);
            vertex(vb, pose, normal, l1, y1, z, r, g, b, a, light);
        }
    }

    private static void vertex(IVertexBuilder vb, Matrix4f pose, Matrix3f normal, float x, float y, float z, 
            int r, int g, int b, int a, int light) {
        vb.vertex(pose, x, y, z).color(r, g, b, a).uv(0.5F, 0.5F)
        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(GhostCrackEntity crack) {
        return GhostObjectRenderer.WHITE;
    }
}
