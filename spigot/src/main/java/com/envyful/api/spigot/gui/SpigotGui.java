package com.envyful.api.spigot.gui;

import com.envyful.api.gui.Gui;
import com.envyful.api.gui.item.Displayable;
import com.envyful.api.gui.pane.Pane;
import com.envyful.api.player.EnvyPlayer;
import com.envyful.api.spigot.gui.close.SpigotCloseConsumer;
import com.envyful.api.spigot.gui.item.SpigotSimpleDisplayable;
import com.envyful.api.spigot.gui.pane.SpigotSimplePane;
import com.envyful.api.spigot.player.SpigotEnvyPlayer;
import com.envyful.api.spigot.player.SpigotPlayerManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

/**
 *
 * Spigot implementation of the {@link Gui} interface.
 *
 */
public class SpigotGui implements Gui {

    private final Component title;
    private final int height;
    private final SpigotPlayerManager playerManager;
    private final SpigotCloseConsumer closeConsumer;
    private final SpigotSimplePane parentPane;
    private final SpigotSimplePane[] panes;

    SpigotGui(Component title, int height, SpigotPlayerManager playerManager, SpigotCloseConsumer closeConsumer,
              Pane... panes) {
        this.title = title;
        this.height = height;
        this.playerManager = playerManager;
        this.closeConsumer = closeConsumer;
        this.parentPane = (SpigotSimplePane) new SpigotSimplePane.Builder().height(height).topLeftX(0).topLeftY(0).width(9).build();
        this.panes = new SpigotSimplePane[panes.length];
        int i = 0;

        for (Pane pane : panes) {
            if (!(pane instanceof SpigotSimplePane)) {
                continue;
            }

            this.panes[i] = (SpigotSimplePane) pane;
            ++i;
        }
    }

    @Override
    public void open(EnvyPlayer<?> player) {
        if (!(player instanceof SpigotEnvyPlayer)) {
            return;
        }

        Player parent = (Player)player.getParent();
        Inventory inventory = Bukkit.createInventory(null, this.height * 9, this.title);

        for (SpigotSimplePane pane : panes) {
            if (pane == null) {
                continue;
            }

            for (int y = 0; y < pane.getItems().length; y++) {
                Displayable[] row = pane.getItems()[y];

                if (row == null) {
                    continue;
                }

                for (int x = 0; x < row.length; x++) {
                    Displayable item = row[x];

                    if (item == null) {
                        continue;
                    }

                    int index = pane.updateIndex((9 * y) + x);

                    inventory.setItem(index, SpigotSimpleDisplayable.Converter.toNative(item));
                }
            }
        }

        SpigotGuiTracker.addGui(player, this, inventory);
        parent.openInventory(inventory);
    }

    public static class Listener implements org.bukkit.event.Listener  {

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player)) {
                return;
            }

            SpigotGuiTracker.InventoryDetails details = SpigotGuiTracker.getDetails((Player) event.getWhoClicked());

            if (details == null) {
                return;
            }

            Player player = (Player)event.getWhoClicked();
            Displayable.ClickType clickType = this.convert(event.getClick());

            for (SpigotSimplePane pane : details.getGui().panes) {
                if (pane == null) {
                    continue;
                }

                for (int y = 0; y < pane.getItems().length; y++) {
                    Displayable[] row = pane.getItems()[y];

                    if (row == null) {
                        continue;
                    }

                    for (int x = 0; x < row.length; x++) {
                        Displayable item = row[x];

                        if (item == null) {
                            continue;
                        }

                        int index = pane.updateIndex((9 * y) + x);

                        if (index != event.getSlot()) {
                            continue;
                        }

                        item.onClick(details.getGui().playerManager.getPlayer(player), clickType);
                    }
                }
            }
        }

        private Displayable.ClickType convert(ClickType clickType) {
            try {
                return Displayable.ClickType.valueOf(clickType.name());
            } catch (Exception e) {
                return Displayable.ClickType.RIGHT;
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            if (!(event.getPlayer() instanceof Player)) {
                return;
            }

            SpigotGuiTracker.InventoryDetails details = SpigotGuiTracker.getDetails((Player) event.getPlayer());

            if (details == null) {
                return;
            }

            details.getGui().closeConsumer.handle(details.getGui().playerManager.getPlayer((Player) event.getPlayer()));
            SpigotGuiTracker.removePlayer(details.getGui().playerManager.getPlayer((Player) event.getPlayer()));
        }
    }
}
