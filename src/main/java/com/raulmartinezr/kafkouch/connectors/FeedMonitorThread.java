package com.raulmartinezr.kafkouch.connectors;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.connect.connector.ConnectorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raulmartinezr.kafkouch.connectors.config.source.CouchdbSourceConfig;
import com.raulmartinezr.kafkouch.couchdb.CouchdbChangesFeedReader;
import com.raulmartinezr.kafkouch.couchdb.CouchdbClient;
import com.raulmartinezr.kafkouch.couchdb.CouchdbChangesFeedReader.CouchdbChangesFeedReaderBuilder;

class FeedMonitorThread extends Thread {

  private static final Logger log = LoggerFactory.getLogger(FeedMonitorThread.class);

  private RuntimeChangedResources RuntimeChangedResources;
  private CountDownLatch shutdownLatch;
  private ConnectorContext context;
  private CouchdbChangesFeedReader couchdbChangesFeedReader = null;
  private CouchdbSourceConfig sourceConfig;
  private String since;
  private CouchdbClient couchdbClient;

  /**
   *
   */
  public FeedMonitorThread(RuntimeChangedResources RuntimeChangedResources,
      ConnectorContext context, CouchdbSourceConfig sourceConfig, String since,
      CouchdbClient couchdbClient) {
    this.RuntimeChangedResources = RuntimeChangedResources;
    this.context = context;
    this.sourceConfig = sourceConfig;
    this.couchdbClient = couchdbClient;
    this.since = since;
    this.shutdownLatch = new CountDownLatch(1);
  }

  @Override
  public void run() {
    log.info("Starting thread monitoring global changes feed.");
    this.startReadingFeed();
    while (shutdownLatch.getCount() > 0) {
      // Perform the thread's main logic here
      if (updateTasks()) {
        context.requestTaskReconfiguration(); // Request reconfiguration of the task
      }
      try {
        shutdownLatch.await(500, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        log.warn("Feed monitoring thread interrupted: ", e);
      }
    }
  }

  private boolean updateTasks() {
    // Object record = this.changesQueue.take(); // Wait for a record to be
    // available
    // When we have changes in a database and no task is running it
    // Special case: task handling several databses: Could be thant the task already finished this DB
    return false;
  }

  public void shutdown() {
    log.info("Shutting down thread monitoring global changes feed.");
    this.couchdbChangesFeedReader.stopReadingChangesFeed();
    shutdownLatch.countDown();
  }

  private void startReadingFeed() {
    this.couchdbChangesFeedReader = new CouchdbChangesFeedReaderBuilder()
        .setCouchdbClient(this.couchdbClient).setRuntimeChangedResources(RuntimeChangedResources)
        .setHeartbeat(sourceConfig.feedHeartbeat().toMillis())
        .setTimeout(sourceConfig.feedTimeout().toMillis())
        .setMaxBufferSize(sourceConfig.feedReaderBufferSize())
        .setMaxBufferTimeInterval(sourceConfig.feedReaderBufferTimerInterval().toMillis()).build();

    this.couchdbChangesFeedReader.startReadingChangesFeed(this.since);

  }

  /**
   * @return the couchdbChangesFeedReader
   */
  public CouchdbChangesFeedReader getCouchdbChangesFeedReader() {
    return couchdbChangesFeedReader;
  }

}
