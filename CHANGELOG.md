# Beta Changelog

## v1.0.9
1. [Issue #3](https://github.com/smokahs/culinary-compat/issues/3) addon stove and cabinet multiblock support (thanks MaveTheMaverick!)
    - All of the following blocks now supported in a CFB multiblock kitchen:
        - Ender's Delight: `endersdelight:endstone_stove`
        - End's Delight: `ends_delight:end_stove`
        - Nether's Delight: `nethersdelight:blackstone_stove`
        - Twilight's Flavor and Delight: `twilightdelight:maze_stove`
    - Also fixed an issue with the `farmersdelight:cabinets` blocks that i found while fixing #1, so now certain cabinets from various addons and fd itself: 
        - Join the CFB multiblock as connectors
        - Expose their inventory to CFB recipes and the AE2 *ME Kitchen Station*
    - Yellow *Multiblock Kitchen* tooltip added to all the above (easy recipe viewer searching)
2. Twilight's Flavor and Delight Fiery Cooking Pot fixes
    - Recognized as a valid Cooking Pot inside the CFB multiblock (pot recipes now craftable when)
    - Registered as a Pot workstation in both JEI and EMI
    - Model offset on CFB blocks now matches the outline shape
3. Farmer's Delight 1.3.1 compat
    - Fixed cutting board crash from FD changing `CuttingBoardBlockEntity.addItem` return type
    - Fixed cutting board knife sound id (FD renamed `block.cutting_board.knife` to `block.cutting_board.knife_cut`)
    - Reimplemented FD's removed `ItemUtils.isInventoryEmpty` helper for addons still calling it
        - Patches Ender's Delight ticking crash ([their issue #37](https://github.com/Ax3dGaming/EndersDelight/issues/37))
        - Patches End's Delight ticking crash ([their issue #59](https://github.com/FoggyHillside/End-s-Delight/issues/59))
4. **Major** Cutting board improvements to support new FD 1.3.1!
    - Slot 1 capped at 1 item per right-click again (FD 1.3.1 had dropped the slot limit, letting whole stacks dump in)
    - Crouch + right-click an empty cutting board with food to 'bulk cut' (or whatever fd is calling it) up to 64 items
    - Multi-input cutting + extras placement are disabled while slot 1 is >1
    - Sneak + right click with an empty hand or knife to pick up all cutting board ingredients!
5. Op Netherite Knife config option is now respected during CFB recipe-book bridge crafts (was always treated as immune regardless of the toggle, whoops lol)

## v1.0.8
1. Added Patchouli as an *OPTIONAL* dependency for the new *Head Chef's Journal* guide (see 3.)
2. Forked ALL features of [AppliedCooking](https://github.com/Sebastrn/AppliedCooking) (thanks ItsSebastrn!) into Culinary Compat as a soft-dep AE2 bridge
    - New block: *ME Kitchen Station* (`culinarycompat:ae2_kitchen_station`)
        - Wirelessly links any AE2 ME network to a Cooking For Blockheads kitchen multiblock via a Wireless Connector
        - All **NEW** storage modes:
            - AE2 → Kitchen: kitchen recipes pull ingredients from the ME network (original AppliedCooking behavior)
            - Kitchen → AE2: kitchen cabinets, fridges, etc. show up in your AE2 terminal as storage
            - BIDIRECTIONAL: Works both ways!
                - Priority value supported, full integer range, uses AE2's native priority system (same as storage cells and storage buses)
        - Custom AE2 GUI (right-click the block):
            - *Mode* cycle: `ME → Kitchen`, `Kitchen → ME`, or `BIDIRECTIONAL` (default)
            - *Screen* cycle: On / Off (purely cosmetic once linked and connected)
            - *Priority* wrench tab in the upper-right opens AE2's native priority page
            - Hover the *Mode* and *Screen* labels for in-context tooltips
        - Black screen when disconnected, purple when linked to an active ME Network
        - model offset when placed on full blocks and `cfb:cooking_table` (no more z-fighting on counters, cabinets, Storage Delight blocks, etc.)
        - Green *Linked* tooltip indicator when bound to a wireless access point, matching the AE2 wireless terminals
        - Hold-Shift expanded info on the item tooltip, pointing to the *Head Chef's Journal*
        - Preserves the access-point link when broken and picked up
3. Added the *Head Chef's Journal* (`culinarycompat:guidebook`)
    - Crafted with a vanilla book + cake
    - Patchouli categories and entries auto-show/hide based on which optional mods are installed
    - First category added: *Applied Energistics*, with a deep walkthrough of the ME Kitchen Station, settings panel, mode directions, priority, and screen reference images. More to entries to come! 
4. Tooltip + EMI cleanups:
    - Stripped EMI's `(+NBT)` tag from the ME Kitchen station
    - Hides the duped Patchouli copy of the journal from EMI search

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
     
## v1.0.6
1. Fixed issue from [MaveTheMaverick and RainbowMagicMarker's reports](https://github.com/smokahs/culinary-compat/issues/2), thanks guys!

## v1.0.5
1. Added support for [Storage Delight](https://www.curseforge.com/minecraft/mc-mods/storage-delight-forge) per the request of RobynTheGhoul on Curseforge!
2. Bakeware block now behaves like FD (pot, skillet) blocks with identical sounds and mineability
3. Thank you to everyone who submitted issues! v1.0.6-beta should have all the fixes!

## v1.0.4 
1. removed redundant cfb multiblock kitchen tooltips
2. fixed model offsets on cooking_table block
3. fixed bake tooltip using cfb native balm tooltips (see 4.)
4. added balm dependency (shouldn't be a problem, cfb already has balm dependency)

## v1.0.3
1. reworked recipe handler a bit to ensure compat with hoshihoku modpack

## v1.0.2
1. Disabled model offset on cfb:cooking_table for FD skillet and CC bakeware (fixes z-fighting).
2. FD cutting board can now be placed on cfb:corner; shape, BER offset, and kitchen-multiblock detection extended to cover corner.
3. changed some logic to make sure every bridge recipe requires its workstation present.
4. Disabled FD bread_from_smelting and bread_from_smoking recipes. Disabled vanilla 3x wheat bread recipe (redundant)
   
## v1.0.1
1. Fixed Emi tab to look identical to JEI's
2. Added missing Cutting Board Tooltip
3. Disabled FD model offset on cfb:cooking_table
4. removed dough tag from phc2:doughitem

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
