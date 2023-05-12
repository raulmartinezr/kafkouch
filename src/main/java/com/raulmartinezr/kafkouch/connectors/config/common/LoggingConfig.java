package com.raulmartinezr.kafkouch.connectors.config.common;

import com.raulmartinezr.kafkouch.connectors.config.annotation.Default;

public interface LoggingConfig {

  public enum LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR, FATAL, OFF
  }

  /**
   * Determines log level to be used by connector logging framework.
   */
  @Default("INFO")
  LogLevel logLevel();

}
