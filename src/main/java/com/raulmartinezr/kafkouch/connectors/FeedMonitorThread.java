package com.raulmartinezr.kafkouch.connectors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.connect.connector.ConnectorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raulmartinezr.kafkouch.connectors.config.source.CouchdbSourceConfig;
import com.raulmartinezr.kafkouch.couchdb.CouchdbChangesFeedReader;
import com.raulmartinezr.kafkouch.couchdb.CouchdbChangesFeedReader.CouchdbChangesFeedReaderBuilder;
import com.raulmartinezr.kafkouch.couchdb.feed.ContinuousFeedEntry;
import com.raulmartinezr.kafkouch.util.ThreadSafeSetHandler;

class FeedMonitorThread extends Thread {

  private static final Logger log = LoggerFactory.getLogger(FeedMonitorThread.class);

  private BlockingQueue<ContinuousFeedEntry> changesQueue = null;
  private ThreadSafeSetHandler<String> changedDatabases = null;
  private CountDownLatch shutdownLatch;
  private ConnectorContext context;
  private CouchdbChangesFeedReader couchdbChangesFeedReader = null;
  private CouchdbSourceConfig sourceConfig;

  /**
   *
   */
  public FeedMonitorThread(BlockingQueue<ContinuousFeedEntry> changesQueue,
      ThreadSafeSetHandler<String> changedDatabases, ConnectorContext context,
      CouchdbSourceConfig sourceConfig) {
    this.changesQueue = changesQueue;
    this.changedDatabases = changedDatabases;
    this.context = context;
    this.sourceConfig = sourceConfig;
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
    return false;
  }

  public void shutdown() {
    log.info("Shutting down thread monitoring global changes feed.");
    this.couchdbChangesFeedReader.stopReadingChangesFeed();
    shutdownLatch.countDown();
  }

  private void startReadingFeed() {
    this.couchdbChangesFeedReader = new CouchdbChangesFeedReaderBuilder().setUrl(sourceConfig.url())
        .setUsername(sourceConfig.username()).setPassword(sourceConfig.password().value())
        .setAuthMethod(sourceConfig.authMethod()).setConnect(true).setSince(sourceConfig.since())
        .setChangesQueue(changesQueue).setChangedDatabases(changedDatabases)
        .setHeartbeat(sourceConfig.feedHeartbeat().toMillis())
        .setTimeout(sourceConfig.feedTimeout().toMillis())
        .setMaxBufferSize(sourceConfig.feedReaderBufferSize())
        .setMaxBufferTimeInterval(sourceConfig.feedReaderBufferTimerInterval().toMillis())
        .setReadTimeout(sourceConfig.httpTimeout().toMillis()).build();

    this.couchdbChangesFeedReader.startReadingChangesFeed();

  }

}
