package org.nukkit.dynamiclight;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.PluginBase;

import java.util.HashMap;
import java.util.Map;

public class DynamicLightPlugin extends PluginBase implements Listener {

    private final Map<Player, Vector3> lightPositions = new HashMap<>();
    private final int lightLevel = 15;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("DynamicLight plugin enabled (with offhand support)!");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Check if holding torch in main hand or offhand
        boolean holdingTorch = isTorch(player.getInventory().getItemInHand()) ||
                               isTorch(player.getOffhandInventory().getItem(0));

        Vector3 currentPos = player.getPosition().add(0, player.getEyeHeight(), 0).floor();  // Position at eye height

        // If holding a torch, place light around the player
        if (holdingTorch) {
            // Remove the previous light block
            Vector3 lastLight = lightPositions.get(player);
            if (lastLight != null && !lastLight.equals(currentPos)) {
                if (player.getLevel().getBlock(lastLight).getId() == Block.LIGHT_BLOCK) {
                    player.getLevel().setBlock(lastLight, Block.get(Block.AIR));  // Clear previous light
                }
            }

            // Place a new light block at the player's current position
            if (player.getLevel().getBlock(currentPos).getId() == Block.AIR) {
                Block lightBlock = new BlockLightBlock();
                lightBlock.setDamage(lightLevel);
                player.getLevel().setBlock(currentPos, lightBlock);  // Place light at the player's position
                lightPositions.put(player, currentPos);  // Store light position
            }
        } else {
            // If the player is not holding a torch, remove the light
            Vector3 lastLight = lightPositions.remove(player);
            if (lastLight != null && player.getLevel().getBlock(lastLight).getId() == Block.LIGHT_BLOCK) {
                player.getLevel().setBlock(lastLight, Block.get(Block.AIR));  // Clear light if no torch
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Vector3 pos = lightPositions.remove(player);
        if (pos != null && player.getLevel().getBlock(pos).getId() == Block.LIGHT_BLOCK) {
            player.getLevel().setBlock(pos, Block.get(Block.AIR));  // Clear light when player quits
        }
    }

    private boolean isTorch(Item item) {
        return item != null && item.getId() == Item.TORCH;  // Check if the item is a torch
    }
}
