package net.unkleacid.tropimount;

import net.modificationstation.stationapi.api.util.Identifier;
import net.unkleacid.tropimount.entity.IguanaEntity;
import net.unkleacid.tropimount.entity.renderer.IguanaRenderer;
import net.modificationstation.stationapi.api.client.event.render.entity.EntityRendererRegisterEvent;
import net.modificationstation.stationapi.api.event.entity.EntityRegister;
import net.modificationstation.stationapi.api.event.registry.EntityHandlerRegistryEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.Entrypoint;
import net.modificationstation.stationapi.api.util.Namespace;
import net.mine_diver.unsafeevents.listener.EventListener;

@Entrypoint
public class Tropimount {

    public static final Namespace NAMESPACE = Namespace.of("tropimount");

    @EventListener
    public void registerEntities(EntityRegister event) {
        event.register(IguanaEntity.class, "ridingiguana");
    }

    @EventListener
    public void registerEntityHandlers(EntityHandlerRegistryEvent event) {
        event.register(Identifier.of("ridingiguana"), (world, x, y, z) -> {
            IguanaEntity entity = new IguanaEntity(world);
            return entity;
        });
    }

    @EventListener
    public void registerEntityRenderers(EntityRendererRegisterEvent event) {
        event.renderers.put(IguanaEntity.class, new IguanaRenderer());
    }
}
