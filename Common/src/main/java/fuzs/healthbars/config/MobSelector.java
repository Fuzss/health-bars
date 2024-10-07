package fuzs.healthbars.config;

import fuzs.puzzleslib.api.core.v1.CommonAbstractions;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public enum MobSelector {
    ALL {
        @Override
        public boolean isValid(LivingEntity entity) {
            return true;
        }
    },
    TAMED {
        @Override
        public boolean isValid(LivingEntity entity) {
            return entity instanceof OwnableEntity tamableAnimal && tamableAnimal.getOwnerUUID() != null;
        }
    },
    TAMED_ONLY_OWNER {
        @Override
        public boolean isValid(LivingEntity entity) {
            UUID owner = Minecraft.getInstance().player.getUUID();
            return entity instanceof OwnableEntity tamableAnimal && owner.equals(tamableAnimal.getOwnerUUID());
        }
    },
    PLAYER {
        @Override
        public boolean isValid(LivingEntity entity) {
            return entity instanceof Player;
        }
    },
    MONSTER {
        @Override
        public boolean isValid(LivingEntity entity) {
            return entity instanceof Enemy || !entity.getType().getCategory().isFriendly();
        }
    },
    BOSS {
        @Override
        public boolean isValid(LivingEntity entity) {
            return CommonAbstractions.INSTANCE.isBossMob(entity.getType());
        }
    },
    MOUNT {
        @Override
        public boolean isValid(LivingEntity entity) {
            return entity instanceof Saddleable saddleable && saddleable.isSaddleable();
        }
    };

    public abstract boolean isValid(LivingEntity entity);
}
