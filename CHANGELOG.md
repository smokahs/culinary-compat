# Changelog

### Added

**Kitchen Multiblock**
Farmer's Delight (Pot, Skillet, Cutting Board) blocks are now registered as valid CFB "Kitchen Multiblock" blocks. Pam's HC2 recipes (cutting board, skillet, pot, saucepan, oven) are bridged into the CFB "Cooking Table", each gated on the correct tool.

**Bakeware**
New Bakeware block for Pam's baking recipes. Two-click bake flow with 4-second timer, cooldown overlay, sounds, and ingredient refund on cancel.

**Recipe UX**
Recipes grey out when their tool is missing, "Missing tools" tooltip names the specific tool, catalyst items stripped from recipe display, recipes anchored top-left.

**Flush Placement**
FD Skillet, Cooking Pot, Cutting Board, and Bakeware render and collide flush with CFB blocks (-1/16 Y offset on model, BER, and hitbox).

**Food Rebalancing**
Optional restore of Pam's 1.12.2 food values, optional inedible-ingredient stripping, optional vanilla Minecraft food nerf.

**Tags**
Forge item tags for Pam's crops, grain, leafyvegetables, salad_ingredients, seeds, vegetables, eggs.

**Recipe Viewers**
JEI and EMI plugins with five kitchen-bridge categories.

**Configuration**
Common: `foodNerf.*`, `bakeware.enabled`, `bakeware.durationTicks`, `bakeware.refundOnCancel`. Client: `sounds.bakeDingSound`, `sounds.dingVolume`.
