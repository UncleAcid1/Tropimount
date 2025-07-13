package net.unkleacid.tropimount;

import net.unkleacid.tropimount.entity.IguanaEntity;
import net.unkleacid.tropimount.entity.renderer.IguanaRenderer;
import net.modificationstation.stationapi.api.client.event.render.entity.EntityRendererRegisterEvent;
import net.modificationstation.stationapi.api.client.event.texture.TextureRegisterEvent;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlases;
import net.modificationstation.stationapi.api.client.texture.atlas.ExpandableAtlas;
import net.modificationstation.stationapi.api.event.entity.EntityRegister;
import net.modificationstation.stationapi.api.mod.entrypoint.Entrypoint;
import net.modificationstation.stationapi.api.util.Namespace;
import net.mine_diver.unsafeevents.listener.EventListener;

@Entrypoint
public class Tropimount {

    @Entrypoint.Namespace
    public static Namespace NAMESPACE;


    @EventListener
    public void registerEntities(EntityRegister event) {
        event.register(IguanaEntity.class, "ridingiguana");
    }

    @EventListener
    public void registerEntityRenderers(EntityRendererRegisterEvent event) {
        event.renderers.put(IguanaEntity.class, new IguanaRenderer());
    }

    @EventListener
    public void onTextureRegister(TextureRegisterEvent event) {
        ExpandableAtlas atlas = Atlases.getTerrain();
        atlas.addTexture(NAMESPACE.id("ridingiguana"));
    }
}