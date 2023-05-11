package com.raulmartinezr.kafkouch.couchdb.feed;

import com.fasterxml.jackson.annotation.JsonValue;

public class ContinuousFeedEntry {

  public enum Type {
    CREATED("created"),
    UPDATED("updated"),
    DELETED("deleted");

    private final String value;

    Type(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }
  }

  private String db_name;
  private Type type;
  private long seq;

  // Getters and setters

  public String getDbName() {
    return db_name;
  }

  public void setDbName(String db_name) {
    this.db_name = db_name;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public long getSeq() {
    return seq;
  }

  public void setSeq(long seq) {
    this.seq = seq;
  }
}
