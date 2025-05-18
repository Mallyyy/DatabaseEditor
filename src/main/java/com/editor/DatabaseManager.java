package com.editor;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;

public class DatabaseManager {

  public enum DatabaseType {
    DONATION_REWARDS("morpheu3_donationrewards"),
    GAME_SHOPS("morpheu3_gameshops"),
    NPCS("morpheu3_npcs"),
    PLAYERS("morpheu3_players"),
    SECURITY("morpheu3_security"),
    SERVER("morpheu3_server"),
    STORE("morpheu3_store"),
    REFERRALS("morpheu3_refs"),
    VOTE("morpheu3_vote");

    private final String dbName;
    DatabaseType(String dbName) { this.dbName = dbName; }
    public String getDbName() { return dbName; }
  }

  private static final String HOST = "198.12.15.138";
  private static final String USER = "morpheu3_admin";
  private static final String PASSWORD = "morpheusadmin123";

  private static final Map<DatabaseType, HikariDataSource> POOLS = new EnumMap<>(DatabaseType.class);

  static {
    for (DatabaseType type : DatabaseType.values()) {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl("jdbc:mysql://" + HOST + ":3306/" + type.getDbName() + "?useSSL=false&autoReconnect=true");
      config.setUsername(USER);
      config.setPassword(PASSWORD);
      config.setMaximumPoolSize(10);
      config.setMinimumIdle(2);
      config.setIdleTimeout(60000);
      config.setConnectionTimeout(10000);
      config.setLeakDetectionThreshold(10000);
      config.setPoolName(type.name() + "_POOL");
      POOLS.put(type, new HikariDataSource(config));
    }
  }

  public static Connection getConnection(DatabaseType type) throws SQLException {
    System.out.println("Connected to " + type.getDbName());
    return POOLS.get(type).getConnection();
  }

  public static void shutdownAll() {
    for (HikariDataSource ds : POOLS.values()) {
      if (ds != null && !ds.isClosed()) {
        ds.close();
      }
    }
  }
}
