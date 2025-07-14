package net.unkleacid.tropimount.client;

import net.unkleacid.tropimount.Tropimount;
import net.modificationstation.stationapi.api.client.event.texture.TextureRegisterEvent;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlases;
import net.modificationstation.stationapi.api.client.texture.atlas.ExpandableAtlas;
import net.modificationstation.stationapi.api.mod.entrypoint.Entrypoint;
import net.mine_diver.unsafeevents.listener.EventListener;

@Entrypoint
public class TropimountClient {

    @EventListener
    public void onTextureRegister(TextureRegisterEvent event) {
        ExpandableAtlas atlas = Atlases.getTerrain();
        atlas.addTexture(Tropimount.NAMESPACE.id("ridingiguana"));
    }
}
