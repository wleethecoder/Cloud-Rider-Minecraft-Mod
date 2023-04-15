package com.leecrafts.cloudrider.event;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.capability.ModCapabilities;
import com.leecrafts.cloudrider.capability.cloudsteedentity.CloudSteedEntityCap;
import com.leecrafts.cloudrider.capability.cloudsteedentity.CloudSteedEntityCapProvider;
import com.leecrafts.cloudrider.capability.cloudsteedentity.ICloudSteedEntityCap;
import com.leecrafts.cloudrider.capability.cloudsteeditem.CloudSteedItemCap;
import com.leecrafts.cloudrider.capability.cloudsteeditem.CloudSteedItemCapProvider;
import com.leecrafts.cloudrider.capability.cloudsteeditem.ICloudSteedItemCap;
import com.leecrafts.cloudrider.capability.lightning.ILightningCap;
import com.leecrafts.cloudrider.capability.lightning.LightningCap;
import com.leecrafts.cloudrider.capability.lightning.LightningCapProvider;
import com.leecrafts.cloudrider.config.CloudRiderCommonConfigs;
import com.leecrafts.cloudrider.entity.ModEntityTypes;
import com.leecrafts.cloudrider.entity.custom.CloudRiderEntity;
import com.leecrafts.cloudrider.entity.custom.CloudSteedEntity;
import com.leecrafts.cloudrider.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.leecrafts.cloudrider.entity.custom.CloudRiderEntity.CLOUD_LEVEL;
import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class ModEvents {

    @Mod.EventBusSubscriber(modid = CloudRider.MODID)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.register(ILightningCap.class);
            event.register(ICloudSteedItemCap.class);
            event.register(ICloudSteedEntityCap.class);
        }

        @SubscribeEvent
        public static void onAttachCapabilitiesEventLightning(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof LightningBolt lightningBolt && !lightningBolt.getCommandSenderWorld().isClientSide) {
                LightningCapProvider lightningCapProvider = new LightningCapProvider();
                event.addCapability(new ResourceLocation(CloudRider.MODID, "from_gray_cloud_steed"), lightningCapProvider);
                event.addListener(lightningCapProvider::invalidate);
            }
        }

        @SubscribeEvent
        public static void onAttachCapabilitiesEventCloudSteedItem(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof ItemEntity itemEntity && !itemEntity.getCommandSenderWorld().isClientSide) {
                // for some reason, itemEntity.getItem() always returns "air"
                CloudSteedItemCapProvider cloudSteedItemCapProvider = new CloudSteedItemCapProvider();
                event.addCapability(new ResourceLocation(CloudRider.MODID, "dropped_from_player"), cloudSteedItemCapProvider);
                event.addListener(cloudSteedItemCapProvider::invalidate);
            }
        }

        @SubscribeEvent
        public static void onAttachCapabilitiesEventCloudSteedEntity(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof CloudSteedEntity cloudSteedEntity && !cloudSteedEntity.getCommandSenderWorld().isClientSide) {
                CloudSteedEntityCapProvider cloudSteedEntityCapProvider = new CloudSteedEntityCapProvider();
                event.addCapability(new ResourceLocation(CloudRider.MODID, "player_passenger_had_logged_out"), cloudSteedEntityCapProvider);
                event.addListener(cloudSteedEntityCapProvider::invalidate);
            }
        }

        @SubscribeEvent
        public static void playerTick(LivingEvent.LivingTickEvent event) {
            if (event.getEntity() instanceof Player player && !player.level.isClientSide && !player.isDeadOrDying() && player.tickCount % TICKS_PER_SECOND == 0) {
                // Cloud rider spawning mechanics
                // A spawn attempt is made every second

                // Before checking amount of surrounding cloud riders, check if enough chunks have been loaded
                ServerLevel serverLevel = (ServerLevel) player.level;
                int chunkRadius = 6;
                int blocksPerChunk = 16;
                int playerX = player.getBlockX();
                int playerZ = player.getBlockZ();
                boolean chunksLoaded = true;
                for (int x = playerX - blocksPerChunk * chunkRadius; x <= playerX + blocksPerChunk * chunkRadius; x += blocksPerChunk) {
                    for (int z = playerZ - blocksPerChunk * chunkRadius; z <= playerZ + blocksPerChunk * chunkRadius; z += blocksPerChunk) {
                        if (!serverLevel.isLoaded(new BlockPos(x, player.getBlockY(), z))) {
                            chunksLoaded = false;
                        }
                    }
                }
                if (chunksLoaded) {
                    int spawnRadius = 128;
                    if (serverLevel.dimension() == Level.OVERWORLD &&
                            !player.isSpectator() &&
                            player.getY() > CLOUD_LEVEL - spawnRadius + 5 &&
                            player.getY() < CLOUD_LEVEL + spawnRadius - 5 &&
                            serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                        // Mob cap logic
                        int numSpawned = serverLevel.getEntitiesOfClass(
                                CloudRiderEntity.class,
                                (new AABB(player.blockPosition())).inflate(spawnRadius),
                                (cloudRiderEntity) -> !cloudRiderEntity.isPersistenceRequired()
                        ).size();
                        if (numSpawned < CloudRiderCommonConfigs.CLOUD_RIDER_SPAWN_CAP.get()) {
                            double xSpawn = player.getX() - spawnRadius +
                                    player.getRandom().nextInt(spawnRadius * 2);
                            double ySpawn = CLOUD_LEVEL - 4 + player.getRandom().nextInt(8);
                            double zSpawn = player.getZ() - spawnRadius +
                                    player.getRandom().nextInt(spawnRadius * 2);
                            BlockPos blockPos = new BlockPos(xSpawn, ySpawn, zSpawn);
                            if (player.distanceToSqr(xSpawn, ySpawn, zSpawn) >= 48 * 48 &&
                                    CloudRiderEntity.isValidSpawn(blockPos, serverLevel)) {
                                CloudRiderEntity cloudRiderEntity = ModEntityTypes.CLOUD_RIDER.get().spawn(serverLevel, blockPos, MobSpawnType.NATURAL);
                                if (cloudRiderEntity != null) {
                                    if (serverLevel.isThundering()) {
                                        cloudRiderEntity.setVariant(CloudRiderEntity.Type.GRAY);
                                    }
//                                    System.out.println("☁️ spawned in thanks to player " + player.getId());
                                }
                            }
                        }
                    }
                }

                // Dropped cloud steed items are more "magnetic"
                AABB aabb = player.getBoundingBox();
                Entity vehicle = player.getVehicle();
                if (vehicle != null && !vehicle.isRemoved()) {
                    aabb = aabb.minmax(vehicle.getBoundingBox());
                }
                for (ItemEntity itemEntity : player.level.getEntitiesOfClass(
                        ItemEntity.class, aabb.inflate(2), ForgeEvents::isCloudSteedItem)) {
                    if (!itemEntity.isRemoved()) {
                        itemEntity.playerTouch(player);
                    }
                }
            }
        }

        // Dropped cloud steeds float and have a name tag over them so that they are easier to find
        // They don't float when dropped by players (i.e. by pressing Q)
        @SubscribeEvent
        public static void droppedCloudSteedItemEvent(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof ItemEntity itemEntity && !itemEntity.level.isClientSide) {
                if (isCloudSteedItem(itemEntity)) {
                    MutableComponent mutableComponent = MutableComponent.create(ComponentContents.EMPTY);
                    mutableComponent = mutableComponent.setStyle(itemEntity.getItem().getRarity().getStyleModifier().apply(mutableComponent.getStyle()));
                    mutableComponent = mutableComponent.setStyle(mutableComponent.getStyle().applyFormat(ChatFormatting.BOLD));
                    mutableComponent = mutableComponent.append(itemEntity.getName());
                    itemEntity.setCustomName(mutableComponent);
                    itemEntity.setCustomNameVisible(true);
                    itemEntity.getCapability(ModCapabilities.CLOUD_STEED_ITEM_CAPABILITY).ifPresent(iCloudSteedItemCap -> {
                        CloudSteedItemCap cloudSteedItemCap = (CloudSteedItemCap) iCloudSteedItemCap;
                        if (!cloudSteedItemCap.droppedFromPlayer) {
                            itemEntity.setNoGravity(true);
                            itemEntity.setDeltaMovement(Vec3.ZERO);
                            itemEntity.setPickUpDelay(0);
                        }
                    });
                }
            }
        }

        @SubscribeEvent
        public static void playerDropItemEvent(ItemTossEvent event) {
            ItemEntity itemEntity = event.getEntity();
            if (!itemEntity.level.isClientSide && isCloudSteedItem(itemEntity)) {
                itemEntity.getCapability(ModCapabilities.CLOUD_STEED_ITEM_CAPABILITY).ifPresent(iCloudSteedItemCap -> {
                    CloudSteedItemCap cloudSteedItemCap = (CloudSteedItemCap) iCloudSteedItemCap;
                    cloudSteedItemCap.droppedFromPlayer = true;
                });
            }
        }

        // It would be unfair if a cloud steed item gets destroyed by lightning, especially if it is dropped from a
        // cloud rider that you killed with a channeling trident.
        // Any items hit by gray cloud steed lightning will not get destroyed.
        // It would also be unfair if a gray cloud steed gets destroyed by its own lightning.
        // And we don't want our precious villagers taking collateral damage from lightning caused by gray cloud steeds.
        @SubscribeEvent
        public static void lightningStrikeEvent(EntityStruckByLightningEvent event) {
            if (!event.getEntity().level.isClientSide) {
                LightningBolt lightningBolt = event.getLightning();
                lightningBolt.getCapability(ModCapabilities.LIGHTNING_CAPABILITY).ifPresent(iLightningCap -> {
                    LightningCap lightningCap = (LightningCap) iLightningCap;
                    if (event.getEntity() instanceof ItemEntity itemEntity) {
                        if (isCloudSteedItem(itemEntity) || lightningCap.fromGrayCloudSteed) {
                            event.setCanceled(true);
                        }
                    }
                    else if ((event.getEntity() instanceof CloudSteedEntity cloudSteedEntity && cloudSteedEntity.hasControllingPassenger()) ||
                            event.getEntity() instanceof Villager) {
                        if (lightningCap.fromGrayCloudSteed) {
                            event.setCanceled(true);
                        }
                    }
                });
            }
        }

        private static boolean isCloudSteedItem(ItemEntity itemEntity) {
            return itemEntity.getItem().is(ModItems.WHITE_CLOUD_STEED_ITEM.get()) ||
                    itemEntity.getItem().is(ModItems.GRAY_CLOUD_STEED_ITEM.get());
        }

        // If logging out while riding a cloud steed, the (player) passenger "dismounts" it, and calling Entity::discard does not actually work for some reason.
        // The cloud steed's replacement still gets added to the world, so if the player then rejoins the world, the vehicle gets duplicated.
        // To prevent this bug, this event listener must check if the player is riding the cloud steed while logging out.
        // See entity/custom/CloudSteedEntity.java
        @SubscribeEvent
        public static void playerLogOutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
            Player player = event.getEntity();
            if (!player.level.isClientSide) {
                if (player.getVehicle() instanceof CloudSteedEntity cloudSteedEntity) {
                    cloudSteedEntity.getCapability(ModCapabilities.CLOUD_STEED_ENTITY_CAPABILITY).ifPresent(iCloudSteedEntityCap -> {
                        CloudSteedEntityCap cloudSteedEntityCap = (CloudSteedEntityCap) iCloudSteedEntityCap;
                        cloudSteedEntityCap.playerPassengerHadLoggedOut = true;
                    });
                }
            }
        }

    }

    @Mod.EventBusSubscriber(modid = CloudRider.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {

        @SubscribeEvent
        public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
            event.put(ModEntityTypes.CLOUD_RIDER.get(), CloudRiderEntity.setAttributes());
        }

        // vanilla spawning mechanics just won't do
//        @SubscribeEvent
//        public static void onSpawnPlacementRegisterEvent(SpawnPlacementRegisterEvent event) {
//            event.register(ModEntityTypes.CLOUD_RIDER.get(),
//                    SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
//                    CloudRiderEntity::checkCloudRiderSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
//        }

    }

}
