package com.raulmartinezr.kafkouch.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

// collection-a=doc_type==`type-a`,collection-b=doc_type==`type-b`
public class CollectionFilterMap {
  private CollectionFilterMap() {
    throw new AssertionError("not instantiable");
  }

  public static Map<String, CollectionFilterType> parseCollectionToFilter(
      List<String> collectionTo) {
    return mapValues(parseCommon(collectionTo), CollectionFilterType::parse);
  }

  public static Map<CollectionFilterType, String> parseFilterToCollection(
      List<String> toCollection) {
    return mapKeys(parseCommon(toCollection), CollectionFilterType::parse);
  }

  private static Map<String, String> parseCommon(List<String> map) {
    Map<String, String> result = new HashMap<>();
    String delimiter = "=";
    for (String entry : map) {
      int delimiterIndex = entry.indexOf(delimiter);
      if (delimiterIndex == -1) {
        throw new IllegalArgumentException("Bad entry: '" + entry
            + "'. Expected exactly one equals (=) character separating collection and filter.");
      }
      String firstPart = entry.substring(0, delimiterIndex);
      String secondPart = entry.substring(delimiterIndex + 1);

      result.put(firstPart, secondPart);
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
