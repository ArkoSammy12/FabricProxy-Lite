package one.oktw.mixin.hack;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.fabricmc.fabric.impl.networking.server.ServerLoginNetworkAddon;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import one.oktw.VelocityLib;
import one.oktw.mixin.ServerLoginNetworkHandler_DelayHello;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandler_EarlySendPacket implements ServerLoginNetworkHandler_DelayHello {
    @Shadow
    @Nullable GameProfile profile;
    private LoginHelloC2SPacket helloPacket = null;

    @Inject(method = "onHello", at = @At(value = "HEAD"), cancellable = true)
    private void skipKeyPacket(LoginHelloC2SPacket packet, CallbackInfo ci) {
        if (profile != null && profile.isComplete()) return; // Already receive profile form velocity.

        helloPacket = packet;
        ((ServerLoginNetworkAddon) ((NetworkHandlerExtensions) this).getAddon()).sendPacket(VelocityLib.PLAYER_INFO_CHANNEL, PacketByteBufs.empty());
        ci.cancel();
    }

    @Nullable
    @Override
    public LoginHelloC2SPacket delayedHelloPacket() {
        LoginHelloC2SPacket packet = helloPacket;
        helloPacket = null; // Only read once.
        return packet;
    }
}
