package guichaguri.betterfps.clones.tileentity;

import java.util.Arrays;
import java.util.Iterator;
import guichaguri.betterfps.transformers.cloner.CopyMode;
import guichaguri.betterfps.transformers.cloner.CopyMode.Mode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

/**
 * @author Guilherme Chaguri
 */
public class BeaconLogic extends TileEntityBeacon {

    @CopyMode(Mode.IGNORE) // Ignore the constructor to prevent an infinite loop
    public BeaconLogic() {

    }

    private int tickCount = 0;
    @Override
    public void update() {
        tickCount--;
        if(tickCount == 100) {
            updateEffects(pos.getX(), pos.getY(), pos.getZ());
        } else if(tickCount <= 0) {
            updateBeacon();
        }
    }

    @Override
    public void updateBeacon() {
        // Updates everything, including player effects
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if(worldObj.isRemote) {
            updateGlassLayers(x, y, z);
        } else {
            updateActivation(x, y, z);
        }
        updateLevels(x, y, z);
        updateEffects(x, y, z);
        tickCount = 200;
    }

    @Override
    public void updateSegmentColors() {
        // Only updates the beacon structure
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if(worldObj.isRemote) {
            updateGlassLayers(x, y, z);
        } else {
            updateActivation(x, y, z);
        }
        updateLevels(x, y, z);
    }

    @Override
    public void addEffectsToPlayers() {
        // Only updates player effects
        updateEffects(pos.getX(), pos.getY(), pos.getZ());
    }

    private void updateEffects(int x, int y, int z) {
        if((isComplete) && (levels > 0) && (!worldObj.isRemote) && (primaryEffect != null)) {
            int radius = (levels + 1) * 10;
            byte effectLevel = 0;
            if((levels >= 4) && (primaryEffect == secondaryEffect)) {
                effectLevel = 1;
            }
            AxisAlignedBB box = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
            box = box.expand(radius, radius, radius).addCoord(0.0D, worldObj.getHeight(), 0.0D);
            Iterator iterator = worldObj.getEntitiesWithinAABB(EntityPlayer.class, box).iterator();

            boolean hasSecondaryEffect = (levels >= 4) && (primaryEffect != secondaryEffect) && (secondaryEffect != null);

            while(iterator.hasNext()) {
                EntityPlayer player = (EntityPlayer)iterator.next();
                player.addPotionEffect(new PotionEffect(primaryEffect, 180, effectLevel, true, true));
                if(hasSecondaryEffect) {
                    player.addPotionEffect(new PotionEffect(secondaryEffect, 180, 0, true, true));
                }
            }
        }
    }


    private void updateGlassLayers(int x, int y, int z) {
        // Checks if the beacon should be active and searches for stained glass to color the beam.
        // Should be only called client-side
        isComplete = true;
        beamSegments.clear();
        BeamSegment beam = new BeamSegment(EntitySheep.getDyeRgb(EnumDyeColor.WHITE));
        float[] oldColor = null;
        beamSegments.add(beam);
        int height = worldObj.getActualHeight();
        for(int blockY = y + 1; blockY < height; blockY++) {
            BlockPos pos = new BlockPos(x, blockY, z);
            IBlockState state = this.worldObj.getBlockState(pos);
            Block b = state.getBlock();
            float[] color;
            if(b == Blocks.STAINED_GLASS) {
                color = EntitySheep.getDyeRgb(state.getValue(BlockStainedGlass.COLOR));
            } else if(b == Blocks.STAINED_GLASS_PANE) {
                color = EntitySheep.getDyeRgb(state.getValue(BlockStainedGlassPane.COLOR));
            } else {
                if(b != Blocks.BEDROCK && b.getLightOpacity(state) >= 15) {
                    isComplete = false;
                    beamSegments.clear();
                    break;
                }
                beam.incrementHeight();
                continue;
            }

            if(oldColor != null) {
                color = new float[]{(oldColor[0] + color[0]) / 2.0F, (oldColor[1] + color[1]) / 2.0F, (oldColor[2] + color[2]) / 2.0F};
            }
            if(Arrays.equals(color, oldColor)) {
                beam.incrementHeight();
            } else {
                beam = new BeamSegment(color);
                beamSegments.add(beam);
                oldColor = color;
            }
        }
    }

    private void updateActivation(int x, int y, int z) {
        // Checks if the beacon should be activate
        // Should be called only server-side. updateGlassLayers do the trick on the client
        isComplete = true;
        int height = worldObj.getActualHeight();
        for(int blockY = y + 1; blockY < height; blockY++) {
            BlockPos pos = new BlockPos(x, blockY, z);
            IBlockState state = this.worldObj.getBlockState(pos);
            Block b = state.getBlock();
            if(b != Blocks.BEDROCK && b.getLightOpacity(state) >= 15) {
                isComplete = false;
                break;
            }
        }
    }

    private void updateLevels(int x, int y, int z) {
        // Checks if the beacon should be active and how many levels it should have.
        boolean isClient = worldObj.isRemote;
        int levelsOld = levels;
        lvlLoop: for(int lvl = 1; lvl <= 4; lvl++) {
            levels = lvl;
            int blockY = y - lvl;
            if(blockY < 0) break;

            for(int blockX = x - lvl; blockX <= x + lvl; blockX++) {
                for(int blockZ = z - lvl; blockZ <= z + lvl; blockZ++) {
                    BlockPos blockPos = new BlockPos(blockX, blockY, blockZ);
                    Block block = worldObj.getBlockState(blockPos).getBlock();
                    if(block != Blocks.EMERALD_BLOCK && block != Blocks.GOLD_BLOCK && block != Blocks.DIAMOND_BLOCK && block != Blocks.IRON_BLOCK) {
                        levels--;
                        break lvlLoop;
                    }
                }
            }

            if(isClient) break;
        }
        if(levels == 0) {
            this.isComplete = false;
        }

        if((!isClient) && (levels == 4) && (levelsOld < levels)) {
            AxisAlignedBB box = new AxisAlignedBB(x, y, z, x, y - 4, z).expand(10.0, 5.0, 10.0);
            Iterator iterator = worldObj.getEntitiesWithinAABB(EntityPlayer.class, box).iterator();
            while(iterator.hasNext()) {
                EntityPlayer entityplayer = (EntityPlayer)iterator.next();
                entityplayer.addStat(AchievementList.FULL_BEACON);
            }
        }
    }
}
