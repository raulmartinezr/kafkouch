package com.raulmartinezr.kafkouch.util;

import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;

public class EnabledCollectionsType {
  private final List<String> enabledCollections;

  public static EnabledCollectionsType parse(String enabledCollectionsType) {
    String[] split = enabledCollectionsType.split("|", -1);
    List<String> splitList =
        Arrays.stream(split).filter(str -> !str.isEmpty()).collect(Collectors.toList());

    if (splitList.size() == 0) {
      throw new IllegalArgumentException(
          "Bad entry: '" + enabledCollectionsType + "'. Expected at least one enabled collection.");
    }
    return new EnabledCollectionsType(splitList);
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
