package com.envyful.api.spigot.gui.item;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.gui.item.Displayable;
import com.envyful.api.player.EnvyPlayer;
import com.envyful.api.spigot.gui.factory.SpigotGuiFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 *
 * A static Spigot implementation of the {@link Displayable} interface. Meaning the itemstack cannot be changed once initially
 * set.
 *
 */
public class SpigotSimpleDisplayable implements Displayable {

    private final ItemStack itemStack;
    private final BiConsumer<EnvyPlayer<?>, ClickType> clickHandler;
    private final Consumer<EnvyPlayer<?>> updateHandler;
    private final int tickDelay;
    private final boolean async;
    private final boolean singleClick;
    private final long clickDelay;
    private final int lockOutClicks;

    private boolean clicked = false;
    private long lastClick = -1L;
    private int clicks = 0;

    public SpigotSimpleDisplayable(ItemStack itemStack, BiConsumer<EnvyPlayer<?>, ClickType> clickHandler,
                                  Consumer<EnvyPlayer<?>> updateHandler, int tickDelay, boolean async, boolean singleClick,
                                  long clickDelay, int lockOutClicks) {
        this.itemStack = itemStack;
        this.clickHandler = clickHandler;
        this.updateHandler = updateHandler;
        this.tickDelay = tickDelay;
        this.async = async;
        this.singleClick = singleClick;
        this.clickDelay = clickDelay;
        this.lockOutClicks = lockOutClicks;
    }

    @Override
    public void onClick(EnvyPlayer<?> player, ClickType clickType) {
        if (this.clicked && this.singleClick) {
            return;
        }

        this.clicked = true;

        if (++this.clicks >= this.lockOutClicks) {
            return;
        }

        if (this.lastClick != -1 && (System.currentTimeMillis() - this.lastClick) <= this.clickDelay) {
            return;
        }

        this.lastClick = System.currentTimeMillis();

        if (this.tickDelay <= 0) {
            if (this.async) {
                UtilConcurrency.runAsync(() -> this.clickHandler.accept(player, clickType));
            } else {
                Bukkit.getScheduler().runTask(((SpigotGuiFactory)GuiFactory.getPlatformFactory()).getPlugin(),
                        () -> this.clickHandler.accept(player, clickType));
            }

            return;
        }

        if (this.async) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(((SpigotGuiFactory)GuiFactory.getPlatformFactory()).getPlugin(),
                    () -> this.clickHandler.accept(player, clickType), this.tickDelay);
        } else {
            Bukkit.getScheduler().runTaskLater(((SpigotGuiFactory)GuiFactory.getPlatformFactory()).getPlugin(),
                    () -> this.clickHandler.accept(player, clickType), this.tickDelay);
        }
    }

    @Override
    public void update(EnvyPlayer<?> viewer) {
        this.updateHandler.accept(viewer);
    }

    public static final class Converter {
        public static ItemStack toNative(Displayable displayable) {
            if (!(displayable instanceof SpigotSimpleDisplayable)) {
                return new ItemStack(Material.AIR);
            }

            return ((SpigotSimpleDisplayable) displayable).itemStack;
        }
    }

    public static final class Builder implements Displayable.Builder<ItemStack> {

        private ItemStack itemStack;
        private BiConsumer<EnvyPlayer<?>, ClickType> clickHandler = (envyPlayer, clickType) -> {};
        private Consumer<EnvyPlayer<?>> updateHandler = envyPlayer -> {};
        private int tickDelay = 0;
        private boolean async = true;
        private boolean singleClick = false;
        private long clickDelay = 50L;
        private int lockOutClicks = 100;

        @Override
        public Displayable.Builder<ItemStack> itemStack(ItemStack itemStack) {
            this.itemStack = itemStack;
            return this;
        }

        @Override
        public Displayable.Builder<ItemStack> clickHandler(BiConsumer<EnvyPlayer<?>, ClickType> clickHandler) {
            this.clickHandler = clickHandler;
            return this;
        }

        @Override
        public Displayable.Builder<ItemStack> updateHandler(Consumer<EnvyPlayer<?>> updateHandler) {
            this.updateHandler = updateHandler;
            return this;
        }

        @Override
        public Displayable.Builder<ItemStack> delayTicks(int tickDelay) {
            this.tickDelay = tickDelay;
            return this;
        }

        @Override
        public Displayable.Builder<ItemStack> asyncClick(boolean async) {
            this.async = async;
            return this;
        }

        @Override
        public Displayable.Builder<ItemStack> singleClick(boolean singleClick) {
            this.singleClick = singleClick;
            return this;
        }

        @Override
        public Displayable.Builder<ItemStack> clickDelay(long milliseconds) {
            this.clickDelay = milliseconds;
            return this;
        }

        @Override
        public Displayable.Builder<ItemStack> lockOutClicks(int clickLockCount) {
            this.lockOutClicks = clickLockCount;
            return this;
        }

        @Override
        public Displayable build() {
            if (this.itemStack == null) {
                throw new RuntimeException("Cannot create displayable without itemstack");
            }

            return new SpigotSimpleDisplayable(this.itemStack, this.clickHandler, this.updateHandler, this.tickDelay,
                    this.async, this.singleClick, this.clickDelay, this.lockOutClicks);
        }
    }
}
