package com.raulmartinezr.kafkouch.connectors.config.source;

import com.raulmartinezr.kafkouch.connectors.config.annotation.Default;

public interface SourcePersistanceConfig {

  @Default("internal__kafka_connect")
  String persistanceDatabase();

}
