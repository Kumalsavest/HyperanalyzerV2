package com.kum.hyperanalyzer.mixins;

import com.kum.hyperanalyzer.tracking.PowerupTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    @Inject(method = "handleEntityMetadata", at = @At(value = "HEAD"))
    public void handleEntityMetadata(S1CPacketEntityMetadata packetIn, CallbackInfo callbackInfo) {
        if (packetIn == null)
            return;
        List<DataWatcher.WatchableObject> list = packetIn.func_149376_c();
        if (list == null || list.isEmpty())
            return;

        if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().theWorld != null) {
            Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(packetIn.getEntityId());
            if (entity instanceof EntityArmorStand) {
                for (DataWatcher.WatchableObject watchableObject : new ArrayList<>(list)) {
                    if (watchableObject != null
                            && watchableObject.getObjectType() == 4
                            && watchableObject.getDataValueId() == 2) {
                        if (watchableObject.getObject() instanceof String) {
                            String name = net.minecraft.util.StringUtils
                                    .stripControlCodes((String) watchableObject.getObject()).trim();
                            PowerupTracker.onArmorStandSpawn(name, packetIn.getEntityId());
                        }
                    }
                }
            }
        }
    }
}
