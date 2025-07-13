package net.unkleacid.tropimount.entity;

import net.unkleacid.tropimount.Tropimount;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

    private static final byte BIT_TAMED = 4;

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
        return Tropimount.NAMESPACE.id("ridingiguana");
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(16, (byte) 0); // status flags (byte)
        this.dataTracker.startTracking(17, "");     // owner name
    }

    public boolean isTamed() {
        return (this.dataTracker.getByte(16) & BIT_TAMED) != 0;
    }

    public void setTamed(boolean tamed) {
        byte flags = this.dataTracker.getByte(16);
        if (tamed) {
            flags |= BIT_TAMED;
        } else {
            flags &= ~BIT_TAMED;
        }
        this.dataTracker.set(16, flags);
    }

    public String getOwnerName() {
        return this.dataTracker.getString(17);
    }

    public void setOwnerName(String owner) {
        this.dataTracker.set(17, owner);
    }

    @Override
    public boolean interact(PlayerEntity player) {
        if (this.passenger != null && this.passenger instanceof PlayerEntity && this.passenger != player) {
            return true;
        }

        ItemStack held = player.inventory.getSelectedItem();

        if (!this.isTamed()) {
            if (held != null && held.itemId == Item.GOLDEN_APPLE.id) {
                --held.count;
                if (held.count <= 0) {
                    player.inventory.setStack(player.inventory.selectedSlot, null);
                }
                if (!this.world.isRemote) {
                    this.setTamed(true);
                    this.setOwnerName(player.name);
                    this.noClip = false;
                    this.world.broadcastEntityEvent(this, (byte) 7);
                }
                return true;
            }
        } else {
            if (!this.world.isRemote) {
                player.setVehicle(this);
            }
            return true;
        }

        return super.interact(player);
    }

    @Override
    public void tick() {
        PlayerEntity rider = this.passenger instanceof PlayerEntity ? (PlayerEntity) this.passenger : null;

        if (!this.world.isRemote && rider != null) {
            float forwardSpeed = 27.6F;
            float sideSpeed = 10.3F;
            float backSpeed = 10.3F;

            float forward = 0f;
            float strafe = 0f;

            double dx = rider.velocityX;
            double dz = rider.velocityZ;

            float yawRad = (float) Math.toRadians(rider.yaw);

            double forwardMotion = -Math.sin(yawRad) * dx + Math.cos(yawRad) * dz;
            double strafeMotion = Math.cos(yawRad) * dx + Math.sin(yawRad) * dz;

            forward = (float) forwardMotion;
            strafe = (float) strafeMotion;

            double motionX = 0;
            double motionZ = 0;

            if (forward > 0) {
                motionX += -Math.sin(yawRad) * forwardSpeed * forward;
                motionZ += Math.cos(yawRad) * forwardSpeed * forward;
            } else if (forward < 0) {
                motionX += -Math.sin(yawRad) * backSpeed * forward;
                motionZ += Math.cos(yawRad) * backSpeed * forward;
            }

            if (strafe != 0) {
                double strafeYaw = yawRad + Math.PI / 2 * (strafe > 0 ? 1 : -1);
                motionX += Math.sin(strafeYaw) * sideSpeed * Math.abs(strafe);
                motionZ += -Math.cos(strafeYaw) * sideSpeed * Math.abs(strafe);
            }

            float targetYaw = rider.yaw;
            float delta = targetYaw - this.yaw;
            while (delta < -180) delta += 360;
            while (delta > 180) delta -= 360;
            float rotationFactor = 0.22F;
            this.yaw += delta * rotationFactor;
            this.prevYaw = this.yaw;

            this.lastBodyYaw = this.bodyYaw;
            this.bodyYaw = this.yaw;
            this.setRotation(this.yaw, 0);
            this.pitch = 0;

            this.velocityX = motionX;
            this.velocityZ = motionZ;

            if (this.onGround && this.isJumping) {
                this.velocityY = 0.5D;
                this.onGround = false;
            }

            this.velocityY -= 0.08D;
            this.velocityY *= 0.98D;
            this.move(this.velocityX, this.velocityY, this.velocityZ);

            if (this.y < 0) this.y = 0;

            this.prevX = this.x;
            this.prevY = this.y;
            this.prevZ = this.z;
            this.prevYaw = this.yaw;
            this.prevPitch = this.pitch;

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
            double offsetZ = Math.cos(Math.toRadians(this.yaw)) * forwardOffset;
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
        nbt.putBoolean("Tamed", this.isTamed());
        nbt.putString("Owner", this.getOwnerName());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.isJumping = nbt.getBoolean("IsJumping");
        this.setTamed(nbt.getBoolean("Tamed"));
        this.setOwnerName(nbt.getString("Owner"));
    }

    @Override
    protected boolean canDespawn() {
        return !this.isTamed();
    }

    @Override
    protected void handleFallDamage(float fallDistance) {
    }
}
