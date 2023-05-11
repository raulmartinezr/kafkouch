package com.raulmartinezr.kafkouch.connectors.config.source;

import org.apache.kafka.common.config.ConfigDef;

import com.raulmartinezr.kafkouch.connectors.config.annotation.Default;
import com.raulmartinezr.kafkouch.connectors.config.annotation.Stability;
import com.raulmartinezr.kafkouch.filters.Filter;

import java.util.List;

public interface SourceBehaviorConfig {
    /**
     * Name of the default Kafka topic to publish data to, for collections
     * that don't have an entry in the `couchbase.collection.to.topic` map.
     * <p>
     * This is a format string that recognizes the following placeholders:
     * <p>
     * ${bucket} refers to the bucket containing the document.
     * <p>
     * ${scope} refers to the scope containing the document.
     * <p>
     * ${collection} refers to the collection containing the document.
     */
    @Default("${bucket}.${scope}.${collection}")
    String topic();

    /**
     * A map from Couchbase collection to Kafka topic.
     * <p>
     * Collection and Topic are joined by an equals sign.
     * Map entries are delimited by commas.
     * <p>
     * For example, if you want to write messages from collection
     * "scope-a.invoices" to topic "topic1", and messages from collection
     * "scope-a.widgets" to topic "topic2", you would write:
     * "scope-a.invoices=topic1,scope-a.widgets=topic2".
     * <p>
     * Defaults to an empty map. For collections not present in this map,
     * the destination topic is determined by the `couchbase.topic` config property.
     *
     * @since 4.1.8
     */
    @Default
    List<String> collectionToTopic();

    @SuppressWarnings("unused")
    static ConfigDef.Validator collectionToTopicValidator() {
        return validate(TopicMap::parseCollectionToTopic, "scope.collection=topic,...");
    }

    /**
     * The fully-qualified class name of the source handler to use.
     * The source handler determines how the Couchbase document is converted into a
     * Kafka record.
     * <p>
     * To publish JSON messages identical to the Couchbase documents, use
     * `com.couchbase.connect.kafka.handler.source.RawJsonSourceHandler`
     * and set `value.converter` to
     * `org.apache.kafka.connect.converters.ByteArrayConverter`.
     * <p>
     * When using a custom source handler that filters out certain messages,
     * consider also configuring `couchbase.black.hole.topic`.
     * See that property's documentation for details.
     */
    Class<? extends SourceHandler> sourceHandler();

    /**
     * The class name of the event filter to use.
     * The event filter determines whether a database change event is ignored.
     * <p>
     * When using a non-default filter,
     * consider also configuring `couchbase.black.hole.topic`.
     * See that property's documentation for details.
     */
    @Default("com.raul.martinezr.kafkouch.filters.AllPassFilter")
    Class<? extends Filter> eventFilter();

    /**
     * If this property is non-blank, the connector publishes a tiny synthetic
     * record
     * to this topic whenever the Filter or SourceHandler ignores a source event.
     * <p>
     * This lets the connector tell the Kafka Connect framework about the
     * source offset of the ignored event. Otherwise, a long sequence of ignored
     * events in a low-traffic deployment might cause the stored source offset to
     * lag
     * too far behind the current source offset, which can lead to rollbacks to zero
     * when the connector is restarted.
     * <p>
     * After a record is published to this topic, the record is no longer important,
     * and should be deleted as soon as possible. To reduce disk usage, configure
     * this topic to use small segments and the lowest possible retention settings.
     *
     * @since 4.1.8
     */
    @Stability.Uncommitted
    @Default
    String blackHoleTopic();

    /**
     * Controls maximum size of the batch for writing into topic.
     */
    @Default("2000")
    int batchSizeMax();

    /**
     * Controls when in history then connector starts streaming from.
     */
    @Default("SAVED_OFFSET_OR_BEGINNING")
    StreamFrom streamFrom();

    /**
     * If you wish to stream from all collections within a scope, specify the scope
     * name here.
     * <p>
     * If you specify neither "couchbase.scope" nor "couchbase.collections",
     * the connector will stream from all collections of all scopes in the bucket.
     * <p>
     * Requires Couchbase Server 7.0 or later.
     */
    @Default
    String scope();

    /**
     * If you wish to stream from specific collections, specify the qualified
     * collection
     * names here, separated by commas. A qualified name is the name of the scope
     * followed by a dot (.) and then the name of the collection. For example:
     * "tenant-foo.invoices".
     * <p>
     * If you specify neither "couchbase.scope" nor "couchbase.collections",
     * the connector will stream from all collections of all scopes in the bucket.
     * <p>
     * Requires Couchbase Server 7.0 or later.
     */
    @Default
    List<String> collections();
}