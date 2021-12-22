package de.laparudi.rudicrates.data;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MySQL {
    
    private final String host, username, password, database;
    private final int port;
    private Connection connection;
    
    public MySQL(String host, int port, String username, String password, String database) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = port;
        this.database = database;
    }
    
    public void create() {
        try {
            setConnection(DriverManager.getConnection("jdbc:mysql://" + host + ":" + port, username, password));
            setUpdate("CREATE DATABASE IF NOT EXISTS `" + database + "`");
            disconnect();
            
        } catch (SQLException ignored) {}
    }
    
    public void connect() {
        try {
            if (isConnected() && !getConnection().isClosed()) {
                Bukkit.getConsoleSender().sendMessage(RudiCrates.getPlugin().getLanguage().mysqlAlreadyConnected);
                return;
            }
            setConnection(DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password));
            Bukkit.getConsoleSender().sendMessage(RudiCrates.getPlugin().getLanguage().mysqlConnected);

        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(RudiCrates.getPlugin().getLanguage().mysqlCouldNotConnect);
            Bukkit.getConsoleSender().sendMessage(RudiCrates.getPlugin().getLanguage().mysqlDisabled);
            Bukkit.getScheduler().runTaskLater(RudiCrates.getPlugin(), RudiCrates.getPlugin()::unloadPlugin, 60);
        }
    }

    public void disconnect() {
        if (!isConnected()) {
            return;
        }
        
        try {
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void setUpdate(String value) {
        checkConnection();
        try (PreparedStatement statement = connection.prepareStatement(value)) {
            statement.executeUpdate();

        } catch ( Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean playerExists(UUID uuid) {
        try (PreparedStatement statement = RudiCrates.getPlugin().getMySQL().getConnection()
                .prepareStatement("SELECT * FROM `rudicrates` WHERE `player_uuid` = '" + uuid + "'");

             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getString("player_uuid") != null;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    
    public void createPlayer(UUID uuid) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(RudiCrates.getPlugin(), () -> {
            if (!playerExists(uuid)) {
                
                try (PreparedStatement statement = RudiCrates.getPlugin().getMySQL().getConnection()
                        .prepareStatement("INSERT INTO `rudicrates` (`player_uuid`, " + crateNames() + ") VALUES (?, " + crateAmount() + ")" )) {

                    statement.setString(1, uuid.toString());
                    statement.executeUpdate();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void updateTable() {
        CompletableFuture.runAsync( () -> {
            for (Crate crate : RudiCrates.getPlugin().getCrateUtils().getCrates()) {

                try (PreparedStatement statement = connection.prepareStatement("ALTER TABLE `rudicrates` ADD COLUMN IF NOT EXISTS`" + crate.getName() + "` INT")) {
                    statement.executeUpdate();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    public void resetPlayer(UUID uuid) {
        if (!playerExists(uuid)) return;

        Bukkit.getScheduler().runTaskAsynchronously(RudiCrates.getPlugin(), () -> {
            try (PreparedStatement statement = RudiCrates.getPlugin().getMySQL().getConnection()
                    .prepareStatement("UPDATE `rudicrates` SET " + crateNames() + " = 0 WHERE `player_uuid` = '" + uuid + "'")) {

                statement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    public int getValue(UUID uuid, Crate crate) {
        if (playerExists(uuid)) {

            try (PreparedStatement statement = RudiCrates.getPlugin().getMySQL().getConnection()
                    .prepareStatement("SELECT * FROM `rudicrates` WHERE `player_uuid` = '" + uuid + "'");

                 ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(crate.getName());
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return 0;
    }

    public void setValue(UUID uuid, Crate crate, int value) {
        Bukkit.getScheduler().runTaskAsynchronously(RudiCrates.getPlugin(), () -> {
            if (playerExists(uuid)) {

                try (PreparedStatement statement = RudiCrates.getPlugin().getMySQL().getConnection()
                        .prepareStatement("UPDATE `rudicrates` SET `" + crate.getName() + "` = " + value + " WHERE `player_uuid` = '" + uuid + "'")) {

                    statement.executeUpdate();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void checkConnection() {
        try {
            if (!isConnected() || !this.connection.isValid(10) || this.connection.isClosed()) connect();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection( Connection connection ) {
        this.connection = connection;
    }

    private String crateAmount() {
        int size = RudiCrates.getPlugin().getCrateUtils().getCrates().length;
        if(size == 1) return "0";
        StringBuilder builder = new StringBuilder("0");

        for(int i = 1; i < size; i++) {
            builder.append(", ");
            builder.append("0");
        }

        return builder.toString();
    }

    private String crateNames() {
        int count = 0;
        int size = RudiCrates.getPlugin().getCrateUtils().getCrates().length;
        StringBuilder builder = new StringBuilder();

        for(Crate crate : RudiCrates.getPlugin().getCrateUtils().getCrates()) {
            count++;
            builder.append("`").append(crate.getName()).append("`");
            if(count < size) builder.append(", ");
        }

        return builder.toString();
    }

    public String createTableString() {
        int count = 0;
        int size = RudiCrates.getPlugin().getCrateUtils().getCrates().length;
        StringBuilder builder = new StringBuilder();

        for(Crate crate : RudiCrates.getPlugin().getCrateUtils().getCrates()) {
            count++;
            builder.append("`").append(crate.getName()).append("` INT");
            if(count < size) builder.append(", ");
        }

        return builder.toString();
    }
}
