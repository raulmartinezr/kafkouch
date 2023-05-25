package com.raulmartinezr.kafkouch.util;

import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

import io.burt.jmespath.JmesPath;
import io.burt.jmespath.jackson.JacksonRuntime;
import io.burt.jmespath.parser.ParseException;

import static java.util.Objects.requireNonNull;

public class CollectionFilterType {
  private final String filter;
  static JmesPath<JsonNode> jmespath = new JacksonRuntime();

  public static CollectionFilterType parse(String filter) {
    try {
      jmespath.compile(filter);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Bad entry: '" + filter
          + "'. Expected a valid JMESPath expression. Errors: " + e.getMessage());
    }
    return new CollectionFilterType(filter);
  }

  public CollectionFilterType(String filter) {
    this.filter = requireNonNull(filter);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    CollectionFilterType that = (CollectionFilterType) o;
    return filter.equals(that.filter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(filter);
  }

  /**
   * @return the filter
   */
  public String getFilter() {
    return filter;
  }

}
