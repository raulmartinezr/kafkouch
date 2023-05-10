package com.raulmartinezr.kafkouch.couchdb.feed;

public class ContinuousFeedEntry {
  private String db_name;
  private String type;
  private long seq;

  // Getters and setters

  public String getDbName() {
    return db_name;
  }

  public void setDbName(String db_name) {
    this.db_name = db_name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public long getSeq() {
    return seq;
  }

  public void setSeq(long seq) {
    this.seq = seq;
  }
}
