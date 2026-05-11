<p align="center">
  <img src="src/main/resources/icon.png" width="200" alt="Culinary Compat icon"/>
</p>

# Culinary Compat!
<a href='https://files.minecraftforge.net'><img alt="forge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/forge_vector.svg"></a>

The ultimate culinary compatibility mod! Brigding the culinary gaps between mods like Farmers Delight (+ addons) and Cooking for Blockheads, the Pam's mods, AE2, & many others with additions, bug fixes, & more! A must-have mod for culinary connoisseurs!

Minecraft 1.20.1, Forge 47+, MIT.

  [![CurseForge Version](https://img.shields.io/curseforge/v/1516050?label=CurseForge&color=orange&logo=curseforge&style=for-the-badge)](https://www.curseforge.com/minecraft/mc-mods/culinary-compat)
  [![Modrinth Version](https://img.shields.io/modrinth/v/culinary-compat?label=Modrinth&color=00AF5C&logo=modrinth&style=for-the-badge)](https://modrinth.com/mod/culinary-compat)

## Changelog

See the [CHANGELOG](CHANGELOG.md) for all changes.

--------------------------------------

## Full Featureset

### - Kitchen Multiblock

The Farmer's Delight (Pot, Skillet, Cutting Board) blocks are now registered as valid CFB "Kitchen Multiblock" blocks. All their respective recipes are visible through the CFB "Cooking Table"!

Pam's HarvestCraft 2 recipes (cutting board, skillet, pot, saucepan, oven) are bridged too, each gated on the correct Farmer's Delight tool being in the kitchen. If Farmer's Delight Version doesn't exist, Pam's tool serves as replacement.

### - Custom Bakeware Block

New Bakeware block, custom block to Culinary Compat which allows all Pam's bakeware recipes! Instead of unintuitive shapeless crafting, the CFB recipes mimic an oven! Click once to confirm, click again to bake, 4-second timer with cooldown overlay, sounds, and a cancel-refund. Fully configurable.

### - AE2 Wireless Bridge

The ME Kitchen Station wirelessly links any Applied Energistics 2 ME network to your CFB kitchen multiblock. Bidirectional by default: your AE2 network can feed kitchen recipes AND your kitchen cabinets, fridges, and counters show up on your ME terminal. Right-click the block for a settings panel to switch direction modes (ME → Kitchen, Kitchen → ME, or Bidirectional), a cosmetic screen toggle, and a wrench tab opening AE2's native priority page. Thanks Sebastrn!

### - Head Chef's Journal

A Patchouli guidebook crafted with a vanilla book + cake. Categories and entries auto-show/hide based on which optional mods you have installed, so it stays relevant to whatever subset of integrations you're running. Currently covers the AE2 bridge in depth, more sections will be added as the mod grows as well!

### - Food Rebalancing

Optional restore of Pam's 1.12.2 food values, optional strip of edibility from ingredients (butter, spices, etc.), optional nerf of vanilla Minecraft foods. These Features have been forked from Pixel1011's Mod PamsHC2 Food Nerf!

### - Recipe Viewers

JEI and EMI plugins with five kitchen-bridge categories and workstation hints.

### - Configuration

Client and server config files are included! More options will be added to them as well. Feel free to give some suggestions!

## Currently Supported (optional) Addons & Integrations 
<details> <summary>Farmer's Delight Addons</summary>
  
    - Storage Delight
    - Ender's Delight
    - End's Delight
    - Nether's Delight
    - Twilight's Flavor and Delight
</details>

<details> <summary>Applied Energistics 2</summary>
    - ME Kitchen Station: wirelessly connect your CFB kitchen to your AE2 Network (Refined storage coming soon!)
</details>

<details> <summary>Patchouli</summary>
    - Adds an in game guidebook, titled "Head Chef's Journal"
</details>

### Integrations:
- Fixes crashes from multiple addons that reference outdated (pre 1.3.1) Farmers Delight methods 

--------------------------------------
## Installation

Drop `culinarycompat-<version>.jar` into `mods/`. All integrations are soft dependencies, runs cleanly with any subset. INTENDED TO BE USED WITH ALL OF THE FOLLOWING:

[Pam's HC2 Food Core](https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft-2-food-core), [Food Extended](https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft-2-food-extended), [Crops](https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft-2-crops), [Trees](https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft-2-trees), [Farmer's Delight](https://www.curseforge.com/minecraft/mc-mods/farmers-delight), [Cooking for Blockheads](https://www.curseforge.com/minecraft/mc-mods/cooking-for-blockheads).

Optional, : [Applied Energistics 2](https://www.curseforge.com/minecraft/mc-mods/applied-energistics-2) (ME Kitchen Station), [Patchouli](https://www.curseforge.com/minecraft/mc-mods/patchouli) (Head Chef's Journal).

--------------------------------------

## Credits

This mod wouldn't exist without: Pam (Pam's HarvestCraft 2), vectorwing (Farmer's Delight), BlayTheNinth (Cooking for Blockheads), Pixel 1101 (PXH2 Nerf), and ItsSebastrn (AppliedCooking).

--------------------------------------

## Issues

[GitHub issue tracker](https://github.com/smokahs/Culinary-Compat/issues). Include MC+Forge version, Culinary Compat version, installed mod subset, crash log (if applicable), and `logs/latest.log`.
