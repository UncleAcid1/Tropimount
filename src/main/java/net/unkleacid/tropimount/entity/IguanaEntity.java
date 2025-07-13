package net.unkleacid.tropimount.entity;

import net.unkleacid.tropimount.Tropicraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.server.entity.HasTrackingParameters;
import net.modificationstation.stationapi.api.server.entity.MobSpawnDataProvider;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.TriState;

import static net.unkleacid.tropimount.util.MathHelper.cycleClampUp;
import static net.unkleacid.tropimount.util.MathHelper.pushBack;

@HasTrackingParameters(updatePeriod = 2, sendVelocity = TriState.TRUE, trackingDistance = 30)
public class IguanaEntity extends AttackingAnimalEntity implements MobSpawnDataProvider {
    public float tailAngle1;
    public float tailAngle2;
    public float tailAngle3;
    public static float TAIL_ANIMATION_SPEED = 1.0F;
    private float tailAnimationMultiplier = 1.0F;

    private boolean isJumping = false;

    public IguanaEntity(World world) {
        super(world);
        this.setBoundingBoxSpacing(2F, 0.5F);
        this.fireImmune = true;
        this.maxHealth = 100;
        this.noClip = false;
        this.stepHeight = 1.0F;
    }

    @Override
    public Identifier getHandlerIdentifier() {
        return Tropicraft.NAMESPACE.id("ridingiguana");
    }

    @Override
    public boolean interact(PlayerEntity player) {
        if (this.passenger != null && this.passenger instanceof PlayerEntity && this.passenger != player) {
            return true;
        }
        if (!this.world.isRemote) {
            player.setVehicle(this);
        }
        return true;
    }

    @Override
    public void tick() {
        PlayerEntity rider = this.passenger instanceof PlayerEntity ? (PlayerEntity) this.passenger : null;

        if (!this.world.isRemote && rider != null) {
            float speed = 0.6F;

            float targetYaw = rider.yaw;
            float delta = targetYaw - this.yaw;
            while (delta < -180) delta += 360;
            while (delta > 180) delta -= 360;
            float rotationFactor = 0.12F;
            this.yaw += delta * rotationFactor;
            this.prevYaw = this.yaw;

            this.lastBodyYaw = this.bodyYaw;
            this.bodyYaw = this.yaw;
            this.setRotation(this.yaw, 0);
            this.pitch = 0;

            double dirX = -Math.sin(Math.toRadians(this.yaw));
            double dirZ =  Math.cos(Math.toRadians(this.yaw));

            boolean moving = Math.abs(rider.velocityX) > 0.005 || Math.abs(rider.velocityZ) > 0.005;
            if (moving) {
                this.velocityX = dirX * speed;
                this.velocityZ = dirZ * speed;
            } else {
                this.velocityX = 0;
                this.velocityZ = 0;
            }

            if (this.onGround && this.isJumping) {
                this.velocityY = 0.5D;
                this.onGround = false;
            }

            this.velocityY -= 0.08D;
            this.velocityY *= 0.98D;
            this.move(this.velocityX, this.velocityY, this.velocityZ);

            if (this.y < 0) this.y = 0;
        } else {
            super.tick();
        }

        updatePassengerPosition();
        animateTail();

        float walk = (float) Math.sqrt(this.velocityX * this.velocityX + this.velocityZ * this.velocityZ);
        this.walkAnimationSpeed = walk;
        this.walkAnimationProgress += walk;
    }

    private void animateTail() {
        tailAnimationMultiplier = TAIL_ANIMATION_SPEED;
        if (Math.abs(this.velocityX) > 0.05D || Math.abs(this.velocityZ) > 0.05D) {
            tailAnimationMultiplier = TAIL_ANIMATION_SPEED * 0.2F;
            tailAngle1 = pushBack(tailAngle1, 80F, 100F, 2.5F);
        }
        tailAngle1 = cycleClampUp(tailAngle1, 1.2F * tailAnimationMultiplier, 360F);
        tailAngle2 = cycleClampUp(tailAngle2, 1.5F * tailAnimationMultiplier, 360F);
        tailAngle2 = pushBack(tailAngle2, tailAngle1 - 15F, tailAngle1 + 15F);
        tailAngle3 = cycleClampUp(tailAngle3, 1.7F * tailAnimationMultiplier, 360F);
        tailAngle3 = pushBack(tailAngle3, tailAngle2 - 15F, tailAngle2 + 15F);
    }

    @Override
    public void updatePassengerPosition() {
        if (this.passenger != null) {
            double forwardOffset = 1.0D;
            double offsetX = -Math.sin(Math.toRadians(this.yaw)) * forwardOffset;
            double offsetZ =  Math.cos(Math.toRadians(this.yaw)) * forwardOffset;
            this.passenger.setPosition(
                    this.x + offsetX,
                    this.y + this.getPassengerRidingHeight(),
                    this.z + offsetZ
            );
        }
    }

    @Override
    public double getPassengerRidingHeight() {
        return this.height + 2.0D;
    }

    @Override
    protected void dropItems() {
        // No drops
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putBoolean("IsJumping", this.isJumping);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.isJumping = nbt.getBoolean("IsJumping");
    }
}
