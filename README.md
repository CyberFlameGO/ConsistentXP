# ConsistentXP

A Paper plugin that flattens Minecraft's experience curve so every level requires a fixed, consistent amount of XP instead of scaling linearly in difficulty.

## Features

- **Flat Progression**: Every level requires the exact same amount of XP (default: `50` XP/level).
- **Consistent Gains**: XP orbs fill the bar by the same percentage regardless of level.
- **Fair Enchanting & Anvils**: Consuming levels deducts a predictable amount of raw XP at any level.
- **Plugin Compatible**: Preserves vanilla Mending and respects external XP multipliers and death/grave plugins.
- **Automatic Migration**: Converts existing players' vanilla XP on first login (`CONVERT_TOTAL_XP` or `KEEP_LEVEL`).
- **PlaceholderAPI Support**: Native expansion for scoreboards, tablists, and menus.

## Commands & Permissions

Aliases: `/consistentxp`, `/cxp`

| Command | Permission | Description |
|---|---|---|
| `/cxp check [player]` | `consistentxp.check` | View consistent XP level, progress, needed XP, and total XP |
| `/cxp add <player> <amount> [points\|levels]` | `consistentxp.admin` | Add XP points or levels |
| `/cxp set <player> <amount> [points\|levels]` | `consistentxp.admin` | Set XP points or levels |
| `/cxp reload` | `consistentxp.admin` | Reload `config.yml` |

*Note: Players require `consistentxp.check.others` (or `consistentxp.admin`) to check other players' stats.*

## Placeholders

- `%consistentxp_level%` — Current level
- `%consistentxp_points%` — Points accumulated in the current level
- `%consistentxp_needed%` — Points needed for next level
- `%consistentxp_progress%` — Progress percentage to next level (e.g. `45.0`)
- `%consistentxp_total%` — Total accumulated consistent XP
- `%consistentxp_xp_per_level%` — XP required per level

## Building

Requires Java 27:

```bash
./gradlew build
```

Compiled jar will be in `build/libs/`.
