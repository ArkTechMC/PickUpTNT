package com.iafenvoy.pickuptnt.mixin;

import com.iafenvoy.pickuptnt.Constants;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void onTickTnt(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (!stack.isOf(Items.TNT) || stack.isEmpty()) return;
        if (stack.getNbt() != null && stack.getNbt().contains(Constants.FUSE)) {
            int fuse = stack.getNbt().getInt(Constants.FUSE);
            if (fuse == 0) {
                if (!world.isClient)
                    world.createExplosion(null, entity.getX(), entity.getBodyY(0.0625), entity.getZ(), 4.0F * stack.getCount(), World.ExplosionSourceType.TNT);
                stack.setCount(0);
            } else stack.getNbt().putInt(Constants.FUSE, fuse - 1);
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void handleTntThrow(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);
        if (!stack.isOf(Items.TNT) || stack.isEmpty()) return;
        if (stack.getNbt() != null && stack.getNbt().contains(Constants.FUSE) && !world.isClient) {//Ender Pearl Logic
            TntEntity tnt = new TntEntity(world, user.getX(), user.getBodyY(0.0625), user.getZ(), user);
            float pitch = user.getPitch();
            float yaw = user.getYaw();
            float f = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
            float g = -MathHelper.sin((pitch + 0.0F) * 0.017453292F);
            float h = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
            Random random = ((EntityAccessor) tnt).getRandom();
            Vec3d vec3d1 = new Vec3d(f, g, h).normalize().add(random.nextTriangular(0.0, 0.0172275), random.nextTriangular(0.0, 0.0172275), random.nextTriangular(0.0, 0.0172275)).multiply(1.5F);
            tnt.setYaw((float) (MathHelper.atan2(vec3d1.x, vec3d1.z) * 57.2957763671875));
            tnt.setPitch((float) (MathHelper.atan2(vec3d1.y, vec3d1.horizontalLength()) * 57.2957763671875));
            tnt.prevYaw = tnt.getYaw();
            tnt.prevPitch = tnt.getPitch();
            Vec3d vec3d = user.getVelocity();
            tnt.setVelocity(vec3d1.add(vec3d.x, user.isOnGround() ? 0.0 : vec3d.y, vec3d.z));
            tnt.setFuse(stack.getNbt().getInt(Constants.FUSE));
            world.spawnEntity(tnt);
            stack.decrement(1);
            cir.setReturnValue(TypedActionResult.success(stack));
        }
    }
}
