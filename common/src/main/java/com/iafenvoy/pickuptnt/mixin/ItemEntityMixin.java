package com.iafenvoy.pickuptnt.mixin;

import com.iafenvoy.pickuptnt.Constants;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract ItemStack getStack();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onItemEntityTick(CallbackInfo ci) {
        ItemStack stack = this.getStack();
        if (!stack.isOf(Items.TNT) || stack.isEmpty()) return;
        if (stack.getNbt() != null && stack.getNbt().contains(Constants.FUSE)) {
            int fuse = stack.getNbt().getInt(Constants.FUSE);
            if (fuse == 0) {
                if (!this.getWorld().isClient)
                    this.getWorld().createExplosion(null, this.getX(), this.getBodyY(0.0625), this.getZ(), 4.0F * stack.getCount(), World.ExplosionSourceType.TNT);
                this.discard();
                ci.cancel();
            } else stack.getNbt().putInt(Constants.FUSE, fuse - 1);
        }
    }
}
