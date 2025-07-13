package net.unkleacid.tropimount.entity.renderer;

import net.unkleacid.tropimount.entity.IguanaEntity;
import net.unkleacid.tropimount.entity.model.IguanaModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class IguanaRenderer extends TropiEntityRenderer {

    public IguanaRenderer() {
        super(new IguanaModel(), 0.5F, "/assets/tropimount/stationapi/textures/entity/ridingiguana.png");
    }

    @Override
    protected void applyScale(LivingEntity entity, float partialTickTime) {
        if (entity instanceof IguanaEntity) {
            GL11.glScalef(4f, 4f, 4f);
        }
    }
}
