package com.raulmartinezr.kafkouch.connectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raulmartinezr.kafkouch.connectors.config.ConfigHelper;
import com.raulmartinezr.kafkouch.connectors.config.source.CouchdbSourceConfig;
import com.raulmartinezr.kafkouch.connectors.config.source.CouchdbSourceTaskConfig;
import com.raulmartinezr.kafkouch.couchdb.feed.ContinuousFeedEntry;
import com.raulmartinezr.kafkouch.tasks.CouchdbSourceTask;
import com.raulmartinezr.kafkouch.util.CollectionFilterMap;
import com.raulmartinezr.kafkouch.util.CollectionFilterType;
import com.raulmartinezr.kafkouch.util.CollectionTopicMap;
import com.raulmartinezr.kafkouch.util.ConnectorLifecycle;
import com.raulmartinezr.kafkouch.util.DatabaseCollectionsMap;
import com.raulmartinezr.kafkouch.util.EnabledCollectionsSet;
import com.raulmartinezr.kafkouch.util.EnabledCollectionsType;
import com.raulmartinezr.kafkouch.util.ThreadSafeSetHandler;
import com.raulmartinezr.kafkouch.util.Version;

import static org.apache.kafka.connect.util.ConnectorUtils.groupPartitions;
import static com.raulmartinezr.kafkouch.connectors.config.ConfigHelper.keyName;

public class CouchdbSourceConnector extends SourceConnector {

  private static final Logger log = LoggerFactory.getLogger(CouchdbSourceConnector.class);
  private CouchdbSourceConfig config;

  BlockingQueue<ContinuousFeedEntry> changesQueue = null;
  ThreadSafeSetHandler<String> changedDatabases = null;

  FeedMonitorThread feedMonitorThread = null;
  private Map<String, String> configProperties;

  private final ConnectorLifecycle lifecycle = new ConnectorLifecycle();

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

  /*
   * Each entry is the configuration for a single task. Then we can control here the numner of tasks
   * and the configuration for each one of them.
   */
  @Override
  public List<Map<String, String>> taskConfigs(int maxTasks) {

    Map<String, EnabledCollectionsType> databaseToCollections =
        DatabaseCollectionsMap.parseDatabaseToCollections(config.collectionsInDatabases());
    Map<String, String> collectionToTopic =
        CollectionTopicMap.parseCollectionToTopic(config.collectionToTopic());
    Map<String, CollectionFilterType> collectionToFilter =
        CollectionFilterMap.parseCollectionToFilter(config.collections());
    lifecycle.logCollectionsUnAssigned(collectionToFilter, collectionToTopic,
        databaseToCollections);

    List<EnabledCollectionsSet> expandedDatabaseToCollections =
        this.expandDatabaseToCollections(databaseToCollections, collectionToFilter);

    List<List<EnabledCollectionsSet>> groupedDatabaseCollections =
        groupPartitions(expandedDatabaseToCollections, maxTasks);

    lifecycle.logCollectionsAssigned(groupedDatabaseCollections);

    String taskCollectionsInDatabasesKey =
        keyName(CouchdbSourceTaskConfig.class, CouchdbSourceTaskConfig::taskCollectionsInDatabases);
    String taskIdKey = keyName(CouchdbSourceTaskConfig.class, CouchdbSourceTaskConfig::maybeTaskId);

    // String taskIdKey = keyName(CouchdbSourceTaskConfig.class,
    // CouchdbSourceTaskConfig::maybeTaskId);
    int taskId = 0;
    List<Map<String, String>> taskConfigs = new ArrayList<>();
    for (List<EnabledCollectionsSet> taskGroup : groupedDatabaseCollections) {
      Map<String, String> taskProps = new HashMap<>(configProperties); // Initialize task props with
                                                                       // global props
      taskProps.put(taskCollectionsInDatabasesKey,
          DatabaseCollectionsMap.formatDatabaseToCollections(taskGroup));
      taskProps.put(taskIdKey, "maybe-" + taskId++);
      taskConfigs.add(taskProps);
    }

    return taskConfigs;
  }

  /**
   * Expands the database to collections map to include all databases if keyword "__all__" is was
   * included and all collections if keyword "*" was included for some database
   *
   * @param databaseToCollections
   * @param collectionToFilter
   * @return
   */
  private List<EnabledCollectionsSet> expandDatabaseToCollections(
      Map<String, EnabledCollectionsType> databaseToCollections,
      Map<String, CollectionFilterType> collectionToFilter) {
    Map<String, EnabledCollectionsType> expandedDatabaseToCollections =
        new HashMap<String, EnabledCollectionsType>();
    boolean expandDatabases = databaseToCollections.containsKey("__all__");

    Set<String> allCollections = collectionToFilter.keySet();
    if (expandDatabases) {
      List<String> allDatabases = this.getAllDatabases();
      EnabledCollectionsType enabledCollectionsForAllDatabases =
          databaseToCollections.get("__all__");
      if (enabledCollectionsForAllDatabases.getEnabledCollections().contains("*")) {
        enabledCollectionsForAllDatabases.getEnabledCollections().addAll(allCollections);
      }
      // Load expanded database collections map
      for (String database : allDatabases) {
        expandedDatabaseToCollections.put(database, enabledCollectionsForAllDatabases);
      }
    }
    // Fill with configured values
    for (String database : databaseToCollections.keySet()) {
      if (!database.equals("__all__")) {
        if (expandDatabases) {
          expandedDatabaseToCollections.get(database).getEnabledCollections()
              .addAll(expandCollections(databaseToCollections.get(database), allCollections)
                  .getEnabledCollections());
        } else {
          expandedDatabaseToCollections.put(database,
              expandCollections(databaseToCollections.get(database), allCollections));
        }
      }
    }

    List<EnabledCollectionsSet> mergedList = expandedDatabaseToCollections.entrySet().stream()
        .map(entry -> new EnabledCollectionsSet(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
    return mergedList;
  }

  private EnabledCollectionsType expandCollections(EnabledCollectionsType enabledCollections,
      Set<String> allCollections) {
    if (enabledCollections.getEnabledCollections().contains("*")) {
      enabledCollections.getEnabledCollections().addAll(allCollections);
    }
    return enabledCollections;
  }

  private List<String> getAllDatabases() {
    return null;
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
