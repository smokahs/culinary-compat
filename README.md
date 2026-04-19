<p align="center">
  <img src="src/main/resources/icon.png" width="128" alt="Culinary Compat icon"/>
</p>

# Culinary Compat!

A clean, lightweight compatibility layer for **Pam's HarvestCraft 2**, **Farmer's Delight**, and **Cooking for Blockheads**!

Minecraft 1.20.1, Forge 47+, MIT.

[![CurseForge](https://img.shields.io/badge/curseforge-culinary%20compat-f16436)](https://www.curseforge.com/minecraft/mc-mods/culinary-compat)

## Changelog

See the [CHANGELOG](CHANGELOG.md) for all changes.

--------------------------------------

## Features

### Kitchen Multiblock

Farmer's Delight (Pot, Skillet, Cutting Board) blocks are now registered as valid CFB "Kitchen Multiblock" blocks. All their respective recipes are visible through the CFB "Cooking Table"!

Pam's HarvestCraft 2 recipes (cutting board, skillet, pot, saucepan, oven) are bridged too, each gated on the correct Farmer's Delight tool being in the kitchen. If Farmer's Delight Version doesn't exist, Pam's tool serves as replacement.

### Bakeware

New Bakeware block, custom block to Culinary Compat which allows all Pam's bakeware recipes! Instead of unintuitive shapeless crafting, the CFB recipes mimic an oven! Click once to confirm, click again to bake, 4-second timer with cooldown overlay, sounds, and a cancel-refund. Fully configurable.

### Food Rebalancing

Optional restore of Pam's 1.12.2 food values, optional strip of edibility from ingredients (butter, spices, etc.), optional nerf of vanilla Minecraft foods. These Features have been forked from Pixel1011's Mod PamsHC2 Food Nerf!

### Recipe Viewers

JEI and EMI plugins with five kitchen-bridge categories and workstation hints.

--------------------------------------

## Installation

Drop `culinarycompat-<version>.jar` into `mods/`. All integrations are soft dependencies, runs cleanly with any subset.

Works with: [Pam's HC2 Food Core](https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft-2-food-core), [Food Extended](https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft-2-food-extended), [Crops](https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft-2-crops), [Trees](https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft-2-trees), [Farmer's Delight](https://www.curseforge.com/minecraft/mc-mods/farmers-delight), [Cooking for Blockheads](https://www.curseforge.com/minecraft/mc-mods/cooking-for-blockheads).

--------------------------------------

## Credits

This mod wouldn't exist without: Pam (Pam's HarvestCraft 2), vectorwing (Farmer's Delight), BlayTheNinth (Cooking for Blockheads).

--------------------------------------

## Issues

[GitHub issue tracker](https://github.com/smokahs/Culinary-Compat/issues). Include MC+Forge version, Culinary Compat version, installed mod subset, and `logs/latest.log`.
