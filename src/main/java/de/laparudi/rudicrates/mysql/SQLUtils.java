package de.laparudi.rudicrates.mysql;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;

import java.util.UUID;

public class SQLUtils {

    public static void addCrates(UUID uuid, Crate crate, int amount) {
        final int oldAmount = RudiCrates.getPlugin().getMySQL().getValue(uuid, crate);
        RudiCrates.getPlugin().getMySQL().setValue(uuid, crate, oldAmount + amount);
    }
    
    public static void removeCrates(UUID uuid, Crate crate, int amount) {
        final int oldAmount = RudiCrates.getPlugin().getMySQL().getValue(uuid, crate);
        final int newAmount = oldAmount - amount;
        
        if(newAmount < 0) return;
        RudiCrates.getPlugin().getMySQL().setValue(uuid, crate, newAmount);
    }
    
    public static int getCrateAmount(UUID uuid, Crate crate) {
        return RudiCrates.getPlugin().getMySQL().getValue(uuid, crate);
    }
    
    public static void setCrateAmount(UUID uuid, Crate crate, int amount) {
        RudiCrates.getPlugin().getMySQL().setValue(uuid, crate, amount);
    }
}
