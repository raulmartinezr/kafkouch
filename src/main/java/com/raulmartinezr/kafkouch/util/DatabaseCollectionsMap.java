package com.raulmartinezr.kafkouch.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

//database-a=collection-a|collection-b,database-b-=collection-a
public class DatabaseCollectionsMap {
  private DatabaseCollectionsMap() {
    throw new AssertionError("not instantiable");
  }

  public static Map<String, EnabledCollectionsType> parseDatabaseToCollections(List<String> toCollections ) {
    return mapValues(parseCommon(toCollections), EnabledCollectionsType::parse);
  }

  public static Map<EnabledCollectionsType, String> parseCollectionsToDatabase(List<String> collectionsTo) {
    return mapKeys(parseCommon(collectionsTo), EnabledCollectionsType::parse);
  }

  private static Map<String, String> parseCommon(List<String> map) {
    Map<String, String> result = new HashMap<>();
    for (String entry : map) {
      String[] components = entry.split("=", -1);
      if (components.length != 2) {
        throw new IllegalArgumentException("Bad entry: '" + entry
            + "'. Expected exactly one equals (=) character separating database and it's enabled collections.");
      }
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
