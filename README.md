# Burning Down the House - Ripples of the Past addon

Stone Ocean's Stand with no body: a crack in a wall leads to the ghost of a burnt-down music room full of ghost objects.
Built on the Example Addon; follows the architecture of Mr. President (`NoSummonStandType`, own dimension, world capability, teleporters).

Authors: Gocimenya, inkegg.

## Build
Same as the example addon: set `main_mod_version` in `gradle.properties` to a RotP snapshot, then `./gradlew build` (JDK 8 toolchain).
Give yourself the Stand: `/stand give @s burning_down_the_house` (or the Stand disc from the creative tab).

## What is where
| Package | Role |
|---|---|
| `init/` | registries: stand, actions, items, blocks, entities, dimension key |
| `stand/`, `action/` | `BdthStandType` + 7 `StandAction`s (crack open/close, enter/leave, invite, pocket, grab/release) |
| `ghost/` | ghost object types and shapes, perception rule (`GhostUtil` reuses `StandUtil.playerCanSeeStands`), computer |
| `ghost/room/` | room world data (capability of the dimension), builder, teleporters, enter/exit service, transitions |
| `capability/` | per-player `GhostUserData` (pocket, invitations, crack), generic NBT provider/storage |
| `entity/`, `item/`, `block/` | ghost object, crack, bullet (on `ModdedProjectileEntity`); ghost items; room blocks |
| `network/` | channel + 4 packets on RotP's `IModPacketHandler` |
| `event/` | Forge events; `GhostBulletPassThroughEvent` is the hook for other Stands (e.g. Kiss) |
| `client/` | renderers, squash effect, computer screen, `ClientBridge` |

## Known limits
- Not compiled in the authoring environment (Forge/Mojang/RotP maven were unreachable); APIs were checked against the supplied RotP sources.
- "Everything looks flattened inside the room" is approximated: the body is flattened for onlookers and the user's view closes to a slit while passing through the crack. A true anisotropic world squash would need a custom projection/shader.
- Art is procedural (cuboid shapes, generated icons) - replace `GhostShapes` / textures with real models when available.
- Invited players can only enter while the owner is already inside; everyone but the owner is sent out when the owner leaves.
