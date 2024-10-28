package com.iafenvoy.pickuptnt.mixin;

import com.iafenvoy.pickuptnt.Constants;
import com.iafenvoy.pickuptnt.PickUpTnt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void afterClientInit(RunArgs args, CallbackInfo ci) {
        ModelPredicateProviderRegistryAccessor.register(Items.TNT, new Identifier(PickUpTnt.MOD_ID, "burning"), (stack, world, entity, seed) -> stack.getNbt() != null && stack.getNbt().contains(Constants.FUSE) ? 1 : 0);
    }
}
