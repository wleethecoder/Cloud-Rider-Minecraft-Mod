package com.leecrafts.cloudrider.event;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.capability.ModCapabilities;
import com.leecrafts.cloudrider.capability.cloudriderentity.CloudRiderCap;
import com.leecrafts.cloudrider.capability.cloudriderentity.CloudRiderCapProvider;
import com.leecrafts.cloudrider.capability.cloudriderentity.ICloudRiderCap;
import com.leecrafts.cloudrider.capability.player.IPlayerCap;
import com.leecrafts.cloudrider.capability.player.PlayerCap;
import com.leecrafts.cloudrider.capability.player.PlayerCapProvider;
import com.leecrafts.cloudrider.config.CloudRiderCommonConfigs;
import com.leecrafts.cloudrider.entity.ModEntityTypes;
import com.leecrafts.cloudrider.entity.custom.CloudRiderEntity;
import com.leecrafts.cloudrider.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.leecrafts.cloudrider.capability.player.PlayerCap.SPAWN_RADIUS;
import static com.leecrafts.cloudrider.entity.custom.CloudRiderEntity.CLOUD_LEVEL;

public class ModEvents {

    @Mod.EventBusSubscriber(modid = CloudRider.MODID)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.register(IPlayerCap.class);
            event.register(ICloudRiderCap.class);
        }

        @SubscribeEvent
        public static void onAttachCapabilitiesEventPlayer(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player player && !player.getCommandSenderWorld().isClientSide) {
                PlayerCapProvider playerCapProvider = new PlayerCapProvider();
                event.addCapability(new ResourceLocation(CloudRider.MODID, "num_cloud_riders"), playerCapProvider);
            }
        }

        @SubscribeEvent
        public static void onAttachCapabilitiesEventCloudRider(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof CloudRiderEntity cloudRiderEntity && !cloudRiderEntity.getCommandSenderWorld().isClientSide) {
                CloudRiderCapProvider cloudRiderCapProvider = new CloudRiderCapProvider();
                event.addCapability(new ResourceLocation(CloudRider.MODID, "player_id"), cloudRiderCapProvider);
                event.addListener(cloudRiderCapProvider::invalidate);
            }
        }

        @SubscribeEvent
        public static void playerTick(LivingEvent.LivingTickEvent event) {
            if (event.getEntity() instanceof Player player && !player.level.isClientSide) {
                player.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iPlayerCap -> {
                    PlayerCap playerCap = (PlayerCap) iPlayerCap;
                    playerCap.numCloudRiders = Math.max(playerCap.numCloudRiders, 0);
                    ServerLevel serverLevel = (ServerLevel) player.getLevel();
                    if (playerCap.numCloudRiders < CloudRiderCommonConfigs.CLOUD_RIDER_SPAWN_CAP.get() &&
                            serverLevel.dimension() == Level.OVERWORLD &&
                            !player.isSpectator() &&
                            player.getY() > CLOUD_LEVEL - SPAWN_RADIUS + 5 &&
                            player.getY() < CLOUD_LEVEL + SPAWN_RADIUS - 5 &&
                            serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                        double xSpawn = player.getX() - SPAWN_RADIUS +
                                player.getRandom().nextInt(SPAWN_RADIUS * 2);
                        double zSpawn = player.getZ() - SPAWN_RADIUS +
                                player.getRandom().nextInt(SPAWN_RADIUS * 2);
                        BlockPos blockPos = new BlockPos(xSpawn, CLOUD_LEVEL, zSpawn);
                        if (player.distanceToSqr(xSpawn, CLOUD_LEVEL, zSpawn) >= 576 &&
                                CloudRiderEntity.isValidSpawn(blockPos, serverLevel)) {
                            EntityType<CloudRiderEntity> entityType = !serverLevel.isThundering() ? ModEntityTypes.WHITE_CLOUD_RIDER.get() : ModEntityTypes.GRAY_CLOUD_RIDER.get();
                            CloudRiderEntity cloudRiderEntity = entityType.spawn(serverLevel, blockPos, MobSpawnType.NATURAL);
                            if (cloudRiderEntity != null) {
                                cloudRiderEntity.getCapability(ModCapabilities.CLOUD_RIDER_CAPABILITY).ifPresent(iCloudRiderCap -> {
                                    CloudRiderCap cloudRiderCap = (CloudRiderCap) iCloudRiderCap;
                                    cloudRiderCap.playerId = player.getId();
                                    playerCap.numCloudRiders++;
                                    System.out.println("☁️ spawned in thanks to player " + cloudRiderCap.playerId);
                                });
                            }
                        }
                    }
                });
            }
        }

        @SubscribeEvent
        public static void playerCloneEvent(PlayerEvent.Clone event) {
            Player newPlayer = event.getEntity();
            Player oldPlayer = event.getOriginal();
            newPlayer.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iNewPlayerCap -> {
                oldPlayer.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iOldPlayerCap -> {
                    PlayerCap newPlayerCap = (PlayerCap) iNewPlayerCap;
                    PlayerCap oldPlayerCap = (PlayerCap) iOldPlayerCap;
                    newPlayerCap.numCloudRiders = oldPlayerCap.numCloudRiders;
                });
            });
            oldPlayer.invalidateCaps();
        }

//        @SubscribeEvent
//        public static void test(LivingEvent.LivingTickEvent event) {
//            if (event.getEntity() instanceof Player player && !player.level.isClientSide) {
//                player.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iPlayerCap -> {
//                    PlayerCap playerCap = (PlayerCap) iPlayerCap;
//                    System.out.println(playerCap.numCloudRiders);
//                });
//            }
//        }
        @SubscribeEvent
        public static void droppedCloudSteedItemEvent(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof ItemEntity itemEntity && !itemEntity.level.isClientSide) {
                if (itemEntity.getItem().is(ModItems.WHITE_CLOUD_STEED_ITEM.get()) ||
                        itemEntity.getItem().is(ModItems.GRAY_CLOUD_STEED_ITEM.get())) {
                    itemEntity.setNoGravity(true);
                    itemEntity.setDeltaMovement(Vec3.ZERO);
                    MutableComponent mutableComponent = MutableComponent.create(ComponentContents.EMPTY);
                    mutableComponent = mutableComponent.setStyle(itemEntity.getItem().getRarity().getStyleModifier().apply(mutableComponent.getStyle()));
                    mutableComponent = mutableComponent.setStyle(mutableComponent.getStyle().applyFormat(ChatFormatting.BOLD));
                    mutableComponent = mutableComponent.append(itemEntity.getName());
                    itemEntity.setCustomName(mutableComponent);
                    itemEntity.setCustomNameVisible(true);
                }
            }
        }

    }

    @Mod.EventBusSubscriber(modid = CloudRider.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {

        @SubscribeEvent
        public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
            event.put(ModEntityTypes.WHITE_CLOUD_RIDER.get(), CloudRiderEntity.setAttributes());
            event.put(ModEntityTypes.GRAY_CLOUD_RIDER.get(), CloudRiderEntity.setAttributes());
        }

        // Normal spawning mechanics just won't do
//        @SubscribeEvent
//        public static void onSpawnPlacementRegisterEvent(SpawnPlacementRegisterEvent event) {
//            event.register(ModEntityTypes.CLOUD_RIDER.get(),
//                    SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
//                    CloudRiderEntity::checkCloudRiderSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
//        }

    }

}
