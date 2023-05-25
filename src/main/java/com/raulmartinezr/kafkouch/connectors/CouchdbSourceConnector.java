package com.raulmartinezr.kafkouch.connectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raulmartinezr.kafkouch.connectors.config.ConfigHelper;
import com.raulmartinezr.kafkouch.connectors.config.source.CouchdbSourceConfig;
import com.raulmartinezr.kafkouch.connectors.config.source.CouchdbSourceTaskConfig;
import com.raulmartinezr.kafkouch.couchdb.feed.ContinuousFeedEntry;
import com.raulmartinezr.kafkouch.tasks.CouchdbSourceTask;
import com.raulmartinezr.kafkouch.util.CollectionFilterMap;
import com.raulmartinezr.kafkouch.util.CollectionTopicMap;
import com.raulmartinezr.kafkouch.util.DatabaseCollectionsMap;
import com.raulmartinezr.kafkouch.util.ThreadSafeSetHandler;
import com.raulmartinezr.kafkouch.util.Version;

import static com.raulmartinezr.kafkouch.connectors.config.ConfigHelper.keyName;

public class CouchdbSourceConnector extends SourceConnector {

  private static final Logger log = LoggerFactory.getLogger(CouchdbSourceConnector.class);
  private CouchdbSourceConfig config;

  BlockingQueue<ContinuousFeedEntry> changesQueue = null;
  ThreadSafeSetHandler<String> changedDatabases = null;

  FeedMonitorThread feedMonitorThread = null;
  private Map<String, String> configProperties;

  @Override
  public String version() {
    return Version.getVersion();
  }

  @Override
  public ConfigDef config() {
    return ConfigHelper.define(CouchdbSourceConfig.class);
  }

  @Override
  public void start(Map<String, String> properties) {
    /*
     * Starts connector
     */
    this.configProperties = properties;
    this.config = ConfigHelper.parse(CouchdbSourceConfig.class, this.configProperties);
    this.setLogLevel(config.logLevel().toString());

    this.changesQueue = new LinkedBlockingQueue<ContinuousFeedEntry>();
    this.changedDatabases = new ThreadSafeSetHandler<String>();
    this.feedMonitorThread =
        new FeedMonitorThread(this.changesQueue, this.changedDatabases, context, config);
    this.feedMonitorThread.start();

  }

  @Override
  public void stop() {
    this.feedMonitorThread.shutdown();
    // Wait for the thread to complete (optional)
    try {
      this.feedMonitorThread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Class<? extends Task> taskClass() {
    return CouchdbSourceTask.class;
  }

  @Override
  public List<Map<String, String>> taskConfigs(int maxTasks) {
    /*
     * Each entry is the configuration for a single task. Then we can control here the numner of
     * tasks and the configuration for each one of them.
     */
    ObjectMapper objectMapper = new ObjectMapper();

    try {
      String json_collections = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(CollectionFilterMap.parseCollectionToFilter(config.collections()));
      System.out.println("Collections->Filters:");
      System.out.println(json_collections);
      String json_collection_topic =
          objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
              CollectionTopicMap.parseCollectionToTopic(config.collectionToTopic()));
      System.out.println("Collections->Topics:");
      System.out.println(json_collection_topic);
      String json_collection_in_dbs =
          objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
              DatabaseCollectionsMap.parseDatabaseToCollections(config.collectionsInDatabases()));
      System.out.println("DBs->Collections:");
      System.out.println(json_collection_in_dbs);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    String taskIdKey = keyName(CouchdbSourceTaskConfig.class, CouchdbSourceTaskConfig::maybeTaskId);

    List<Map<String, String>> taskConfigs = new ArrayList<>();
    return taskConfigs;
  }

  private void setLogLevel(String logLevel) {
    if (logLevel == null || !logLevel.matches("(TRACE|DEBUG|INFO|WARN|ERROR)")) {
      log.warn("Invalid log level: " + logLevel);
    } else {
      // Set log level in MDC context
      MDC.put("logLevel", logLevel);
    }

  }

}
