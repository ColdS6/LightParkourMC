package com.colds.doublejump;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class DoubleJumpClient implements ClientModInitializer {

    // Сила второго прыжка.
    // Можно поставить 0.42D как обычный прыжок,
    // или 0.5D / 0.6D для более высокого прыжка.
    private static final double DOUBLE_JUMP_POWER = 0.5D;

    // Сколько тиков игрок должен находиться в воздухе,
    // прежде чем сможет сделать двойной прыжок.
    //
    // 1 тик = 1/20 секунды.
    // 2 тика = 0.1 секунды.
    //
    // Если двойной прыжок всё ещё срабатывает вместе с первым прыжком,
    // увеличь это значение до 3 или 4.
    private static final int MIN_AIR_TICKS = 3;

    private boolean wasJumpPressed = false;
    private boolean canDoubleJump = true;
    private boolean wasOnGround = true;
    private int airTicks = 0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(Minecraft client) {
        LocalPlayer player = client.player;

        if (player == null || client.level == null) {
            wasJumpPressed = false;
            wasOnGround = true;
            airTicks = 0;
            return;
        }

        boolean jumpPressed = client.options.keyJump.isDown();

        // Сбрасываем двойной прыжок,
        // когда игрок на земле, на лестнице, в воде, на элитрах или уже летает.
        if (player.onGround()
                || player.onClimbable()
                || player.isInWater()
                || player.isFallFlying()
                || player.getAbilities().flying) {
            canDoubleJump = true;
            airTicks = 0;
        } else {
            airTicks++;
        }

        boolean canUseDoubleJump = canDoubleJump
                && airTicks >= MIN_AIR_TICKS
                && !player.onGround()
                && !wasOnGround
                && !player.onClimbable()
                && !player.isInWater()
                && !player.isFallFlying()
                && !player.isSwimming()
                && player.getVehicle() == null
                && !player.isSleeping()
                && !player.isSpectator()
                && !player.getAbilities().flying;

        if (jumpPressed && !wasJumpPressed && canUseDoubleJump) {
            Vec3 motion = player.getDeltaMovement();

            player.setDeltaMovement(new Vec3(
                    motion.x,
                    DOUBLE_JUMP_POWER,
                    motion.z));

            player.fallDistance = 0.0F;
            canDoubleJump = false;
        }

        wasJumpPressed = jumpPressed;
        wasOnGround = player.onGround();
    }
}