package com.raulmartinezr.kafkouch.connectors.config;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;

public class DataSizeValidator implements ConfigDef.Validator {
  @Override
  public void ensureValid(String name, Object value) {
    try {
      if (value != null && !((String) value).isEmpty()) {
        DataSizeParser.parseDataSize((String) value);
      }
    } catch (IllegalArgumentException e) {
      throw new ConfigException(
          "Failed to parse config property '" + name + "' -- " + e.getMessage());
    }
  }

  public String toString() {
    return "An integer followed by a size unit (b = bytes, k = kilobytes, m = megabytes, g = gigabytes)."
        + " For example, to specify 64 megabytes: 64m";
  }
}
