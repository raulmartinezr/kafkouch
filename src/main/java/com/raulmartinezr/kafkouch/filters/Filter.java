package com.raulmartinezr.kafkouch.filters;

import java.util.Map;
import com.raulmartinezr.kafkouch.couchdb.feed.ContinuousFeedEntry;

/**
 * Determines which changes get published to Kafka.
 */
public interface Filter {
  /**
   * Called when the filter is instantiated.
   *
   * @param configProperties the connector configuration.
   */
  default void init(Map<String, String> configProperties) {}

  /**
   * Returns true if the event should be published to Kafka, otherwise false.
   */
  boolean pass(ContinuousFeedEntry event);
}
