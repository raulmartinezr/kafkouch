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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raulmartinezr.kafkouch.connectors.config.ConfigHelper;
import com.raulmartinezr.kafkouch.connectors.config.source.CouchdbSourceConfig;
import com.raulmartinezr.kafkouch.connectors.config.source.CouchdbSourceTaskConfig;
import com.raulmartinezr.kafkouch.couchdb.CouchdbClient;
import com.raulmartinezr.kafkouch.couchdb.CouchdbClientBuilder;
import com.raulmartinezr.kafkouch.couchdb.client.pojo.Document;
import com.raulmartinezr.kafkouch.tasks.CouchdbSourceTask;
import com.raulmartinezr.kafkouch.util.CollectionFilterMap;
import com.raulmartinezr.kafkouch.util.CollectionFilterType;
import com.raulmartinezr.kafkouch.util.CollectionTopicMap;
import com.raulmartinezr.kafkouch.util.ConnectorLifecycle;
import com.raulmartinezr.kafkouch.util.DatabaseCollectionsMap;
import com.raulmartinezr.kafkouch.util.EnabledCollectionsSet;
import com.raulmartinezr.kafkouch.util.EnabledCollectionsType;
import com.raulmartinezr.kafkouch.util.Version;

import static org.apache.kafka.connect.util.ConnectorUtils.groupPartitions;
import static com.raulmartinezr.kafkouch.connectors.config.ConfigHelper.keyName;

public class CouchdbSourceConnector extends SourceConnector {

  private static final Logger log = LoggerFactory.getLogger(CouchdbSourceConnector.class);
  private CouchdbSourceConfig config;
  private CollectionsConfiguration collectionsConfiguration;

  RuntimeChangedResources runtimeChangedResources = null;

  FeedMonitorThread feedMonitorThread = null;
  private Map<String, String> configProperties;

  private final ConnectorLifecycle lifecycle = new ConnectorLifecycle();
  private CouchdbClient couchdbClient;
  private List<EnabledCollectionsSet> expandedDatabaseToCollections;

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

    this.couchdbClient =
        new CouchdbClientBuilder().setUrl(this.config.url()).setUsername(this.config.username())
            .setPassword(this.config.password().value()).setAuthMethod(this.config.authMethod())
            .setConnect(true).setReadTimeout(this.config.httpTimeout().toMillis()).build();

    this.collectionsConfiguration = new CollectionsConfiguration(
        DatabaseCollectionsMap.parseDatabaseToCollections(config.collectionsInDatabases()),
        CollectionTopicMap.parseCollectionToTopic(config.collectionToTopic()),
        CollectionFilterMap.parseCollectionToFilter(config.collections()));
    // Expand first time. If "__all__" not in databases, the same value will be used
    // always
    this.expandedDatabaseToCollections =
        this.expandDatabaseToCollections(this.collectionsConfiguration.getDatabaseToCollections(),
            this.collectionsConfiguration.getCollectionToFilter());
    this.runtimeChangedResources = new RuntimeChangedResources();
    /*
     * Probably we would only need changed databases. The offset obtained in task will contain last
     * seq read from database Using that as starting point would be enough. From changes queue maybe
     * we could get the last seq read from database and use it as starting point in next glonal
     * changes request
     */

    String since = this.getSinceOffset();
    this.feedMonitorThread = new FeedMonitorThread(this.runtimeChangedResources, context, config,
        since, this.couchdbClient);
    this.feedMonitorThread.start();

  }

  private String getSinceOffset() {
    boolean databaseCreated = this.ensurePersistanceDatabase();
    boolean documentCreated = this.ensurePersistanceDocument();
    if (databaseCreated || documentCreated) {
      return this.config.persistanceDatabase(); // When there was nothing, use configured value
    } else {
      String dbSince = this.getSinceFromDatabase();
      return dbSince != null ? dbSince : this.config.since();
    }
  }

  private String getSinceFromDatabase() {
    Document connectorStorage = this.couchdbClient.documentGet(this.config.persistanceDatabase(),
        this.config.connectorName());
    return connectorStorage.getProperties() != null
        ? (String) connectorStorage.getProperties().get("since")
        : null;
  }

  private boolean ensurePersistanceDocument() {
    String databaseName = this.config.persistanceDatabase();
    String documentId = this.config.connectorName();
    boolean exists = this.couchdbClient.documentHead(databaseName, documentId);
    if (!exists) {
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode emptyJson = objectMapper.createObjectNode();
      this.couchdbClient.documentPut(databaseName, documentId, emptyJson.toString());
    }
    return !exists;
  }

  private boolean ensurePersistanceDatabase() {
    String databaseName = this.config.persistanceDatabase();
    boolean exists = this.couchdbClient.databaseHead(databaseName);
    if (!exists) {
      this.couchdbClient.databasePut(databaseName);
    }
    return !exists;
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

    /*
     * Config would be always the same except if we have "__all__" keyword. In this case, if a
     * database is created while connector is runnung, then it should be included
     *
     */
    if (this.collectionsConfiguration.getDatabaseToCollections().containsKey("__all__")) {
      this.expandedDatabaseToCollections =
          this.expandDatabaseToCollections(this.collectionsConfiguration.getDatabaseToCollections(),
              this.collectionsConfiguration.getCollectionToFilter());
    }

    // Here the filter to use only changed databases. Using
    // this.runtimeChangedResources
    // Get current values and reset so it can be still used by feed thread.

    Set<String> databasesChangedSinceLastCheck;
    synchronized (this.runtimeChangedResources) {
      ChangedResources currentChanges = this.runtimeChangedResources.getAndResetChanges();
      databasesChangedSinceLastCheck = currentChanges.getChangedDatabases();
      this.persistLastSeq(currentChanges.getLastSeq());
    }

    List<EnabledCollectionsSet> expandedListCopy =
        new ArrayList<EnabledCollectionsSet>(this.expandedDatabaseToCollections);

    List<EnabledCollectionsSet> filteredExpandedListCopy =
        expandedListCopy.stream().filter(enabledCollectionSet -> databasesChangedSinceLastCheck
            .contains(enabledCollectionSet.getDatabase())).collect(Collectors.toList());;

    List<List<EnabledCollectionsSet>> groupedDatabaseCollections =
        groupPartitions(filteredExpandedListCopy, maxTasks);

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

  private void persistLastSeq(String lastSeq) {}

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
      Set<String> allDatabases = this.getAllDatabases();
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
    // Expand collections if needed
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

  private Set<String> getAllDatabases() {
    List<String> systemPrefixes = List.of("_", "internal__");
    Set<String> allDatabases = this.couchdbClient.serverAllDatabasesGet();
    return allDatabases.stream().filter(str -> !startsWithAny(str, systemPrefixes))
        .collect(Collectors.toSet());
  }

  private boolean startsWithAny(String text, List<String> wordList) {
    boolean startsWithWord = false;
    for (String word : wordList) {
      if (text.startsWith(word)) {
        startsWithWord = true;
        break;
      }
    }
    return startsWithWord;
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
