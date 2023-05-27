package com.raulmartinezr.kafkouch.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.MDC;

public class ConnectHelper {
  private ConnectHelper() {
    throw new AssertionError("not instantiable");
  }

  private static final Pattern taskIdPattern = Pattern.compile("\\|task-(\\d+)");

  public static Optional<String> getConnectorContextFromLoggingContext() {
    // This should be present in Kafka 2.3.0 and later.
    // See https://issues.apache.org/jira/browse/KAFKA-3816
    return Optional.ofNullable(MDC.get("connector.context")).map(String::trim);
  }

  /**
   * Returns the connector's task ID, or an empty optional if it could not be determined from the
   * logging context.
   */
  public static Optional<String> getTaskIdFromLoggingContext() {
    return getConnectorContextFromLoggingContext().map(context -> {
      Matcher m = taskIdPattern.matcher(context);
      return m.find() ? m.group(1) : null;
    });
  }
}
