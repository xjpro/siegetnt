
package org.summit.siegetnt.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.craftbukkit.entity.CraftTNTPrimed;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.util.Vector;
import org.summit.keepcraft.Keepcraft;
import org.summit.siegetnt.ExplosionBlock;
import org.summit.siegetnt.Main;
import org.summit.siegetnt.ShockRadiusTracker;

/**
 *
 * @author Me
 */
public class ExplosionListener implements Listener
{
    private static double NoDefendersPresent;
    private static double NoDefendersPresentResistance;
    private static double ExtremeImbalance;
    private static double ExtremeImbalanceResistance;
    private static double HighImbalance;
    private static double HighImbalanceResistance;
    
    private final Random random = new Random();
    private final Keepcraft keepcraftPlugin;

    public ExplosionListener()
    {
        keepcraftPlugin = (Keepcraft) Bukkit.getServer().getPluginManager().getPlugin("Keepcraft");
    }
    
    public void init()
    {
        Configuration config = Main.config();
        NoDefendersPresent = config.getDouble("imbalance.noDefenders.value", 0.1);
        NoDefendersPresentResistance = config.getDouble("imbalance.noDefenders.resistance", 0.7);
        ExtremeImbalance = config.getDouble("imbalance.extreme.value", 0.15);
        ExtremeImbalanceResistance = config.getDouble("imbalance.extreme.resistance", 0.6);
        HighImbalance = config.getDouble("imbalance.high.value", 0.2);
        HighImbalanceResistance = config.getDouble("imbalance.high.resistance", 0.5);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityExplode(EntityExplodeEvent event) 
	{
        Location loc = event.getLocation();
        World world = loc.getWorld();
        //Plot plot = ListenerHelper.getIntersectedPlot(loc, new ArrayList<Plot>(DataCache.retrieveAll(Plot.class)));
        
        boolean explosionDamagesBlocks = false;
        
        // Triggered explosions have no entity, so make a fake one
        Entity explodedEntity = event.getEntity();
        if(explodedEntity == null)
        {
            explodedEntity = world.spawnArrow(loc, new Vector(0, -1, 0), 0, 0);
            explosionDamagesBlocks = true;
        }
        else if(explodedEntity instanceof CraftTNTPrimed)
        {
            explosionDamagesBlocks = true;
        }
        
        // Event would be cancelled if it was damaging something that it shouldn't be, we will honor that
        // but still want the explosion animation and damage to player to occur
        if(event.isCancelled())
        {
            explosionDamagesBlocks = false;
            event.setCancelled(false);
        }
        
        // Damage from the explosion needs to be modded back in
        double blastDistance = 4;
        for(Entity nearby : explodedEntity.getNearbyEntities(blastDistance, blastDistance, blastDistance))
        {
            if(nearby instanceof LivingEntity && !nearby.isDead())
            {
                LivingEntity p = (LivingEntity) nearby;
                double distanceFromExplosion = p.getLocation().distance(loc);
                int damageTaken = (int) Math.round(25 * (1-(distanceFromExplosion / blastDistance)));
                p.damage(Math.max(0, damageTaken));
            }
        }
        
        explodedEntity.remove();
        
        if(!explosionDamagesBlocks)
        {
            // This would be like a creeper explosion; no block damage will occur
            return;
        }
        
        init();
        
        ShockRadiusTracker.addShockRadiusLocation(loc);
        
        // Now we get the imbalance from keepcraft, if it's available
        double chanceToIgnore = 0.0;
        if(keepcraftPlugin != null)
        {
            double imbalance = keepcraftPlugin.getTeamImbalanceAt(loc);
            
            if(imbalance == -1) chanceToIgnore = 100.0;
            else if(imbalance <= NoDefendersPresent) chanceToIgnore = NoDefendersPresentResistance; // 0:10 
            else if(imbalance <= ExtremeImbalance) chanceToIgnore = ExtremeImbalanceResistance; // 1:10
            else if(imbalance <= HighImbalance) chanceToIgnore = HighImbalanceResistance; // 1:7
            
           // Logger.getLogger("Minecraft").info(String.format("(SiegeTNT) Attack imbalance=%s. Chance to ignore is %s", imbalance, chanceToIgnore));
        }
        
        if(chanceToIgnore == 100.0)
        {
            return;
        }

        // Block destruction code
        
        List<ExplosionBlock> explosionBlocks = new ArrayList<ExplosionBlock>();
        
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX()-1, loc.getBlockY(), loc.getBlockZ()), false));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), false)); // center
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX()+1, loc.getBlockY(), loc.getBlockZ()), false));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX()-1, loc.getBlockY(), loc.getBlockZ()-1), false));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()-1), false));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX()+1, loc.getBlockY(), loc.getBlockZ()-1), false));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX()-1, loc.getBlockY(), loc.getBlockZ()+1), false));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()+1), false));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX()+1, loc.getBlockY(), loc.getBlockZ()+1), false));
        
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX()-1, loc.getBlockY()+1, loc.getBlockZ()), false));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY()+1, loc.getBlockZ()), false)); // one up
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX()+1, loc.getBlockY()+1, loc.getBlockZ()), false));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX()-1, loc.getBlockY()+1, loc.getBlockZ()-1), true));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY()+1, loc.getBlockZ()-1), false));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX()+1, loc.getBlockY()+1, loc.getBlockZ()-1), true));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX()-1, loc.getBlockY()+1, loc.getBlockZ()+1), true));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY()+1, loc.getBlockZ()+1), false));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX()+1, loc.getBlockY()+1, loc.getBlockZ()+1), true));
        
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX()-1, loc.getBlockY()-1, loc.getBlockZ()), false));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY()-1, loc.getBlockZ()), false)); // one down
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX()+1, loc.getBlockY()-1, loc.getBlockZ()), false));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX()-1, loc.getBlockY()-1, loc.getBlockZ()-1), true));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY()-1, loc.getBlockZ()-1), false));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX()+1, loc.getBlockY()-1, loc.getBlockZ()-1), true));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX()-1, loc.getBlockY()-1, loc.getBlockZ()+1), true));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY()-1, loc.getBlockZ()+1), false));
        explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX()+1, loc.getBlockY()-1, loc.getBlockZ()+1), true));
        
        for(ExplosionBlock explosionBlock : explosionBlocks)
        {
            // Uncomment out this line to reimplement change to ignore
            //if(random.nextDouble() < chanceToIgnore) continue;
            
            Block block = explosionBlock.getBlock();
            boolean isCorner = explosionBlock.isCorner();

            switch(block.getType())
            {
                case TNT:
                    block.setType(Material.AIR);
                    block.getWorld().createExplosion(block.getLocation(), 0);
                    break;
                case AIR: // can't blow up air!
                case BEDROCK:
                case IRON_DOOR_BLOCK:
                    // Immune blocks
                    break;
                case DIAMOND_BLOCK:
                case IRON_BLOCK:
                case GOLD_BLOCK:
                    if(!isCorner || random.nextDouble() < 0.05) block.setType(Material.OBSIDIAN);
                    break;
                case OBSIDIAN:
                    if(!isCorner || random.nextDouble() < 0.10) block.setType(Material.STONE);
                    break;
                case STONE:
                case SMOOTH_BRICK:
                case SMOOTH_STAIRS:
                case DOUBLE_STEP:
                case BRICK:
                case BRICK_STAIRS:
                case NETHER_BRICK:
                case NETHER_BRICK_STAIRS:
                case PISTON_BASE:
                case PISTON_STICKY_BASE:
                case IRON_ORE:
                case REDSTONE_ORE:
                case GOLD_ORE:
                case DIAMOND_ORE:
                    if(!isCorner || random.nextDouble() < 0.30) block.setType(Material.COBBLESTONE);
                    break;
                case COBBLESTONE:
                case COBBLESTONE_STAIRS:
                case MOSSY_COBBLESTONE:
                case FURNACE:
                case DISPENSER:
                    if(!isCorner || random.nextDouble() < 0.60) block.setType(Material.GRAVEL);
                    break;
                case SANDSTONE:
                case SOUL_SAND:
                    if(!isCorner || random.nextDouble() < 0.60) block.setType(Material.SAND);
                    break;
                default:
                    // Everything else drops
                    if(!isCorner || random.nextDouble() < 0.90) block.setType(Material.AIR);
                    break;
                   // ItemStack item = new ItemStack(block.getType(), 1);
                   // world.dropItem(block.getLocation(), item);
            }
        }
        // End block destruction
    }
        
    @EventHandler(priority = EventPriority.NORMAL)
    public void onExplosionPrime(ExplosionPrimeEvent event)
    {
        event.setRadius(0f); // No damage to blocks, but the animation will still occur
    }
}
