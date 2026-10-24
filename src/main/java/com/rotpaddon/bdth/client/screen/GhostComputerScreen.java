package com.rotpaddon.bdth.client.screen;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.rotpaddon.bdth.network.BdthNetwork;
import com.rotpaddon.bdth.network.packets.GhostComputerQueryPacket;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

/** The terminal of the ghost computer: type a question, get the answer printed under it. */
public class GhostComputerScreen extends Screen {
    private static final int PANEL_WIDTH = 260;
    private static final int PANEL_HEIGHT = 150;
    private static final int TEXT_COLOR = 0x7FE8D8;
    private static final int MAX_LINES = 11;

    private TextFieldWidget input;
    private final List<IReorderingProcessor> log = new ArrayList<>();

    public GhostComputerScreen() {
        super(new TranslationTextComponent("screen.burningdownthehouse.computer"));
    }

    @Override
    protected void init() {
        super.init();
        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        input = new TextFieldWidget(font, left + 8, top + PANEL_HEIGHT - 20, PANEL_WIDTH - 16, 12, StringTextComponent.EMPTY);
        input.setMaxLength(64);
        input.setBordered(false);
        input.setTextColor(TEXT_COLOR);
        children.add(input);
        setInitialFocus(input);
        if (log.isEmpty()) {
            addResult(java.util.Collections.singletonList(new TranslationTextComponent("computer.burningdownthehouse.welcome")));
        }
    }

    @Override
    public void removed() {
        minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void tick() {
        input.tick();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && input.isFocused()) {
            String query = input.getValue().trim();
            if (!query.isEmpty()) {
                addResult(java.util.Collections.singletonList(new StringTextComponent("> " + query)));
                BdthNetwork.sendToServer(new GhostComputerQueryPacket(query));
                input.setValue("");
            }
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    /** prints lines (wrapped to the panel) under the previous ones */
    public void addResult(List<ITextComponent> lines) {
        for (ITextComponent line : lines) {
            log.addAll(font.split(line, PANEL_WIDTH - 16));
        }
        while (log.size() > MAX_LINES) {
            log.remove(0);
        }
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrix);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        AbstractGui.fill(matrix, left - 2, top - 2, left + PANEL_WIDTH + 2, top + PANEL_HEIGHT + 2, 0xFF9FB4C8);
        AbstractGui.fill(matrix, left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xF0071014);
        drawCenteredString(matrix, font, title, left + PANEL_WIDTH / 2, top + 4, TEXT_COLOR);
        int y = top + 18;
        for (IReorderingProcessor line : log) {
            font.draw(matrix, line, left + 8, y, TEXT_COLOR);
            y += 9;
        }
        AbstractGui.fill(matrix, left + 6, top + PANEL_HEIGHT - 23, left + PANEL_WIDTH - 6, top + PANEL_HEIGHT - 6, 0xFF0E1D24);
        input.render(matrix, mouseX, mouseY, partialTicks);
        super.render(matrix, mouseX, mouseY, partialTicks);
    }
}
