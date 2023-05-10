package com.raulmartinezr.kafkouch.connectors;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import com.raulmartinezr.kafkouch.couchdb.CouchdbChangesFeedReader;
import com.raulmartinezr.kafkouch.couchdb.CouchdbChangesFeedReader.CouchdbChangesFeedReaderBuilder;
import com.raulmartinezr.kafkouch.couchdb.CouchdbClient.CouchdbAuthMethod;
import com.raulmartinezr.kafkouch.couchdb.feed.ContinuousFeedEntry;

public class CouchdbSourceConnector extends SourceConnector {

  BlockingQueue<ContinuousFeedEntry> changesQueue = null;
  CouchdbChangesFeedReader couchdbChangesFeedReader = null;
  ReconfigurationThread reconfigurationThread = null;

  @Override
  public String version() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'version'");
  }

  @Override
  public ConfigDef config() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'config'");
  }

  @Override
  public void start(Map<String, String> arg0) {
    /*
     * Starts connector
     */
    changesQueue = new LinkedBlockingQueue<ContinuousFeedEntry>();
    this.couchdbChangesFeedReader = new CouchdbChangesFeedReaderBuilder()
        .setUrl(arg0.get("couchdb.url")).setUsername(arg0.get("couchdb.username"))
        .setPassword(arg0.get("couchdb.password")).setAuthMethod(CouchdbAuthMethod.COOKIE)
        .setConnect(true).setSince(arg0.get("since")).setChangesQueue(changesQueue).build();

    this.couchdbChangesFeedReader.startReadingChangesFeed();
    this.manageReconfigurationsBasedOnChanges();
  }

  private void manageReconfigurationsBasedOnChanges() {
    this.reconfigurationThread = new ReconfigurationThread(this.changesQueue);
    this.reconfigurationThread.start();
  }

  @Override
  public void stop() {
    this.couchdbChangesFeedReader.stopReadingChangesFeed();
    this.reconfigurationThread.stopThread();
    // Wait for the thread to complete (optional)
    try {
      this.reconfigurationThread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Class<? extends Task> taskClass() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'taskClass'");
  }

  @Override
  public List<Map<String, String>> taskConfigs(int arg0) {
    /*
     * Each entry is the configuration for a single task. Then we can control here the numner of
     * tasks and the configuration for each one of them.
     */
    throw new UnsupportedOperationException("Unimplemented method 'taskConfigs'");
  }

}


class ReconfigurationThread extends Thread {
  private volatile boolean shouldStop = false;

  private BlockingQueue<?> changesQueue = null;

  /**
   *
   */
  public ReconfigurationThread(BlockingQueue<?> changesQueue) {
    this.changesQueue = changesQueue;
  }

  public void stopThread() {
    shouldStop = true;
  }

  @Override
  public void run() {
    while (!shouldStop) {
      // Perform the thread's main logic here
      try {
        Object record = this.changesQueue.take(); // Wait for a record to be available
      } catch (InterruptedException e) {
        // Handle interrupted exception (if required)
        Thread.currentThread().interrupt();
      }
    }

  }
}
