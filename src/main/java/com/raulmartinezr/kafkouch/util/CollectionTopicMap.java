package com.raulmartinezr.kafkouch.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class CollectionTopicMap {
  private CollectionTopicMap() {
    throw new AssertionError("not instantiable");
  }

  public static Map<String, DatabaseAndDocumentType> parseTopicToCollection(List<String> topicTo) {
    return mapValues(parseCommon(topicTo), DatabaseAndDocumentType::parse);
  }

  public static Map<DatabaseAndDocumentType, String> parseCollectionToTopic(List<String> toTopic) {
    return mapKeys(parseCommon(toTopic), DatabaseAndDocumentType::parse);
  }

  private static Map<String, String> parseCommon(List<String> map) {
    Map<String, String> result = new HashMap<>();
    for (String entry : map) {
      String[] components = entry.split("=", -1);
      if (components.length != 2) {
        throw new IllegalArgumentException("Bad entry: '" + entry
            + "'. Expected exactly one equals (=) character separating topic and collection.");
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
