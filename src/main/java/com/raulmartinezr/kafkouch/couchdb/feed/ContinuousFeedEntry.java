package com.raulmartinezr.kafkouch.couchdb.feed;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public class ContinuousFeedEntry {

  public enum Type {
    CREATED("created"), UPDATED("updated"), DELETED("deleted");

    private final String value;

    Type(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }
  }

  @JsonProperty("db_name")
  private String dbName;
  @JsonProperty("type")
  private Type type;
  @JsonProperty("seq")
  private String seq;

  // Getters and setters

  public String getDbName() {
    return dbName;
  }

  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public String getSeq() {
    return seq;
  }

  public void setSeq(String seq) {
    this.seq = seq;
  }
}
