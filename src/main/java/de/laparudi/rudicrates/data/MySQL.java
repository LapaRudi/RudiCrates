package de.laparudi.rudicrates.data;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.language.Language;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MySQL {

    private final String host, username, password, database;
    private final int port;
    private @Getter Connection connection;

    public MySQL(final String host, final int port, final String username, final String password, final String database) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = port;
        this.database = database;
    }

    public void create() {
        try {
            this.setConnection(DriverManager.getConnection("jdbc:mysql://" + host + ":" + port, username, password));
            this.setUpdate("CREATE DATABASE IF NOT EXISTS `" + database + "`");
            this.disconnect();

        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void connect() {
        try {
            if (this.isConnected() && !this.getConnection().isClosed()) {
                Language.send(Bukkit.getConsoleSender(), "mysql.already_connected");
                return;
            }

            this.setConnection(DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password));
            Language.send(Bukkit.getConsoleSender(), "mysql.connected");

        } catch (final SQLException exception) {
            Language.send(Bukkit.getConsoleSender(), "mysql.could_not_connect");
            Language.send(Bukkit.getConsoleSender(), "mysql.disabled");
            Bukkit.getScheduler().runTaskLater(RudiCrates.getPlugin(), RudiCrates.getPlugin()::unloadPlugin, 60);
        }
    }

    public void disconnect() {
        if (!this.isConnected()) return;

        try {
            this.getConnection().close();
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void setUpdate(final String value) {
        if (!this.isConnected()) this.connect();

        try (final PreparedStatement statement = this.getConnection().prepareStatement(value)) {
            statement.executeUpdate();

        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
    }

    public boolean playerExists(final UUID uuid) {
        try (final PreparedStatement statement = this.getConnection().prepareStatement("SELECT * FROM `rudicrates` WHERE `player_uuid` = '" + uuid + "'");
             final ResultSet result = statement.executeQuery()) {

            if (result.next()) {
                return result.getString("player_uuid") != null;
            }

        } catch (final SQLException exception) {
            exception.printStackTrace();
        }

        return false;
    }

    public void createPlayer(final UUID uuid) {
        if (this.playerExists(uuid)) return;
        CompletableFuture.runAsync( () -> {
            
            try (final PreparedStatement statement = this.getConnection()
                    .prepareStatement("INSERT INTO `rudicrates` (`player_uuid`, " + crateNames() + ") VALUES (?, " + crateAmount() + ")")) {

                statement.setString(1, uuid.toString());
                statement.executeUpdate();

            } catch (final SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void updateTable() {
        CompletableFuture.runAsync(() -> {
            for (final Crate crate : CrateUtils.getCrates()) {

                try (final PreparedStatement statement = this.getConnection()
                        .prepareStatement("ALTER TABLE `rudicrates` ADD COLUMN IF NOT EXISTS`" + crate.getName() + "` INT")) {
                    
                    statement.executeUpdate();
                    
                } catch (final SQLException exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    public void resetPlayer(final UUID uuid) {
        if (!this.playerExists(uuid)) return;
        CompletableFuture.runAsync(() -> {
            
            try (final PreparedStatement statement = this.getConnection()
                    .prepareStatement("UPDATE `rudicrates` SET " + crateNames() + " = 0 WHERE `player_uuid` = '" + uuid + "'")) {
                
                statement.executeUpdate();
                
            } catch (final SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public int getValue(final UUID uuid, final Crate crate) {
        if (!this.playerExists(uuid)) return 0;

        try (final PreparedStatement statement = this.getConnection()
                .prepareStatement("SELECT * FROM `rudicrates` WHERE `player_uuid` = '" + uuid + "'");

             final ResultSet result = statement.executeQuery()) {

            if (result.next()) {
                return result.getInt(crate.getName());
            }

        } catch (final SQLException exception) {
            exception.printStackTrace();
        }

        return 0;
    }

    public void setValue(final UUID uuid, final Crate crate, final int value) {
        if (!playerExists(uuid)) return;
        CompletableFuture.runAsync(() -> {
            
            try (final PreparedStatement statement = this.getConnection()
                    .prepareStatement("UPDATE `rudicrates` SET `" + crate.getName() + "` = " + value + " WHERE `player_uuid` = '" + uuid + "'")) {
                
                statement.executeUpdate();
                
            } catch (final SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public void setConnection(final Connection connection) {
        this.connection = connection;
    }

    private String crateAmount() {
        final int size = CrateUtils.getCrates().length;
        if (size == 1) return "0";
        final StringBuilder builder = new StringBuilder("0");

        for (int i = 1; i < size; i++) {
            builder.append(", ").append("0");
        }

        return builder.toString();
    }

    private String crateNames() {
        int count = 0;
        final int size = CrateUtils.getCrates().length;
        final StringBuilder builder = new StringBuilder();

        for (final Crate crate : CrateUtils.getCrates()) {
            count++;
            builder.append("`").append(crate.getName()).append("`");
            if (count < size) builder.append(", ");
        }

        return builder.toString();
    }

    public String createTableString() {
        int count = 0;
        final int size = CrateUtils.getCrates().length;
        final StringBuilder builder = new StringBuilder();

        for (final Crate crate : CrateUtils.getCrates()) {
            count++;
            builder.append("`").append(crate.getName()).append("` INT");
            if (count < size) builder.append(", ");
        }

        return builder.toString();
    }
}
