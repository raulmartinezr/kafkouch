package com.raulmartinezr.kafkouch.connectors;

import java.util.Set;

public class ChangedResources {

  private Set<String> changedDatabases;
  private String lastSeq = null;

  /**
   *
   */
  public ChangedResources(Set<String> changedDatabases) {
    this.changedDatabases = changedDatabases;
  }

  /**
   *
   */
  public ChangedResources() {}

  /**
   * @param changedDatabases
   * @param lastSeq
   */
  public ChangedResources(Set<String> changedDatabases, String lastSeq) {
    this.changedDatabases = changedDatabases;
    this.lastSeq = lastSeq;
  }

  /**
   * @return the lastSeq
   */
  public String getLastSeq() {
    return lastSeq;
  }

  /**
   * @return the changedDatabases
   */
  public Set<String> getChangedDatabases() {
    return changedDatabases;
  }

  /**
   * @param changedDatabases the changedDatabases to set
   */
  public void setChangedDatabases(Set<String> changedDatabases) {
    this.changedDatabases = changedDatabases;
  }

  /**
   * @param lastSeq the lastSeq to set
   */
  public void setLastSeq(String lastSeq) {
    this.lastSeq = lastSeq;
  }

}
