# [TaCZ] First Aid Compat

A compatibility mod that enables precise 3D hitbox-based damage distribution when using **TacZ (Timeless and Classics Zero)** with **First Aid**.

## Features

- **Realistic Bodypart Targeting**: Uses exact 3D hit locations to determine which bodypart was hit, instead of FirstAid's default random/height-based distribution
- **Proper Armor Protection**: Calculates armor protection per bodypart (head, chest, legs, feet)
- **Damage Spillover**: Implements realistic damage spillover mechanics - when a limb is destroyed, excess damage transfers to the torso
- **Crawl Support**: Properly handles damage distribution when players are in TacZ's crawling/prone state
- **Explosion Support**: Distance-based damage distribution for explosions

## Requirements

- **Minecraft**: 1.20.1
- **Forge**: 47.3.0+
- **TacZ (Timeless and Classics Zero)**: 1.1.7+
- **First Aid**: 1.20.1-1.1+

## Installation

1. Download the mod JAR from [Modrinth](#) or [Releases](https://github.com/Ranazy/tacz-firstaid-compat/releases)
2. Place it in your `mods` folder
3. Make sure both TacZ and First Aid are installed
4. Launch the game!

## How It Works

This mod uses Mixin to intercept TacZ's bullet hit events and FirstAid's damage distribution system:

1. When a TacZ bullet hits a player, the exact 3D hit location is captured
2. The hit location is converted to player-local coordinates (accounting for rotation and posture)
3. A 3D hitbox system determines which bodypart was hit
4. FirstAid applies damage to that specific bodypart, with proper armor calculation
5. If the bodypart is destroyed, excess damage spills over to the torso

## Technical Details

- **Hitbox System**: Defines precise 3D bounding boxes for each bodypart (head, torso, arms, legs, feet)
- **Coordinate Transform**: Handles world-to-local coordinate conversion with proper yaw rotation and crawl positioning
- **Spillover Logic**: 
  - Bullets: 80% of excess damage transfers (20% energy loss)
  - Explosions: 100% of excess damage transfers (shockwave)
  - Feet: Damage first spills to the corresponding leg, then to torso

## License

MIT License - See [LICENSE](LICENSE) for details

## Credits

- **Author**: Ranazy
- **TacZ**: [Timeless and Classics Zero](https://modrinth.com/mod/timeless-and-classics-zero)
- **First Aid**: [First Aid](https://modrinth.com/mod/firstaid)

## Support

For issues, suggestions, or questions:
- Open an issue on [GitHub](https://github.com/Ranazy/tacz-firstaid-compat/issues)
- Join our Discord: [Link TBD]
