# Beta Changelog

## v1.0.0
1. Release!
2. Farmer's Delight (Pot, Skillet, Cutting Board) blocks are now registered as valid CFB "Kitchen Multiblock" blocks. Pam's HC2 recipes (cutting board, skillet, pot, saucepan, oven) are bridged into the CFB "Cooking Table", each gated on the correct tool.
3. New Bakeware block for Pam's baking recipes. Two-click bake flow with 4-second timer, cooldown overlay, sounds, and ingredient refund on cancel.
4. Recipes grey out when their tool is missing, "Missing tools" tooltip names the specific tool, catalyst items stripped from recipe display, recipes anchored top-left.
5. FD Skillet, Cooking Pot, Cutting Board, and Bakeware render and collide flush with CFB blocks (-1/16 Y offset on model, BER, and hitbox).
6. Optional restore of Pam's 1.12.2 food values, optional inedible-ingredient stripping, optional vanilla Minecraft food nerf.
7. Forge item tags for Pam's crops, grain, leafyvegetables, salad_ingredients, seeds, vegetables, eggs.
8. JEI and EMI plugins with five kitchen-bridge categories.
9. config values added `foodNerf.*`, `bakeware.enabled`, `bakeware.durationTicks`, `bakeware.refundOnCancel`. Client: `sounds.bakeDingSound`, `sounds.dingVolume`.

## v1.0.1
1. Fixed Emi tab to look identical to JEI's
2. Added missing Cutting Board Tooltip
3. Disabled FD model offset on cfb:cooking_table
4. removed dough tag from phc2:doughitem

## v1.0.2
1. Disabled model offset on cfb:cooking_table for FD skillet and CC bakeware (fixes z-fighting).
2. FD cutting board can now be placed on cfb:corner; shape, BER offset, and kitchen-multiblock detection extended to cover corner.
3. changed some logic to make sure every bridge recipe requires its workstation present.
4. Disabled FD bread_from_smelting and bread_from_smoking recipes. Disabled vanilla 3x wheat bread recipe (redundant)

## v1.0.3
1. reworked recipe handler a bit to ensure compat with hoshihoku modpack

## v1.0.4 
1. removed redundant cfb multiblock kitchen tooltips
2. fixed model offsets on cooking_table block
3. fixed bake tooltip using cfb native balm tooltips (see 4.)
4. added balm dependency (shouldn't be a problem, cfb already has balm dependency)

## v1.0.5
1. Added support for [Storage Delight](https://www.curseforge.com/minecraft/mc-mods/storage-delight-forge) per the request of RobynTheGhoul on Curseforge!
2. Bakeware block now behaves like FD (pot, skillet) blocks with identical sounds and mineability
3. Thank you to everyone who submitted issues! v1.0.6-beta should have all the fixes!

## v1.0.6
1. Fixed issue from [MaveTheMaverick and RainbowMagicMarker's reports](https://github.com/smokahs/culinary-compat/issues/2), thanks guys!

## v1.0.7
1. Made certain inedible ingredients craftable again (thanks stylzm!)
2. Reworked ALL Culinary Compat recipe displays in EMI/JEI
3. Huge Cutting board additions!
    - Multi-Input Support 
    - All of Pam's Cutting Board Recipes fully implemented!
    - New Mechanics:
        - Right click the cutting board with an ingredient to stack it! (up to 9)
        - Right click a cutting board containing multiple ingredients with an empty hand to remove the top item. 
        - Crouch + Right Click a cutting board containing multiple ingredients with an empty hand to remove ALL items!
        - Invalid Recipe Popup: "That doesn't seem right..."