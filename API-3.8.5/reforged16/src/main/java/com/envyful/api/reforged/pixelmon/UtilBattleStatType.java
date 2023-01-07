package com.envyful.api.reforged.pixelmon;

import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;

import java.util.Optional;

public class UtilBattleStatType {

    public static Optional<BattleStatsType> convert(String identifier) {
        BattleStatsType statType = null;

        if (identifier.equalsIgnoreCase("hp")) {
            statType = BattleStatsType.HP;
        } else if (identifier.equalsIgnoreCase("atk")) {
            statType = BattleStatsType.ATTACK;
        } else if (identifier.equalsIgnoreCase("def")) {
            statType = BattleStatsType.DEFENSE;
        } else if (identifier.equalsIgnoreCase("spa")) {
            statType = BattleStatsType.SPECIAL_ATTACK;
        } else if (identifier.equalsIgnoreCase("spd")) {
            statType = BattleStatsType.SPECIAL_DEFENSE;
        } else if (identifier.equalsIgnoreCase("spe")) {
            statType = BattleStatsType.SPEED;
        }

        return Optional.ofNullable(statType);
    }

}
