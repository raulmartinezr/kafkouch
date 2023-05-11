package com.raulmartinezr.kafkouch.connectors.config.common;

import org.apache.kafka.common.config.types.Password;

import com.raulmartinezr.kafkouch.connectors.config.annotation.Default;
import com.raulmartinezr.kafkouch.connectors.config.annotation.EnvironmentVariable;
import com.raulmartinezr.kafkouch.connectors.config.annotation.Importance;
import com.raulmartinezr.kafkouch.connectors.config.annotation.Width;

import java.time.Duration;
import java.util.List;

import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Width.LONG;

public interface ConnectionConfig {
    /**
     * Addresses of Couchbase Server nodes, delimited by commas.
     * <p>
     * If a custom port is specified, it must be the KV port
     * (which is normally 11210 for insecure connections,
     * or 11207 for secure connections).
     */
    @Width(LONG)
    @Importance(HIGH)
    List<String> seedNodes();

    /**
     * Name of the Couchbase user to authenticate as.
     */
    @Importance(HIGH)
    String username();

    /**
     * Password of the Couchbase user.
     */
    @Importance(HIGH)
    @EnvironmentVariable("KAFKA_COUCHBASE_PASSWORD")
    Password password();

    /**
     * Name of the Couchbase bucket to use.
     * <p>
     * This property is required unless using the experimental AnalyticsSinkHandler.
     */
    @Default
    @Width(LONG)
    @Importance(HIGH)
    String bucket();

    /**
     * The network selection strategy for connecting to a Couchbase Server cluster
     * that advertises alternate addresses.
     * <p>
     * A Couchbase node running inside a container environment (like Docker or
     * Kubernetes)
     * might be configured to advertise both its address within the container
     * environment
     * (known as its "default" address) as well as an "external" address for use by
     * clients
     * connecting from outside the environment.
     * <p>
     * Setting the 'couchbase.network' config property to 'default' or 'external'
     * forces
     * the selection of the respective addresses. Setting the value to 'auto' tells
     * the
     * connector to select whichever network contains the addresses specified in the
     * 'couchbase.seed.nodes' config property.
     */
    @Default("auto")
    String network();

    /**
     * On startup, the connector will wait this long for a Couchbase connection to
     * be established.
     * If a connection is not established before the timeout expires, the connector
     * will terminate.
     */
    @Default("30s")
    Duration bootstrapTimeout();
}