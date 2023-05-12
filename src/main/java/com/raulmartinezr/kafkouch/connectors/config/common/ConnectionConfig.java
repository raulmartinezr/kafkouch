package com.raulmartinezr.kafkouch.connectors.config.common;

import org.apache.kafka.common.config.types.Password;

import com.raulmartinezr.kafkouch.connectors.config.annotation.Default;
import com.raulmartinezr.kafkouch.connectors.config.annotation.EnvironmentVariable;
import com.raulmartinezr.kafkouch.connectors.config.annotation.Importance;
import com.raulmartinezr.kafkouch.connectors.config.annotation.Width;
import com.raulmartinezr.kafkouch.couchdb.CouchdbClient.CouchdbAuthMethod;

import java.time.Duration;

import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Width.LONG;

public interface ConnectionConfig {

  /**
   * Address of Couchdb server URL.
   * <p>
   * If a custom port is specified, it must be the KV port (which is normally 11210 for insecure
   * connections, or 11207 for secure connections).
   */
  @Width(LONG)
  @Importance(HIGH)
  String url();

  /**
   * Name of the Couchdb user to authenticate as.
   */
  @Importance(HIGH)
  String username();

  /**
   * Password of the Couchdb user.
   */
  @Importance(HIGH)
  @EnvironmentVariable("KAFKA_COUCHDB_PASSWORD")
  Password password();

  /**
   * Auth method for couchdb connection.
   */
  @Importance(HIGH)
  @Default("COOKIE")
  CouchdbAuthMethod authMethod();

  /**
   * On startup, the connector will wait this long for a couchdeb connection to be established. If a
   * connection is not established before the timeout expires, the connector will terminate.
   */
  @Default("30s")
  Duration bootstrapTimeout();
}
