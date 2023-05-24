package com.raulmartinezr.kafkouch.util;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnabledCollectionsType {
  private final List<String> enabledCollections;

  public static EnabledCollectionsType parse(String enabledCollectionsType) {
    String[] split = enabledCollectionsType.split("|", -1);
    if (split.length == 0) {
      throw new IllegalArgumentException("Bad entry: '" + enabledCollectionsType
          + "'. Expected at least one enabled collection.");
    }
    return new EnabledCollectionsType(new ArrayList<>(Arrays.asList(split)));
  }

  public EnabledCollectionsType(List<String> enabledCollections) {
    this.enabledCollections = requireNonNull(enabledCollections);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    EnabledCollectionsType that = (EnabledCollectionsType) o;
    return enabledCollections.equals(that.enabledCollections);
  }

  @Override
  public int hashCode() {
    return Objects.hash(enabledCollections);
  }

  /**
   * @return the enabledCollections
   */
  public List<String> getEnabledCollections() {
    return enabledCollections;
  }
}
