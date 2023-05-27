package com.raulmartinezr.kafkouch.util;

public class EnabledCollectionsSet {

  private String database;
  private EnabledCollectionsType enabledCollectionsType;

  public EnabledCollectionsSet(String database, EnabledCollectionsType enabledCollectionsType) {
    this.database = database;
    this.enabledCollectionsType = enabledCollectionsType;
  }

  /**
   * @return the database
   */
  public String getDatabase() {
    return database;
  }

  /**
   * @return the enabledCollectionsType
   */
  public EnabledCollectionsType getEnabledCollectionsType() {
    return enabledCollectionsType;
  }

  /**
   * @param database the database to set
   */
  public void setDatabase(String database) {
    this.database = database;
  }

  /**
   * @param enabledCollectionsType the enabledCollectionsType to set
   */
  public void setEnabledCollectionsType(EnabledCollectionsType enabledCollectionsType) {
    this.enabledCollectionsType = enabledCollectionsType;
  }

}
