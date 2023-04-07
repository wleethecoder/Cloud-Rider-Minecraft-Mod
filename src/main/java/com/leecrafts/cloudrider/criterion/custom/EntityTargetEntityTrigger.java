package com.leecrafts.cloudrider.criterion.custom;

import com.google.gson.JsonObject;
import com.leecrafts.cloudrider.CloudRider;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

public class EntityTargetEntityTrigger extends SimpleCriterionTrigger<EntityTargetEntityTrigger.TriggerInstance> {

    // This whole thing was created just for that one advancement

    static final ResourceLocation ID = new ResourceLocation(CloudRider.MODID, "entity_target_entity");

    @Override
    protected EntityTargetEntityTrigger.@NotNull TriggerInstance createInstance(@NotNull JsonObject pJson, EntityPredicate.@NotNull Composite pPlayer, @NotNull DeserializationContext pContext) {
        EntityPredicate.Composite composite = EntityPredicate.Composite.fromJson(pJson, "entity", pContext);
        return new EntityTargetEntityTrigger.TriggerInstance(pPlayer, composite);
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return ID;
    }

    public void trigger(ServerPlayer pPlayer, Entity pEntity) {
        LootContext lootContext = EntityPredicate.createContext(pPlayer, pEntity);
        this.trigger(pPlayer, (trigger) -> trigger.matches(lootContext));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        private final EntityPredicate.Composite entity;

        public TriggerInstance(EntityPredicate.Composite pPlayer, EntityPredicate.Composite pEntity) {
            super(EntityTargetEntityTrigger.ID, pPlayer);
            this.entity = pEntity;
        }

        public boolean matches(LootContext pLootContext) {
            return this.entity.matches(pLootContext);
        }

        public @NotNull JsonObject serializeToJson(@NotNull SerializationContext pConditions) {
            JsonObject jsonObject = super.serializeToJson(pConditions);
            jsonObject.add("entity", this.entity.toJson(pConditions));
            return jsonObject;
        }

    }

}
