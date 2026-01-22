# [TaCZ] First Aid Compat

A compatibility mod that enables 3D hitbox-based damage distribution when using Timeless and Classics Zero (TaCZ) with First Aid.

## Features

- **Precise Limbs Targeting**: Uses exact 3D hit locations to determine which body part was hit.
- **Armor Protection**: Calculates armor protection per body part (head, chest, legs, feet).
- **Damage Spillover**: Implements damage spillover mechanics; when a limb is destroyed, excess damage transfers to the torso.
- **Stance Support**: Handles damage distribution when players are in crawling or prone states.
- **Explosion Support**: Distance-based damage distribution for explosions.

## Requirements

- Minecraft: 1.20.1
- Forge: 47.3.0+
- Timeless and Classics Zero: 1.1.7+
- First Aid: 1.20.1-1.1+

## Installation

1. Download the mod JAR.
2. Place it in the `mods` folder.
3. Ensure both TaCZ and First Aid are installed.

## Technical Details

This mod uses Mixin to intercept TaCZ bullet hit events and the First Aid damage distribution system:

1. When a projectile hits a player, the hit location is captured.
2. The hit location is converted to player-local coordinates, accounting for rotation and posture.
3. A hitbox system determines the specific body part hit.
4. First Aid applies damage to that body part with standard armor calculations.
5. If the body part is destroyed, remaining damage spills over to the torso.

### Spillover Logic
- **Projectiles**: 80% of excess damage transfers to the body.
- **Explosions**: 100% of excess damage transfers to the body.
- **Feet**: Damage spills to the corresponding leg first, then to the torso if the leg is destroyed.

## License

MIT License - See LICENSE for details.

## Credits

- **Author**: Ranazy
- **TaCZ**: Timeless and Classics Zero
- **First Aid**: First Aid Mod
