package de.laparudi.rudicrates.utils;

import de.laparudi.rudicrates.RudiCrates;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

public enum Messages {
    
    PREFIX {
        @Override
        public String toString() {
            final String version = Bukkit.getBukkitVersion();
            if (version.contains("1.16") || version.contains("1.17")) {
                return RudiCrates.getPlugin().getTranslationUtils().hexColorString(
                        new String[] { "#4F4F4F", "#981f00", "#a32201", "#ae2601", "#b92a01", "#c52d02", "#d03102", "#dc3503", "#e73803", "#f33c03", "#ff4004", "#4F4F4F" },
                        new Character[] { '[', 'R', 'u', 'd', 'i', 'C', 'r', 'a', 't', 'e', 's', ']', ' ' } );
            }
            return "§7[§cRudiCrates§7] §r";
        }
    },
   
    PREFIX_OLD {
        @Override
        public String toString() {
            final String version = Bukkit.getBukkitVersion();
            if (version.contains("1.16") || version.contains("1.17")) {
                    return ChatColor.of("#494242") + "[" +
                            ChatColor.of("#900000") + "R" + ChatColor.of("#9D0000") + "u" + ChatColor.of("#A00000") + "d" +
                            ChatColor.of("#A40000") + "i" + ChatColor.of("#AA0000") + "C" + ChatColor.of("#AD0000") + "r" +
                            ChatColor.of("#B00000") + "a" + ChatColor.of("#B40000") + "t" + ChatColor.of("#BB0000") + "e" +
                            ChatColor.of("#C00000") + "s" + ChatColor.of("#494242") + "] " + ChatColor.RESET;
            }
            return "§7[§cRudiCrates§7] §r";
        }
    },
    
    NO_PERMISSION {
        @Override
        public String toString() {
            return PREFIX + "§fDu hast nicht genügend Rechte.";
        }
    },

    UNKNOWN_CRATE {
        @Override
        public String toString() {
            return PREFIX + "§fUnbekannte Crate. (Achte auf Groß- und Kleinschreibung)";
        }
    },

    NO_NUMBER {
        @Override
        public String toString() {
            return PREFIX + "§7Du musst einen Zahlenwert angeben.";
        }
    },

    UNKNOWN_ID {
        @Override
        public String toString() {
            return PREFIX + "§7Diese ID wurde nicht gefunden.";
        }
    },
    
    SYNTAX_EDITCHANCE {
        @Override
        public String toString() {
            return PREFIX + "§7Benutze: §f/editchance §7<§fCrate§7> <§fItem-ID§7> <§fNeue Chance in %§7>";
        }
    },
    
    SYNTAX_ADDTOCRATE {
        @Override
        public String toString() {
            return PREFIX + "§7Benutze: §f/addtocrate §7<§fCrate§7> <§fGewinnchance in %§7>";
        }
    },

    SYNTAX_REMOVEFROMCRATE {
        @Override
        public String toString() {
            return PREFIX + "§7Benutze: §f/removefromcrate §7<§fCrate§7> <§fItem-ID§7>";
        }
    },

    SYNTAX_BINDCOMMAND {
        @Override
        public String toString() {
            return PREFIX + "§7Benutze: §f/bindcommand §7<§fCrate§7> <§fItem-ID§7> <§fBefehl§7>";
        }
    },
    
    SYNTAX_SETVIRTUAL {
        @Override
        public String toString() {
            return PREFIX + "§6Wenn ein Item virtuell ist bekommt der Spieler das Item nicht ins Inventar und es wird nur der Befehl ausgeführt §7(§f/bindcommand§7) §6sinnvoll für Geld oder Crates." + "\n" + 
                    PREFIX + "§7Benutze §f/setvirtual §7<§fCrate§7> <§fItem-ID§7> <§ftrue§7/§ffalse§7>";
        }
    },

    SYNTAX_SETLIMITED {
        @Override
        public String toString() {
            return PREFIX + "§7Benutze §f/setlimited §7<§fCrate§7> <§fItem-ID§7> <§fMenge§7>";
        }
    },
    
    PLAYER_NOT_FOUND {
        @Override
        public String toString() {
            return PREFIX + "§fDieser Spieler wurde nicht gefunden.";
        }
    }
}
