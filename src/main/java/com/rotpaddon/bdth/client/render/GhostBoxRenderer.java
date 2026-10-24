package com.rotpaddon.bdth.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.rotpaddon.bdth.ghost.GhostShape;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;

/** Draws the cuboids of a {@link GhostShape}: translucent, flat-shaded per face, emissive boxes ignore the light level. */
public final class GhostBoxRenderer {
    private static final int FULL_BRIGHT = LightTexture.pack(15, 15);

    private GhostBoxRenderer() {}

    public static void render(MatrixStack matrix, IVertexBuilder buffer, GhostShape shape, int light, float alpha, float glowBoost) {
        for (GhostShape.Box box : shape.getBoxes()) {
            int boxLight = box.glow ? FULL_BRIGHT : light;
            float boost = box.glow ? 1F : glowBoost;
            drawBox(matrix, buffer, box, boxLight, alpha, boost);
        }
    }

    private static void drawBox(MatrixStack matrix, IVertexBuilder buffer, GhostShape.Box b, int light, float alpha, float boost) {
        Matrix4f pose = matrix.last().pose();
        Matrix3f normal = matrix.last().normal();
        // top, bottom, north (-Z), south (+Z), west (-X), east (+X); each with a fixed brightness like vanilla block shading
        quad(pose, normal, buffer, b, light, alpha, boost, 1.0F, 0, 1, 0, 
                b.x0, b.y1, b.z0, b.x0, b.y1, b.z1, b.x1, b.y1, b.z1, b.x1, b.y1, b.z0);
        quad(pose, normal, buffer, b, light, alpha, boost, 0.5F, 0, -1, 0, 
                b.x0, b.y0, b.z1, b.x0, b.y0, b.z0, b.x1, b.y0, b.z0, b.x1, b.y0, b.z1);
        quad(pose, normal, buffer, b, light, alpha, boost, 0.8F, 0, 0, -1, 
                b.x1, b.y0, b.z0, b.x0, b.y0, b.z0, b.x0, b.y1, b.z0, b.x1, b.y1, b.z0);
        quad(pose, normal, buffer, b, light, alpha, boost, 0.8F, 0, 0, 1, 
                b.x0, b.y0, b.z1, b.x1, b.y0, b.z1, b.x1, b.y1, b.z1, b.x0, b.y1, b.z1);
        quad(pose, normal, buffer, b, light, alpha, boost, 0.6F, -1, 0, 0, 
                b.x0, b.y0, b.z0, b.x0, b.y0, b.z1, b.x0, b.y1, b.z1, b.x0, b.y1, b.z0);
        quad(pose, normal, buffer, b, light, alpha, boost, 0.6F, 1, 0, 0, 
                b.x1, b.y0, b.z1, b.x1, b.y0, b.z0, b.x1, b.y1, b.z0, b.x1, b.y1, b.z1);
    }

    private static void quad(Matrix4f pose, Matrix3f normal, IVertexBuilder buffer, GhostShape.Box box, int light, 
            float alpha, float boost, float shade, float nx, float ny, float nz, 
            float x1, float y1, float z1, float x2, float y2, float z2, 
            float x3, float y3, float z3, float x4, float y4, float z4) {
        float s = Math.min(shade * boost, 1.4F);
        int r = Math.min(255, (int) (box.r * s));
        int g = Math.min(255, (int) (box.g * s));
        int bl = Math.min(255, (int) (box.b * s));
        int a = (int) (alpha * 255);
        vertex(pose, normal, buffer, x1, y1, z1, r, g, bl, a, light, nx, ny, nz);
        vertex(pose, normal, buffer, x2, y2, z2, r, g, bl, a, light, nx, ny, nz);
        vertex(pose, normal, buffer, x3, y3, z3, r, g, bl, a, light, nx, ny, nz);
        vertex(pose, normal, buffer, x4, y4, z4, r, g, bl, a, light, nx, ny, nz);
    }

    private static void vertex(Matrix4f pose, Matrix3f normal, IVertexBuilder buffer, float x, float y, float z, 
            int r, int g, int b, int a, int light, float nx, float ny, float nz) {
        buffer.vertex(pose, x, y, z)
        .color(r, g, b, a)
        .uv(0.5F, 0.5F)
        .overlayCoords(OverlayTexture.NO_OVERLAY)
        .uv2(light)
        .normal(normal, nx, ny, nz)
        .endVertex();
    }
}
