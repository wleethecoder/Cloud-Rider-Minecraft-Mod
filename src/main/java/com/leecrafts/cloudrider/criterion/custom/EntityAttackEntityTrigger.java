package com.leecrafts.cloudrider.criterion.custom;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;

public class EntityAttackEntityTrigger extends SimpleCriterionTrigger<EntityAttackEntityTrigger.TriggerInstance> {
    @Override
    protected EntityAttackEntityTrigger.TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pPlayer, DeserializationContext pContext) {
        return null;
    }

    @Override
    public ResourceLocation getId() {
        return null;
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        public TriggerInstance(ResourceLocation pCriterion, EntityPredicate.Composite pPlayer) {
            super(pCriterion, pPlayer);
        }

    }

}
