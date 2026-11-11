java
package com.example.oskbdmod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod("oskbdmod")
public class OnScreenKeyboardMod {
    public OnScreenKeyboardMod() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.register(new ClientEventHandler());
        }
    }
}