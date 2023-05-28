package com.raulmartinezr.kafkouch.connectors;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import com.raulmartinezr.kafkouch.couchdb.feed.ContinuousFeedEntry;
import com.raulmartinezr.kafkouch.util.ThreadSafeSetHandler;

public class RuntimeChangedResources {

  private LinkedBlockingQueue<ContinuousFeedEntry> changesQueue;
  private ThreadSafeSetHandler<String> changedDatabases;
  private String lastSeq = null;

  ReentrantLock lock = new ReentrantLock();

  /**
   *
   */
  public RuntimeChangedResources() {
    this.changesQueue = new LinkedBlockingQueue<ContinuousFeedEntry>();
    this.changedDatabases = new ThreadSafeSetHandler<String>();
  }

  public void registerChange(ContinuousFeedEntry entry) {
    lock.lock();
    try {
      this.changesQueue.add(entry);
      this.lastSeq = entry.getSeq();
      this.changedDatabases.add(entry.getDbName());
    } finally {
      lock.unlock();
    }
  }

  public ChangedResources getAndResetChanges() {
    lock.lock();
    try {
      ChangedResources changedResources =
          new ChangedResources(this.changedDatabases.getSetSnapshot(), this.lastSeq);
      this.changesQueue.clear();
      this.changedDatabases.clear();
      return changedResources;
    } finally {
      lock.unlock();
    }

  }

  /**
   * @return the lastSeq
   */
  public String getLastSeq() {
    return lastSeq;
  }

}
