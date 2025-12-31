## Additions
- Incubators can now withdraw directly from chests and other containers if you shift-interact.
- Added the Original Trainer of a Pokémon to the PC GUI.
  - Added config toggle for the above. 
- Marks can now randomly apply to Pokémon hatched from eggs.
  - Default potential marks are applied to the HatchEgg.Post event with the HIGHEST priority.
  - The potential marks are consumed and (possibly) applied at the LOWEST priority in the same event.
  - If you want to add your own, then you can use Cobblemon's molang callback (which defaults to HIGHEST, I think? Kotlin is dumb) or with code in the HatchEgg.Post event using any priority above LOWEST.

## Changes
- EggHatch.Post event now triggers before the Pokémon is given to the player.
  - This is due to yet another Cobblemon bug, if potential marks are on the Pokémon when it's granted to the player, the game crashes.
    - Literally how do you mess this up?
- Improved the texture of the egg group panel.

## Bugfixes
- Eggs no longer swap their ticks and max ticks after being removed from an incubator.
- Incubators can no longer tick in other players inventories.
- The number of pastures linked to the player now recalculates when closing the server or exiting a singleplayer world.