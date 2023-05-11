package com.raulmartinezr.kafkouch.connectors.config;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class DurationValidator implements ConfigDef.Validator {
    @Override
    public void ensureValid(String name, Object value) {
        try {
            if (value != null && !((String) value).isEmpty()) {
                DurationParser.parseDuration((String) value, MILLISECONDS);
            }
        } catch (IllegalArgumentException e) {
            throw new ConfigException("Failed to parse config property '" + name + "' -- " + e.getMessage());
        }
    }

    public String toString() {
        return "An integer followed by a time unit (ms = milliseconds, s = seconds, m = minutes, h = hours, d = days)."
                +
                " For example, to specify 30 minutes: 30m";
    }
}