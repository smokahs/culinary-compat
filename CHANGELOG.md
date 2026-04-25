# Changelog

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