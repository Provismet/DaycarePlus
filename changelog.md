Patch update following the major revamp to sort out a bug and give more control to servers.

## Additions
- Added new config option to not produce eggs for players who are offline.
  - The pastures still tick and count the time, but they won't create any items until the player logs in.
- Added new config option to deactivate pastures for banned players.
  - Deactivation applies on the first load per server session. You may need to reboot your server to see this apply.

## Changes
- Adjusted daycare error feedback text to clarify when certain actions are illegal.

## Bugfixes
- Fixed bug where Dittos could breed with each other.
- Fixed bug where egg production callbacks still triggered when a pasture failed to produce an egg due to being full.
  - Mostly affected competitive mode due to fertility being reduced.