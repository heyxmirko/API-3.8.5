package com.envyful.api.reforged.pixelmon.storage;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.economy.IPixelmonBankAccount;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 *
 * Static factory class used for easily obtaining player pixelmon information such as their PC
 *
 */
public class UtilPixelmonPlayer {

    /**
     *
     * Gets the player's bank account
     *
     * @param player The player
     * @return The bank account
     */
    public static IPixelmonBankAccount getBank(EntityPlayerMP player) {
        return Pixelmon.moneyManager.getBankAccount(player.getUniqueID()).orElse(null);
    }

    /**
     *
     * Gets the player's party storage
     *
     * @param player The player to get the party of
     * @return The player's party
     */
    public static PlayerPartyStorage getParty(EntityPlayerMP player) {
        return Pixelmon.storageManager.getParty(player);
    }

    /**
     *
     * Get the player's pc storage
     *
     * @param player The player
     * @return The pc
     */
    public static PCStorage getPC(EntityPlayerMP player) {
        return Pixelmon.storageManager.getPCForPlayer(player);
    }

}
