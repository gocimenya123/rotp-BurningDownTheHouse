package com.rotpaddon.bdth.ghost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.bdth.entity.GhostObjectEntity;
import com.rotpaddon.bdth.network.BdthNetwork;
import com.rotpaddon.bdth.network.packets.GhostComputerResultPacket;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;

/**
 * The brain of the ghost computer: it answers a handful of questions about the world the user is in.
 * (A ghost computer knows what the real ones of the burnt-down prison once knew.)
 */
public final class GhostComputerService {
    private static final double STAND_SCAN_RANGE = 128;
    private static final double GHOST_SCAN_RANGE = 64;
    private static final int STRUCTURE_SEARCH_CHUNKS = 50;
    private static final long STRUCTURE_COOLDOWN_TICKS = 200;

    private static final Map<UUID, Long> LAST_STRUCTURE_SEARCH = new HashMap<>();

    private GhostComputerService() {}

    public static void answer(ServerPlayerEntity player, String rawQuery) {
        String query = rawQuery.trim().toLowerCase(Locale.ROOT);
        String[] parts = query.split("\\s+", 2);
        String argument = parts.length > 1 ? parts[1] : "";

        List<ITextComponent> lines = new ArrayList<>();
        switch (parts[0]) {
        case "help":
            lines.add(tr("computer.burningdownthehouse.help"));
            break;
        case "players":
            listPlayers(player, lines);
            break;
        case "stand":
            standOf(player, argument, lines);
            break;
        case "ghosts":
            nearbyGhosts(player, lines);
            break;
        case "structure":
            findStructure(player, argument, lines);
            break;
        case "time":
            lines.add(tr("computer.burningdownthehouse.time", player.level.getDayTime() % 24000));
            break;
        default:
            lines.add(tr("computer.burningdownthehouse.unknown", rawQuery.trim()).withStyle(TextFormatting.RED));
            break;
        }
        BdthNetwork.sendToClient(new GhostComputerResultPacket(lines), player);
    }

    private static TranslationTextComponent tr(String key, Object... args) {
        return new TranslationTextComponent(key, args);
    }

    private static void listPlayers(ServerPlayerEntity asker, List<ITextComponent> lines) {
        for (ServerPlayerEntity other : asker.server.getPlayerList().getPlayers()) {
            lines.add(new StringTextComponent(other.getGameProfile().getName() + " - " + other.level.dimension().location()));
        }
    }

    private static void standOf(ServerPlayerEntity asker, String name, List<ITextComponent> lines) {
        ServerPlayerEntity target = asker.server.getPlayerList().getPlayerByName(name);
        if (target == null || target.level != asker.level || target.distanceToSqr(asker) > STAND_SCAN_RANGE * STAND_SCAN_RANGE) {
            lines.add(tr("computer.burningdownthehouse.no_player"));
            return;
        }
        IStandPower.getStandPowerOptional(target).ifPresent(power -> {
            if (power.hasPower()) {
                lines.add(tr("computer.burningdownthehouse.stand", target.getName(), 
                        new TranslationTextComponent(power.getType().getTranslationKey())));
            }
            else {
                lines.add(tr("computer.burningdownthehouse.no_stand", target.getName()));
            }
        });
    }

    private static void nearbyGhosts(ServerPlayerEntity asker, List<ITextComponent> lines) {
        List<GhostObjectEntity> ghosts = asker.level.getEntitiesOfClass(GhostObjectEntity.class, 
                asker.getBoundingBox().inflate(GHOST_SCAN_RANGE));
        if (ghosts.isEmpty()) {
            lines.add(tr("computer.burningdownthehouse.no_ghosts"));
            return;
        }
        ghosts.sort((a, b) -> Double.compare(a.distanceToSqr(asker), b.distanceToSqr(asker)));
        for (GhostObjectEntity ghost : ghosts.subList(0, Math.min(5, ghosts.size()))) {
            lines.add(tr("computer.burningdownthehouse.ghost_entry", ghost.getStack().getHoverName(), 
                    (int) ghost.getX(), (int) ghost.getY(), (int) ghost.getZ()));
        }
    }

    private static void findStructure(ServerPlayerEntity asker, String name, List<ITextComponent> lines) {
        Structure<?> structure = Structure.STRUCTURES_REGISTRY.get(name);
        if (structure == null) {
            lines.add(tr("computer.burningdownthehouse.structure_unknown", String.join(", ", Structure.STRUCTURES_REGISTRY.keySet())));
            return;
        }
        long now = asker.level.getGameTime();
        Long last = LAST_STRUCTURE_SEARCH.get(asker.getUUID());
        if (last != null && now - last < STRUCTURE_COOLDOWN_TICKS) {
            lines.add(tr("computer.burningdownthehouse.busy"));
            return;
        }
        LAST_STRUCTURE_SEARCH.put(asker.getUUID(), now);
        BlockPos found = ((ServerWorld) asker.level).findNearestMapFeature(structure, asker.blockPosition(), STRUCTURE_SEARCH_CHUNKS, false);
        if (found == null) {
            lines.add(tr("computer.burningdownthehouse.structure_none", name));
        }
        else {
            lines.add(tr("computer.burningdownthehouse.structure_found", name, found.getX(), found.getZ()));
        }
    }
}
