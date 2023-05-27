package com.raulmartinezr.kafkouch.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.raulmartinezr.kafkouch.util.ConnectHelper.getTaskIdFromLoggingContext;

public class ConnectorLifecycle {

  public enum Milestone {
    CONNECTOR_STARTED, CONNECTOR_STOPPED, COLLECTIONS_UNASSIGNED,
    /**
     * The connector has created a configuration for each task, and specified which Couchbase
     * partitions each task should stream from.
     */
    COLLECTIONS_ASSIGNED,
  }

  enum LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR
  }

  private static final Logger log = LoggerFactory.getLogger(ConnectorLifecycle.class);
  private final LogLevel logLevel = LogLevel.INFO;
  private final String uuid = UUID.randomUUID().toString();

  public void logConnectorStarted(String name) {
    Map<String, Object> details = new LinkedHashMap<>();
    details.put("connectorVersion", Version.getVersion());
    details.put("connectorName", name);
    logMilestone(Milestone.CONNECTOR_STARTED, details);
  }

  public void logCollectionsAssigned(List<List<EnabledCollectionsSet>> groupedDatabaseCollections) {
    Map<String, Object> details = new LinkedHashMap<>();
    for (int i = 0; i < groupedDatabaseCollections.size(); i++) {
      details.put("task" + i, groupedDatabaseCollections.get(i));
    }
    logMilestone(Milestone.COLLECTIONS_ASSIGNED, details);
  }

  public void logCollectionsUnAssigned(Map<String, CollectionFilterType> parseCollectionToFilter,
      Map<String, String> parseCollectionToTopic,
      Map<String, EnabledCollectionsType> parseDatabaseToCollections) {

    Map<String, Object> detailsCollectionToFilter = new LinkedHashMap<>();
    for (String collectionToFilterKey : parseCollectionToFilter.keySet()) {
      detailsCollectionToFilter.put("[collection->filter] " + collectionToFilterKey,
          parseCollectionToFilter.get(collectionToFilterKey));
    }
    logMilestone(Milestone.COLLECTIONS_UNASSIGNED, detailsCollectionToFilter);

    Map<String, Object> detailsCollectionToTopic = new LinkedHashMap<>();
    for (String parseCollectionToTopicKey : parseCollectionToTopic.keySet()) {
      detailsCollectionToTopic.put("[collection->topic] " + parseCollectionToTopicKey,
          parseCollectionToTopic.get(parseCollectionToTopicKey));
    }
    logMilestone(Milestone.COLLECTIONS_UNASSIGNED, detailsCollectionToFilter);

    Map<String, Object> detailsDatabaseToCollections = new LinkedHashMap<>();
    for (String parseDatabaseToCollectionsKey : parseDatabaseToCollections.keySet()) {
      detailsDatabaseToCollections.put("[database->collections] " + parseDatabaseToCollectionsKey,
          parseDatabaseToCollections.get(parseDatabaseToCollectionsKey));
    }
    logMilestone(Milestone.COLLECTIONS_UNASSIGNED, detailsDatabaseToCollections);
  }

  public void logConnectorStopped() {
    logMilestone(Milestone.CONNECTOR_STOPPED, Collections.emptyMap());
  }

  private void logMilestone(ConnectorLifecycle.Milestone milestone,
      Map<String, Object> milestoneDetails) {
    if (enabled()) {
      LinkedHashMap<String, Object> message = new LinkedHashMap<>();
      message.put("milestone", milestone);
      message.put("connectorUuid", uuid);
      getTaskIdFromLoggingContext().ifPresent(id -> message.put("taskId", id));
      message.putAll(milestoneDetails);
      doLog(message);
    }
  }

  private void doLog(Object message) {
    try {
      logMessage(logLevel, JsonMapper.encodeAsString(message));
    } catch (Exception e) {
      logMessage(logLevel, message.toString());
    }
  }

  public static void logMessage(LogLevel logLevel, String message) {
    switch (logLevel) {
      case TRACE:
        log.trace(message);
        break;
      case DEBUG:
        log.debug(message);
        break;
      case INFO:
        log.info(message);
        break;
      case WARN:
        log.warn(message);
        break;
      case ERROR:
        log.error(message);
        break;
    }
  }

  private boolean enabled() {
    return true;
  }

}
