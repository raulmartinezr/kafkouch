package com.raulmartinezr.kafkouch.connectors.config.source;

import java.util.List;

import com.raulmartinezr.kafkouch.connectors.config.annotation.Default;

public interface CouchdbSourceTaskConfig extends CouchdbSourceConfig {

  /**
   * The task ID... probably. Kafka 2.3.0 and later expose the task ID in the logging context, but
   * for earlier versions we have to assume the task IDs are assigned in the same order as the
   * configs returned by Connector.taskConfigs(int).
   */
  String maybeTaskId();

  /**
   * Task level database stream settings.
   * <p>
   */
  @Default
  List<String> taskCollectionsInDatabases();

}
