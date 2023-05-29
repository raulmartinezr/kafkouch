package com.raulmartinezr.kafkouch.connectors.config.common;

import com.raulmartinezr.kafkouch.connectors.config.annotation.Importance;
import com.raulmartinezr.kafkouch.connectors.config.annotation.Width;

import static org.apache.kafka.common.config.ConfigDef.Importance.HIGH;
import static org.apache.kafka.common.config.ConfigDef.Width.MEDIUM;

public interface MiscallaneusConfig {

  /**
   * Connector name.
   *
   */
  @Width(MEDIUM)
  @Importance(HIGH)
  String connectorName();

}
