package com.leecrafts.cloudrider.event;

import com.leecrafts.cloudrider.CloudRider;
import com.leecrafts.cloudrider.capability.ModCapabilities;
import com.leecrafts.cloudrider.capability.cloudriderentity.CloudRiderCap;
import com.leecrafts.cloudrider.capability.cloudriderentity.CloudRiderCapProvider;
import com.leecrafts.cloudrider.capability.cloudriderentity.ICloudRiderCap;
import com.leecrafts.cloudrider.capability.player.IPlayerCap;
import com.leecrafts.cloudrider.capability.player.PlayerCap;
import com.leecrafts.cloudrider.capability.player.PlayerCapProvider;
import com.leecrafts.cloudrider.capability.target.ITargetVelocityCap;
import com.leecrafts.cloudrider.capability.target.TargetVelocityCapProvider;
import com.leecrafts.cloudrider.entity.ModEntityTypes;
import com.leecrafts.cloudrider.entity.custom.CloudRiderEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.leecrafts.cloudrider.capability.player.PlayerCap.SPAWN_RADIUS;
import static com.leecrafts.cloudrider.entity.custom.CloudRiderEntity.*;

public class ModEvents {

    @Mod.EventBusSubscriber(modid = CloudRider.MODID)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.register(ITargetVelocityCap.class);
            event.register(IPlayerCap.class);
            event.register(ICloudRiderCap.class);
        }

        @SubscribeEvent
        public static void onAttachCapabilitiesEventTargetVelocity(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof LivingEntity livingEntity && !livingEntity.getCommandSenderWorld().isClientSide) {
                TargetVelocityCapProvider targetVelocityCapProvider = new TargetVelocityCapProvider();
                event.addCapability(new ResourceLocation(CloudRider.MODID, "velocity_when_targeted_by_cloud_rider"), targetVelocityCapProvider);
                if (!(livingEntity instanceof Player)) {
                    event.addListener(targetVelocityCapProvider::invalidate);
                }
            }
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

//        @SubscribeEvent
//        public static void targetTick(LivingEvent.LivingTickEvent event) {
//
//        }

        // TODO 2 new capabilities:
        // player capability that tracks how many cloudriders it caused the spawning of
        // cloud rider capability that tracks the player id
        // TODO to maintain or not to maintain player capability??
        @SubscribeEvent
        public static void playerTick(TickEvent.PlayerTickEvent event) {
            if (event.player instanceof ServerPlayer serverPlayer && !serverPlayer.level.isClientSide) {
                serverPlayer.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iPlayerCap -> {
                    PlayerCap playerCap = (PlayerCap) iPlayerCap;
                    playerCap.numCloudRiders = Math.max(playerCap.numCloudRiders, 0);
                    ServerLevel serverLevel = serverPlayer.getLevel();
                    if (playerCap.numCloudRiders < MAX_SPAWN_PER_PLAYER &&
                            serverLevel.dimension() == Level.OVERWORLD &&
                            !serverPlayer.isSpectator() &&
                            serverPlayer.getY() > CLOUD_LEVEL - SPAWN_RADIUS + 5 &&
                            serverPlayer.getY() < CLOUD_LEVEL + SPAWN_RADIUS - 5 &&
                            serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                        double xSpawn = serverPlayer.getX() - SPAWN_RADIUS +
                                serverPlayer.getRandom().nextInt(SPAWN_RADIUS * 2);
                        double zSpawn = serverPlayer.getZ() - SPAWN_RADIUS +
                                serverPlayer.getRandom().nextInt(SPAWN_RADIUS * 2);
                        BlockPos blockPos = new BlockPos(xSpawn, CLOUD_LEVEL, zSpawn);
                        if (serverPlayer.distanceToSqr(xSpawn, CLOUD_LEVEL, zSpawn) > 576 &&
                                CloudRiderEntity.isValidSpawn(blockPos, serverLevel)) {
                            CloudRiderEntity cloudRiderEntity = ModEntityTypes.CLOUD_RIDER.get().spawn(serverLevel, blockPos, MobSpawnType.NATURAL);
                            if (cloudRiderEntity != null) {
                                cloudRiderEntity.getCapability(ModCapabilities.CLOUD_RIDER_CAPABILITY).ifPresent(iCloudRiderCap -> {
                                    CloudRiderCap cloudRiderCap = (CloudRiderCap) iCloudRiderCap;
                                    cloudRiderCap.playerId = serverPlayer.getId();
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


    }

    @Mod.EventBusSubscriber(modid = CloudRider.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {

        @SubscribeEvent
        public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
            event.put(ModEntityTypes.CLOUD_RIDER.get(), CloudRiderEntity.setAttributes());
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
