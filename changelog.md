Huge internal update to completely revamp the breeding code.

No gameplay changes are present in this update. All new features a purely technical and only affect server owners, side-modders, and datapack makers.

## Additions
### Breeding Rules
Breeding Rules are a new feature replacing the now deprecated Pre-Evolution Overrides. They allow you to completely control the breeding output of two
parent Pokemon.

The Breeding Rule system checks for rules that match the species of the primary parent (the female or non-ditto parent) and then scans a list of rules,
checking them against _both_ parents until a match is found.

The rules use a datapackable predicate system that can check the following components of either parent:
- species
- form name
- species + form
- aspects
- moves
- level
- friendship
- fullness
- held item (showdown id)
- species labels
- form labels
- EVs
- IVs
- whether or not a pre-evolution exists for this species
- whether or not an evolution exists for this species

The system is then able to produce an offspring with any of the following data already set:
- species
- features
- level
- moves
- ability
- gender
- nature
- EVs
- IVs
- held item (any Minecraft ItemStack)
- scale
- shiny
- any properties
  - This PokemonProperties is applied first and includes everything that works with `/pokegive` and similar commands.

A rule may have a predicate for the primary (female/non-ditto) parent, the secondary parent, both, or even neither. A rule may also have
any number of possible offspring; if there is more than one offspring for a rule, then one is selected at random. This randomness is weighted
and the datapack creator can additionally set the weights (integer, defaults to 1 if omitted).

Breeding Rules may also bypass gender and egg group rules if the author wishes. Potentially allowing any Pokemon to breeding with any other Pokemon to
produce any possible offspring.

To give an example of the power of this system: all special vanilla rules have been implemented via Breeding Rules.
- Illumise and Volbeat breeding into each other's species.
- NidoranF and NidoranM breeding into each other's species.
- Pikachu holding a Light Ball produces a Pichu that knows Volt Tackle.
- Regional forms producing the correct "region-bias" form from Cobblemon.
- Indeedee using forms as its genders.

A new datagen API for Breeding Rules has also been provided. (Seriously just use the API, you do NOT want to be writing these out by hand.)

## Bugfixes
- Tatsugiri's form override data has been added to the built-in datapack and should no longer randomise when breeding.

## Deprecations
- The pre-evolution overrides (`data/daycareplus/overrides/preevolutions/...`) have been deprecated in favour of the new Breeding Rules system.
  - Pre-Evolution overrides have not been removed, existing datapacks will still function.
  - This feature will be removed in version 2.0.0, please update all of your datapack pre-evolution overrides to Breeding Rules before then.