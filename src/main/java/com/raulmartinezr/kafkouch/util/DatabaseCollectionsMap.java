package com.raulmartinezr.kafkouch.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;

// database-a=collection-a|collection-b,database-b-=collection-a
public class DatabaseCollectionsMap {
  private DatabaseCollectionsMap() {
    throw new AssertionError("not instantiable");
  }

  public static Map<String, EnabledCollectionsType> parseDatabaseToCollections(
      List<String> toCollections) {
    return mapValues(parseCommon(toCollections), EnabledCollectionsType::parse);
  }

  public static Map<EnabledCollectionsType, String> parseCollectionsToDatabase(
      List<String> collectionsTo) {
    return mapKeys(parseCommon(collectionsTo), EnabledCollectionsType::parse);
  }

  public static String formatDatabaseToCollections(
      Map<String, EnabledCollectionsType> toCollections) {
    List<String> formatDatabaseToCollections = new ArrayList<String>();
    for (String toCollectionKey : toCollections.keySet()) {
      String toCollectionValue =
          String.join("|", toCollections.get(toCollectionKey).getEnabledCollections());
      formatDatabaseToCollections.add(toCollectionKey + "=" + toCollectionValue);
    }
    return String.join(",", formatDatabaseToCollections);
  }

  public static String formatDatabaseToCollections(List<EnabledCollectionsSet> toCollections) {
    List<String> formatDatabaseToCollections = new ArrayList<String>();
    for (EnabledCollectionsSet toCollection : toCollections) {
      String toCollectionValue =
          String.join("|", toCollection.getEnabledCollectionsType().getEnabledCollections());
      formatDatabaseToCollections.add(toCollection.getDatabase() + "=" + toCollectionValue);
    }
    return String.join(",", formatDatabaseToCollections);
  }

  private static Map<String, String> parseCommon(List<String> map) {
    Map<String, String> result = new HashMap<>();
    for (String entry : map) {
      String[] components = entry.split("=", -1);
      if (components.length != 2) {
        throw new IllegalArgumentException("Bad entry: '" + entry
            + "'. Expected exactly one equals (=) character separating database and it's enabled collections.");
      }
      result.put(components[0], components[1]);
    }

    return result;
  }

  private static <K, V1, V2> Map<K, V2> mapValues(Map<K, V1> map,
      Function<? super V1, ? extends V2> valueTransformer) {
    return map.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, entry -> valueTransformer.apply(entry.getValue())));
  }

  private static <K1, K2, V> Map<K2, V> mapKeys(Map<K1, V> map,
      Function<? super K1, ? extends K2> keyTransformer) {
    return map.entrySet().stream()
        .collect(toMap(entry -> keyTransformer.apply(entry.getKey()), Map.Entry::getValue));
  }
}
