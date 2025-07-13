package net.unkleacid.tropimount.init;

import net.unkleacid.tropimount.entity.IguanaEntity;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.OverworldDimension;
import net.modificationstation.stationapi.api.event.worldgen.biome.BiomeModificationEvent;

public class EntitySpawnListener {

    @EventListener
    public void registerEntitySpawn(BiomeModificationEvent event) {
        // Check vanilla biomes first
        if (event.biome == Biome.RAINFOREST || event.biome == Biome.SWAMPLAND) {
            /// Hot and humid vanilla biomes
            event.biome.addPassiveEntity(IguanaEntity.class, 0);

        } else if (event.biome == Biome.DESERT) {
            /// Desert Vanilla Biome
            event.biome.addPassiveEntity(IguanaEntity.class, 2);

        } else if (event.biome == Biome.SAVANNA || event.biome == Biome.SHRUBLAND) {
            /// Dry Vanilla biomes
            event.biome.addPassiveEntity(IguanaEntity.class, 0);

        } else if (event.biome == Biome.FOREST || event.biome == Biome.PLAINS) {
            // Temperate Vanilla Biomes
            event.biome.addPassiveEntity(IguanaEntity.class, 0);

        } else if (event.biome == Biome.ICE_DESERT || event.biome == Biome.HELL || event.biome == Biome.SKY || event.biome == Biome.TAIGA || event.biome == Biome.TUNDRA) {

        } else {
            if (event.biome.canSnow()) {
                return;
            }

            if (!(event.world.dimension instanceof OverworldDimension)) {
                return;
            }

            if (event.biome.canRain()) {
                event.biome.addPassiveEntity(IguanaEntity.class, 0);
            } else {
                event.biome.addPassiveEntity(IguanaEntity.class, 0);
            }
        }
    }
}
