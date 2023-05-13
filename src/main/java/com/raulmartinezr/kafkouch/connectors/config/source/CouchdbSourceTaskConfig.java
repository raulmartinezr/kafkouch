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
   * Task level database stream settings. If you wish to stream from all collections within a
   * database, specify the database name here.
   * <p>
   * If you specify neither "couchdb.databases" nor "couchdb.collections", the connector will stream
   * from all collections of all scopes in the server.
   */
  @Default
  List<String> taskDatabases();

  /**
   * Task level database collection settings. If you wish to stream from specific collections,
   * specify the qualified collection names here, separated by commas. A qualified name is the name
   * of the scope followed by a dot (.) and then the name of the collection. For example:
   * "tenant-foo.accesses".
   * <p>
   * If you specify neither "couchdb.databases" nor "couchdb.collections", the connector will stream
   * from all collections of all scopes in the bucket.
   * <p>
   */
  @Default
  List<String> taskCollections();
}
