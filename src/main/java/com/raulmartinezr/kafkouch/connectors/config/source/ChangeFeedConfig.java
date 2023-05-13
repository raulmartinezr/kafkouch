package com.raulmartinezr.kafkouch.connectors.config.source;

import java.time.Duration;

import com.raulmartinezr.kafkouch.connectors.config.annotation.Default;

public interface ChangeFeedConfig {

  /**
   * since offset
   */
  @Default("now")
  String since();

  /**
   * Number of milliseconds until CouchDB closes the connection. Timeout of feed requests.
   */
  @Default("20s")
  Duration feedTimeout();

  /**
   * Period after which an empty line is sent in the results. Only applicable for longpoll,
   * continuous, and eventsource feeds. Overrides any timeout to keep the feed alive indefinitely
   */
  @Default("10s")
  Duration feedHeartbeat();

  /**
   * Size of buffer for changes feed
   */
  @Default("100")
  int feedReaderBufferSize();

  /**
   * Max time interval bewteen changes feed flushes changes to Task monitor in order to reorganize
   * tasks
   *
   */
  @Default("1s")
  Duration feedReaderBufferTimerInterval();

}
